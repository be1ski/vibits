package space.be1ski.vibits.feature.habits.domain.usecase

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import space.be1ski.vibits.feature.habits.domain.model.HabitColor
import space.be1ski.vibits.feature.habits.domain.model.HabitConfig
import space.be1ski.vibits.feature.habits.domain.model.SaveDailyMemoResult
import space.be1ski.vibits.feature.memos.domain.model.Memo
import space.be1ski.vibits.feature.memos.domain.repository.MemosRepository
import space.be1ski.vibits.feature.memos.domain.test.FakeMemosRepository
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
      assertEquals(expectedMemo, result.memo)
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
      assertEquals(updatedMemo, result.memo)
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
      assertTrue(result.message.contains("no date found"))
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
      assertEquals("Network error", result.message)
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
      assertEquals("Update failed", result.message)
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
  fun `when concurrent calls for same date then does not create duplicates`() =
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
  fun `when rapid habit toggles for same date then results in single memo`() =
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

  @Test
  fun `when toggleHabit with no existing memo then creates memo`() =
    runTest {
      val repository = StatefulFakeMemosRepository()
      val useCase = SaveDailyHabitMemoUseCase(repository)
      val config =
        listOf(
          HabitConfig(tag = "#habits/exercise", label = "Exercise", color = HabitColor(0xFF000000)),
          HabitConfig(tag = "#habits/meditation", label = "Meditation", color = HabitColor(0xFF000000)),
        )

      val result = useCase.toggleHabit(LocalDate(2026, 1, 30), "#habits/exercise", config)

      assertTrue(result is SaveDailyMemoResult.Created)
      assertEquals(1, repository.createCount)
      assertTrue(repository.memos[0].content.contains("#habits/exercise"))
    }

  @Test
  fun `when toggleHabit with existing memo then adds new habit`() =
    runTest {
      val repository = StatefulFakeMemosRepository()
      // Pre-populate with existing daily memo
      repository.memos.add(Memo(name = "memos/1", content = "#habits/daily 2026-01-30\n\n#habits/exercise\n"))
      val useCase = SaveDailyHabitMemoUseCase(repository)
      val config =
        listOf(
          HabitConfig(tag = "#habits/exercise", label = "Exercise", color = HabitColor(0xFF000000)),
          HabitConfig(tag = "#habits/meditation", label = "Meditation", color = HabitColor(0xFF000000)),
        )

      val result = useCase.toggleHabit(LocalDate(2026, 1, 30), "#habits/meditation", config)

      assertTrue(result is SaveDailyMemoResult.Updated)
      assertEquals(1, repository.updateCount)
      assertTrue(repository.memos[0].content.contains("#habits/exercise"))
      assertTrue(repository.memos[0].content.contains("#habits/meditation"))
    }

  @Test
  fun `when toggleHabit with habit already done then removes it`() =
    runTest {
      val repository = StatefulFakeMemosRepository()
      // Pre-populate with existing daily memo that has both habits
      repository.memos.add(
        Memo(name = "memos/1", content = "#habits/daily 2026-01-30\n\n#habits/exercise\n#habits/meditation\n"),
      )
      val useCase = SaveDailyHabitMemoUseCase(repository)
      val config =
        listOf(
          HabitConfig(tag = "#habits/exercise", label = "Exercise", color = HabitColor(0xFF000000)),
          HabitConfig(tag = "#habits/meditation", label = "Meditation", color = HabitColor(0xFF000000)),
        )

      val result = useCase.toggleHabit(LocalDate(2026, 1, 30), "#habits/exercise", config)

      assertTrue(result is SaveDailyMemoResult.Updated)
      assertEquals(1, repository.updateCount)
      assertTrue(!repository.memos[0].content.contains("#habits/exercise"))
      assertTrue(repository.memos[0].content.contains("#habits/meditation"))
    }

  @Test
  fun `when toggleHabit unchecks last habit then deletes memo`() =
    runTest {
      val repository = StatefulFakeMemosRepository()
      // Pre-populate with existing daily memo that has only one habit
      repository.memos.add(Memo(name = "memos/1", content = "#habits/daily 2026-01-30\n\n#habits/exercise\n"))
      val useCase = SaveDailyHabitMemoUseCase(repository)
      val config =
        listOf(
          HabitConfig(tag = "#habits/exercise", label = "Exercise", color = HabitColor(0xFF000000)),
          HabitConfig(tag = "#habits/meditation", label = "Meditation", color = HabitColor(0xFF000000)),
        )

      val result = useCase.toggleHabit(LocalDate(2026, 1, 30), "#habits/exercise", config)

      assertTrue(result is SaveDailyMemoResult.Deleted)
      assertEquals(0, repository.memos.size)
    }

  @Test
  fun `when sequential toggleHabit calls then all habits accumulate correctly`() =
    runTest {
      // This is the key test for the race condition fix:
      // Rapid sequential toggles should ALL be applied correctly
      val repository = StatefulFakeMemosRepository()
      val useCase = SaveDailyHabitMemoUseCase(repository)
      val config =
        listOf(
          HabitConfig(tag = "#habits/exercise", label = "Exercise", color = HabitColor(0xFF000000)),
          HabitConfig(tag = "#habits/meditation", label = "Meditation", color = HabitColor(0xFF000000)),
          HabitConfig(tag = "#habits/reading", label = "Reading", color = HabitColor(0xFF000000)),
        )
      val date = LocalDate(2026, 1, 30)

      // Toggle 3 habits rapidly (simulates user clicking quickly)
      useCase.toggleHabit(date, "#habits/exercise", config)
      useCase.toggleHabit(date, "#habits/meditation", config)
      useCase.toggleHabit(date, "#habits/reading", config)

      assertEquals(1, repository.createCount, "Only first toggle should create")
      assertEquals(2, repository.updateCount, "Subsequent toggles should update")
      assertEquals(1, repository.memos.size, "Should have exactly one memo")

      val finalContent = repository.memos[0].content
      assertTrue(finalContent.contains("#habits/exercise"), "Should have exercise")
      assertTrue(finalContent.contains("#habits/meditation"), "Should have meditation")
      assertTrue(finalContent.contains("#habits/reading"), "Should have reading")
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
