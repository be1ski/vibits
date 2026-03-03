package space.be1ski.vibits.feature.habits.domain.usecase

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import space.be1ski.vibits.core.platform.date.currentLocalDate
import space.be1ski.vibits.feature.habits.domain.model.SaveConfigMemoResult
import space.be1ski.vibits.feature.memos.domain.model.Memo
import space.be1ski.vibits.feature.memos.domain.repository.MemosRepository
import space.be1ski.vibits.feature.memos.domain.test.FakeMemosRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours

class SaveHabitsConfigMemoUseCaseTest {
  private val timeZone = TimeZone.currentSystemDefault()
  private val today = currentLocalDate()
  private val todayInstant = today.atStartOfDayIn(timeZone) + 12.hours
  private val yesterdayInstant = today.minus(DatePeriod(days = 1)).atStartOfDayIn(timeZone) + 12.hours

  @Test
  fun `when no config memos exist then creates new memo`() =
    runTest {
      val expectedMemo = Memo(name = "memos/1", content = "#habits/config\nExercise | #habits/exercise")
      val repository =
        FakeMemosRepository().apply {
          cachedMemosResult = emptyList()
          createMemoResult = Result.success(expectedMemo)
        }
      val useCase = SaveHabitsConfigMemoUseCase(repository)

      val result = useCase("#habits/config\nExercise | #habits/exercise")

      assertTrue(result is SaveConfigMemoResult.Created)
      assertEquals(expectedMemo, result.memo)
      assertEquals(1, repository.createMemoCalls)
      assertEquals(0, repository.updateMemoCalls)
    }

  @Test
  fun `when today config memo exists then updates it`() =
    runTest {
      val existingConfig =
        Memo(
          name = "memos/old-config",
          content = "#habits/config\nExercise | #habits/exercise",
          createTime = todayInstant,
        )
      val updatedMemo =
        Memo(
          name = "memos/old-config",
          content = "#habits/config\nExercise | #habits/exercise\nReading | #habits/reading",
        )
      val repository =
        FakeMemosRepository().apply {
          cachedMemosResult = listOf(existingConfig)
          updateMemoResult = Result.success(updatedMemo)
        }
      val useCase = SaveHabitsConfigMemoUseCase(repository)

      val result = useCase("#habits/config\nExercise | #habits/exercise\nReading | #habits/reading")

      assertTrue(result is SaveConfigMemoResult.Updated)
      assertEquals(updatedMemo, result.memo)
      assertEquals(0, repository.createMemoCalls)
      assertEquals(1, repository.updateMemoCalls)
    }

  @Test
  fun `when config memo from yesterday exists then creates new memo`() =
    runTest {
      val yesterdayConfig =
        Memo(
          name = "memos/yesterday-config",
          content = "#habits/config\nExercise | #habits/exercise",
          createTime = yesterdayInstant,
        )
      val expectedMemo =
        Memo(name = "memos/new", content = "#habits/config\nExercise | #habits/exercise\nReading | #habits/reading")
      val repository =
        FakeMemosRepository().apply {
          cachedMemosResult = listOf(yesterdayConfig)
          createMemoResult = Result.success(expectedMemo)
        }
      val useCase = SaveHabitsConfigMemoUseCase(repository)

      val result = useCase("#habits/config\nExercise | #habits/exercise\nReading | #habits/reading")

      assertTrue(result is SaveConfigMemoResult.Created)
      assertEquals(expectedMemo, result.memo)
      assertEquals(1, repository.createMemoCalls)
      assertEquals(0, repository.updateMemoCalls)
    }

  @Test
  fun `when repository throws then returns error`() =
    runTest {
      val repository =
        FakeMemosRepository().apply {
          cachedMemosResult = emptyList()
          createMemoResult = Result.failure(Exception("Network error"))
        }
      val useCase = SaveHabitsConfigMemoUseCase(repository)

      val result = useCase("#habits/config\nExercise | #habits/exercise")

      assertTrue(result is SaveConfigMemoResult.Error)
      assertEquals("Network error", result.message)
    }

  @Test
  fun `when cancelled then CancellationException propagates`() =
    runTest {
      val repository =
        FakeMemosRepository().apply {
          cachedMemosResult = emptyList()
          createMemoResult = Result.failure(CancellationException("cancelled"))
        }
      val useCase = SaveHabitsConfigMemoUseCase(repository)

      assertFailsWith<CancellationException> {
        useCase("#habits/config\nExercise | #habits/exercise")
      }
    }

  @Test
  fun `when multiple config memos and today exists then updates today`() =
    runTest {
      val oldConfig =
        Memo(
          name = "memos/old",
          content = "#habits/config\nOld | #habits/old",
          createTime = yesterdayInstant,
        )
      val todayConfig =
        Memo(
          name = "memos/today",
          content = "#habits/config\nExercise | #habits/exercise",
          createTime = todayInstant,
        )
      val updatedMemo =
        Memo(
          name = "memos/today",
          content = "#habits/config\nExercise | #habits/exercise\nReading | #habits/reading",
        )
      val repository =
        FakeMemosRepository().apply {
          cachedMemosResult = listOf(oldConfig, todayConfig)
          updateMemoResult = Result.success(updatedMemo)
        }
      val useCase = SaveHabitsConfigMemoUseCase(repository)

      val result = useCase("#habits/config\nExercise | #habits/exercise\nReading | #habits/reading")

      assertTrue(result is SaveConfigMemoResult.Updated)
      assertEquals(updatedMemo, result.memo)
    }

  @Test
  fun `when non-config memos exist but no config then creates`() =
    runTest {
      val dailyMemo =
        Memo(
          name = "memos/daily",
          content = "#habits/daily 2026-03-03\n\n#habits/exercise",
          createTime = todayInstant,
        )
      val expectedMemo = Memo(name = "memos/new", content = "#habits/config\nExercise | #habits/exercise")
      val repository =
        FakeMemosRepository().apply {
          cachedMemosResult = listOf(dailyMemo)
          createMemoResult = Result.success(expectedMemo)
        }
      val useCase = SaveHabitsConfigMemoUseCase(repository)

      val result = useCase("#habits/config\nExercise | #habits/exercise")

      assertTrue(result is SaveConfigMemoResult.Created)
      assertEquals(1, repository.createMemoCalls)
    }

  @Test
  fun `when stateful repository used then dedup prevents duplicates`() =
    runTest {
      val repository = StatefulFakeRepository()
      val useCase = SaveHabitsConfigMemoUseCase(repository)

      val result1 = useCase("#habits/config\nExercise | #habits/exercise")
      assertTrue(result1 is SaveConfigMemoResult.Created)
      assertEquals(1, repository.createCount)

      // Second call sees the first memo in cachedMemos and updates
      val result2 = useCase("#habits/config\nExercise | #habits/exercise\nReading | #habits/reading")
      assertTrue(result2 is SaveConfigMemoResult.Updated)
      assertEquals(1, repository.createCount)
      assertEquals(1, repository.updateCount)
    }
}

private class StatefulFakeRepository : MemosRepository {
  val memos = mutableListOf<Memo>()
  var createCount = 0
    private set
  var updateCount = 0
    private set
  private var nextId = 1
  private val timeZone = TimeZone.currentSystemDefault()

  override suspend fun cachedMemos(): List<Memo> = memos.toList()

  override suspend fun listMemos(): List<Memo> = memos.toList()

  override suspend fun createMemo(content: String): Memo {
    createCount++
    val today = currentLocalDate()
    val memo =
      Memo(
        name = "memos/${nextId++}",
        content = content,
        createTime = today.atStartOfDayIn(timeZone) + 12.hours,
      )
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
