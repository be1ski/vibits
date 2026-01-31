package space.be1ski.vibits.shared.feature.habits.domain.usecase

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.feature.memos.domain.repository.MemosRepository
import space.be1ski.vibits.shared.test.FakeMemosRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SaveDailyHabitMemoUseCaseTest {
  @Test
  fun `when no existing memo for date then creates new memo`() =
    runTest {
      val expectedMemo = Memo(name = "memos/1", content = "#habits/daily 2026-01-30\n\nexercise")
      val repository =
        FakeMemosRepository().apply {
          cachedMemosResult = emptyList()
          createMemoResult = Result.success(expectedMemo)
        }
      val useCase = SaveDailyHabitMemoUseCase(repository)

      val result = useCase("#habits/daily 2026-01-30\n\nexercise")

      assertTrue(result is SaveDailyMemoResult.Created)
      assertEquals(expectedMemo, (result as SaveDailyMemoResult.Created).memo)
      assertEquals(1, repository.createMemoCalls)
      assertEquals(0, repository.updateMemoCalls)
    }

  @Test
  fun `when existing memo for date exists then updates instead of creating`() =
    runTest {
      val existingMemo = Memo(name = "memos/existing", content = "#habits/daily 2026-01-30\n\nold-habit")
      val updatedMemo = Memo(name = "memos/existing", content = "#habits/daily 2026-01-30\n\nexercise")
      val repository =
        FakeMemosRepository().apply {
          cachedMemosResult = listOf(existingMemo)
          updateMemoResult = Result.success(updatedMemo)
        }
      val useCase = SaveDailyHabitMemoUseCase(repository)

      val result = useCase("#habits/daily 2026-01-30\n\nexercise")

      assertTrue(result is SaveDailyMemoResult.Updated)
      assertEquals(updatedMemo, (result as SaveDailyMemoResult.Updated).memo)
      assertEquals(0, repository.createMemoCalls, "Should not create when memo exists")
      assertEquals(1, repository.updateMemoCalls, "Should update existing memo")
    }

  @Test
  fun `when content has invalid format then returns error`() =
    runTest {
      val repository = FakeMemosRepository()
      val useCase = SaveDailyHabitMemoUseCase(repository)

      val result = useCase("invalid content without date")

      assertTrue(result is SaveDailyMemoResult.Error)
      assertTrue((result as SaveDailyMemoResult.Error).message.contains("no date found"))
      assertEquals(0, repository.createMemoCalls)
      assertEquals(0, repository.updateMemoCalls)
    }

  @Test
  fun `when create fails then returns error`() =
    runTest {
      val repository =
        FakeMemosRepository().apply {
          cachedMemosResult = emptyList()
          createMemoResult = Result.failure(Exception("Network error"))
        }
      val useCase = SaveDailyHabitMemoUseCase(repository)

      val result = useCase("#habits/daily 2026-01-30\n\nexercise")

      assertTrue(result is SaveDailyMemoResult.Error)
      assertEquals("Network error", (result as SaveDailyMemoResult.Error).message)
    }

  @Test
  fun `when update fails then returns error`() =
    runTest {
      val existingMemo = Memo(name = "memos/existing", content = "#habits/daily 2026-01-30\n\nold")
      val repository =
        FakeMemosRepository().apply {
          cachedMemosResult = listOf(existingMemo)
          updateMemoResult = Result.failure(Exception("Update failed"))
        }
      val useCase = SaveDailyHabitMemoUseCase(repository)

      val result = useCase("#habits/daily 2026-01-30\n\nexercise")

      assertTrue(result is SaveDailyMemoResult.Error)
      assertEquals("Update failed", (result as SaveDailyMemoResult.Error).message)
    }

  @Test
  fun `when memos for different dates exist then creates new memo`() =
    runTest {
      val otherDateMemo = Memo(name = "memos/other", content = "#habits/daily 2026-01-29\n\nyesterday-habit")
      val expectedMemo = Memo(name = "memos/new", content = "#habits/daily 2026-01-30\n\nexercise")
      val repository =
        FakeMemosRepository().apply {
          cachedMemosResult = listOf(otherDateMemo)
          createMemoResult = Result.success(expectedMemo)
        }
      val useCase = SaveDailyHabitMemoUseCase(repository)

      val result = useCase("#habits/daily 2026-01-30\n\nexercise")

      assertTrue(result is SaveDailyMemoResult.Created)
      assertEquals(1, repository.createMemoCalls)
    }

  @Test
  fun `concurrent calls for same date do not create duplicates`() =
    runTest {
      val createdMemo = Memo(name = "memos/1", content = "#habits/daily 2026-01-30\n\nexercise")
      val repository = StatefulFakeMemosRepository()
      val useCase = SaveDailyHabitMemoUseCase(repository)

      // Launch 3 concurrent calls for the same date
      val results =
        listOf(
          async { useCase("#habits/daily 2026-01-30\n\nexercise") },
          async { useCase("#habits/daily 2026-01-30\n\nmeditation") },
          async { useCase("#habits/daily 2026-01-30\n\nreading") },
        ).awaitAll()

      // Due to mutex synchronization, only ONE should create, others should update
      val createdCount = results.count { it is SaveDailyMemoResult.Created }
      val updatedCount = results.count { it is SaveDailyMemoResult.Updated }

      assertEquals(1, createdCount, "Only one call should create a memo")
      assertEquals(2, updatedCount, "Other calls should update the existing memo")
      assertEquals(1, repository.createCount, "Repository.createMemo should only be called once")
    }

  @Test
  fun `rapid habit toggles for same date result in single memo`() =
    runTest {
      // This simulates the exact race condition scenario:
      // User rapidly marks 3 different habits as done for the same date
      val repository = StatefulFakeMemosRepository()
      val useCase = SaveDailyHabitMemoUseCase(repository)

      // Simulate 3 rapid habit toggles for the same date
      useCase("#habits/daily 2026-01-30\n\nexercise")
      useCase("#habits/daily 2026-01-30\n\nexercise\nmeditation")
      useCase("#habits/daily 2026-01-30\n\nexercise\nmeditation\nreading")

      assertEquals(1, repository.createCount, "Should only create one memo")
      assertEquals(2, repository.updateCount, "Should update twice for subsequent toggles")
      assertEquals(1, repository.memos.size, "Should have exactly one memo in cache")
      assertTrue(
        repository.memos[0].content.contains("reading"),
        "Final memo should have all habits",
      )
    }
}

/**
 * A stateful fake repository that simulates real behavior:
 * - Created memos appear in cachedMemos()
 * - Updated memos update the cached version
 */
private class StatefulFakeMemosRepository : MemosRepository {
  val memos = mutableListOf<Memo>()
  var createCount = 0
    private set
  var updateCount = 0
    private set
  private var nextId = 1

  override suspend fun cachedMemos(): List<Memo> = memos.toList()

  override suspend fun listMemos(): List<Memo> = memos.toList()

  override suspend fun createMemo(content: String): Memo {
    createCount++
    val memo = Memo(name = "memos/${nextId++}", content = content)
    memos.add(memo)
    return memo
  }

  override suspend fun updateMemo(
    name: String,
    content: String,
  ): Memo {
    updateCount++
    val index = memos.indexOfFirst { it.name == name }
    val updated = memos[index].copy(content = content)
    memos[index] = updated
    return updated
  }

  override suspend fun deleteMemo(name: String) {
    memos.removeAll { it.name == name }
  }
}
