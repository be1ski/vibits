package space.be1ski.vibits.feature.habits.presentation.effect

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import space.be1ski.vibits.feature.habits.domain.model.HabitColor
import space.be1ski.vibits.feature.habits.domain.model.HabitConfig
import space.be1ski.vibits.feature.habits.domain.usecase.SaveDailyHabitMemoUseCase
import space.be1ski.vibits.feature.habits.presentation.action.HabitsAction
import space.be1ski.vibits.feature.memos.domain.model.Memo
import space.be1ski.vibits.feature.memos.domain.repository.MemosRepository
import space.be1ski.vibits.feature.memos.domain.test.FakeMemosRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class HabitsMemoEffectHandlerTest {
  private val date = LocalDate(2026, 1, 30)
  private val habitsConfig =
    listOf(
      HabitConfig(tag = "#habits/exercise", label = "Exercise", color = HabitColor(0xFF000000)),
      HabitConfig(tag = "#habits/meditation", label = "Meditation", color = HabitColor(0xFF000000)),
    )

  private fun createHandler(repository: MemosRepository = FakeMemosRepository()): HabitsMemoEffectHandler {
    val saveDailyUseCase = SaveDailyHabitMemoUseCase(repository)
    return HabitsMemoEffectHandler(repository, saveDailyUseCase)
  }

  // ========== ToggleDailyHabit Tests ==========

  @Test
  fun `when ToggleDailyHabit creates new memo then emits MemoCreated`() =
    runTest {
      val repository =
        StatefulRepository().apply {
          // No existing memos - will create
        }
      val handler = createHandler(repository)
      val effect = HabitsEffect.ToggleDailyHabit(date, "#habits/exercise", habitsConfig)

      val actions = handler(effect).toList()

      assertEquals(1, actions.size)
      assertIs<HabitsAction.Response.MemoCreated>(actions[0])
    }

  @Test
  fun `when ToggleDailyHabit updates existing memo then emits MemoUpdated`() =
    runTest {
      val repository =
        StatefulRepository().apply {
          // Pre-populate with existing daily memo
          memos.add(Memo(name = "memos/1", content = "#habits/daily 2026-01-30\n\n#habits/exercise\n"))
        }
      val handler = createHandler(repository)
      val effect = HabitsEffect.ToggleDailyHabit(date, "#habits/meditation", habitsConfig)

      val actions = handler(effect).toList()

      assertEquals(1, actions.size)
      assertIs<HabitsAction.Response.MemoUpdated>(actions[0])
    }

  @Test
  fun `when ToggleDailyHabit unchecks last habit then emits MemoDeleted`() =
    runTest {
      val repository =
        StatefulRepository().apply {
          // Pre-populate with memo having single habit
          memos.add(Memo(name = "memos/1", content = "#habits/daily 2026-01-30\n\n#habits/exercise\n"))
        }
      val handler = createHandler(repository)
      val effect = HabitsEffect.ToggleDailyHabit(date, "#habits/exercise", habitsConfig)

      val actions = handler(effect).toList()

      assertEquals(1, actions.size)
      assertIs<HabitsAction.Response.MemoDeleted>(actions[0])
    }

  @Test
  fun `when ToggleDailyHabit fails then emits MemoOperationFailed`() =
    runTest {
      val repository =
        FakeMemosRepository().apply {
          cachedMemosResult = emptyList()
          createMemoResult = Result.failure(Exception("Network error"))
        }
      val handler = createHandler(repository)
      val effect = HabitsEffect.ToggleDailyHabit(date, "#habits/exercise", habitsConfig)

      val actions = handler(effect).toList()

      assertEquals(1, actions.size)
      val action = actions[0]
      assertIs<HabitsAction.Response.MemoOperationFailed>(action)
      assertEquals("Network error", action.error)
    }

  // ========== CreateMemo Tests ==========

  @Test
  fun `when CreateMemo with config content then creates directly`() =
    runTest {
      val expectedMemo = Memo(name = "memos/config", content = "#habits/config\ntest")
      val repository =
        FakeMemosRepository().apply {
          createMemoResult = Result.success(expectedMemo)
        }
      val handler = createHandler(repository)
      val effect = HabitsEffect.CreateMemo("#habits/config\ntest")

      val actions = handler(effect).toList()

      assertEquals(1, actions.size)
      assertIs<HabitsAction.Response.MemoCreated>(actions[0])
      assertEquals(1, repository.createMemoCalls)
    }

  @Test
  fun `when CreateMemo with config-alt content then creates directly`() =
    runTest {
      val expectedMemo = Memo(name = "memos/config", content = "#habits_config\ntest")
      val repository =
        FakeMemosRepository().apply {
          createMemoResult = Result.success(expectedMemo)
        }
      val handler = createHandler(repository)
      val effect = HabitsEffect.CreateMemo("#habits_config\ntest")

      val actions = handler(effect).toList()

      assertEquals(1, actions.size)
      assertIs<HabitsAction.Response.MemoCreated>(actions[0])
    }

  @Test
  fun `when CreateMemo with daily content and no existing then creates`() =
    runTest {
      val expectedMemo = Memo(name = "memos/daily", content = "#habits/daily 2026-01-30\n\nexercise")
      val repository =
        FakeMemosRepository().apply {
          cachedMemosResult = emptyList()
          createMemoResult = Result.success(expectedMemo)
        }
      val handler = createHandler(repository)
      val effect = HabitsEffect.CreateMemo("#habits/daily 2026-01-30\n\nexercise")

      val actions = handler(effect).toList()

      assertEquals(1, actions.size)
      assertIs<HabitsAction.Response.MemoCreated>(actions[0])
    }

  @Test
  fun `when CreateMemo with daily content and existing then updates`() =
    runTest {
      val existingMemo = Memo(name = "memos/1", content = "#habits/daily 2026-01-30\n\nold")
      val updatedMemo = Memo(name = "memos/1", content = "#habits/daily 2026-01-30\n\nexercise")
      val repository =
        FakeMemosRepository().apply {
          cachedMemosResult = listOf(existingMemo)
          updateMemoResult = Result.success(updatedMemo)
        }
      val handler = createHandler(repository)
      val effect = HabitsEffect.CreateMemo("#habits/daily 2026-01-30\n\nexercise")

      val actions = handler(effect).toList()

      assertEquals(1, actions.size)
      assertIs<HabitsAction.Response.MemoUpdated>(actions[0])
    }

  @Test
  fun `when CreateMemo config fails then emits MemoOperationFailed`() =
    runTest {
      val repository =
        FakeMemosRepository().apply {
          createMemoResult = Result.failure(Exception("Failed"))
        }
      val handler = createHandler(repository)
      val effect = HabitsEffect.CreateMemo("#habits/config\ntest")

      val actions = handler(effect).toList()

      assertEquals(1, actions.size)
      assertIs<HabitsAction.Response.MemoOperationFailed>(actions[0])
    }

  @Test
  fun `when CreateMemo daily fails then emits MemoOperationFailed`() =
    runTest {
      val repository =
        FakeMemosRepository().apply {
          cachedMemosResult = emptyList()
          createMemoResult = Result.failure(Exception("Network error"))
        }
      val handler = createHandler(repository)
      val effect = HabitsEffect.CreateMemo("#habits/daily 2026-01-30\n\nexercise")

      val actions = handler(effect).toList()

      assertEquals(1, actions.size)
      assertIs<HabitsAction.Response.MemoOperationFailed>(actions[0])
    }

  // ========== UpdateMemo Tests ==========

  @Test
  fun `when UpdateMemo succeeds then emits MemoUpdated`() =
    runTest {
      val updatedMemo = Memo(name = "memos/1", content = "updated")
      val repository =
        FakeMemosRepository().apply {
          updateMemoResult = Result.success(updatedMemo)
        }
      val handler = createHandler(repository)
      val effect = HabitsEffect.UpdateMemo("memos/1", "updated")

      val actions = handler(effect).toList()

      assertEquals(1, actions.size)
      assertIs<HabitsAction.Response.MemoUpdated>(actions[0])
      assertEquals(1, repository.updateMemoCalls)
    }

  @Test
  fun `when UpdateMemo fails then emits MemoOperationFailed`() =
    runTest {
      val repository =
        FakeMemosRepository().apply {
          updateMemoResult = Result.failure(Exception("Update failed"))
        }
      val handler = createHandler(repository)
      val effect = HabitsEffect.UpdateMemo("memos/1", "updated")

      val actions = handler(effect).toList()

      assertEquals(1, actions.size)
      val action = actions[0]
      assertIs<HabitsAction.Response.MemoOperationFailed>(action)
      assertEquals("Update failed", action.error)
    }

  // ========== DeleteMemo Tests ==========

  @Test
  fun `when DeleteMemo succeeds then emits MemoDeleted`() =
    runTest {
      val repository =
        FakeMemosRepository().apply {
          deleteMemoResult = Result.success(Unit)
        }
      val handler = createHandler(repository)
      val effect = HabitsEffect.DeleteMemo("memos/1")

      val actions = handler(effect).toList()

      assertEquals(1, actions.size)
      val action = actions[0]
      assertIs<HabitsAction.Response.MemoDeleted>(action)
      assertEquals("memos/1", action.name)
    }

  @Test
  fun `when DeleteMemo fails then emits MemoOperationFailed`() =
    runTest {
      val repository =
        FakeMemosRepository().apply {
          deleteMemoResult = Result.failure(Exception("Delete failed"))
        }
      val handler = createHandler(repository)
      val effect = HabitsEffect.DeleteMemo("memos/1")

      val actions = handler(effect).toList()

      assertEquals(1, actions.size)
      val action = actions[0]
      assertIs<HabitsAction.Response.MemoOperationFailed>(action)
      assertEquals("Delete failed", action.error)
    }

  // ========== Helper Classes ==========

  private class StatefulRepository : MemosRepository {
    val memos = mutableListOf<Memo>()
    private var nextId = 1

    override suspend fun cachedMemos(): List<Memo> = memos.toList()

    override suspend fun listMemos(): List<Memo> = memos.toList()

    override suspend fun createMemo(content: String): Memo {
      val memo = Memo(name = "memos/${nextId++}", content = content)
      memos.add(memo)
      return memo
    }

    override suspend fun updateMemo(
      name: String,
      content: String,
    ): Memo {
      val index = memos.indexOfFirst { it.name == name }
      val updated = memos[index].copy(content = content)
      memos[index] = updated
      return updated
    }

    override suspend fun deleteMemo(name: String) {
      memos.removeAll { it.name == name }
    }
  }
}
