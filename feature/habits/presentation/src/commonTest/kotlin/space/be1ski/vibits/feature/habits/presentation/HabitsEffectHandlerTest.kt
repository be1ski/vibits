package space.be1ski.vibits.feature.habits.presentation
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.feature.habits.domain.model.ActivityMode
import space.be1ski.vibits.feature.habits.domain.model.ActivityRange
import space.be1ski.vibits.feature.habits.domain.usecase.CalculateActivityDataUseCase
import space.be1ski.vibits.feature.habits.domain.usecase.CalculateSuccessRateUseCase
import space.be1ski.vibits.feature.habits.domain.usecase.PrewarmActivityDataUseCase
import space.be1ski.vibits.feature.habits.domain.usecase.SaveDailyHabitMemoUseCase
import space.be1ski.vibits.feature.habits.presentation.action.HabitsAction
import space.be1ski.vibits.feature.habits.presentation.effect.HabitsActivityEffectHandler
import space.be1ski.vibits.feature.habits.presentation.effect.HabitsEffect
import space.be1ski.vibits.feature.habits.presentation.effect.HabitsEffectHandler
import space.be1ski.vibits.feature.habits.presentation.effect.HabitsMemoEffectHandler
import space.be1ski.vibits.feature.habits.presentation.effect.HabitsRefreshEffectHandler
import space.be1ski.vibits.feature.main.test.FakeMemosRepository
import space.be1ski.vibits.feature.memos.domain.model.Memo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HabitsEffectHandlerTest {
  @Test
  fun `when CreateMemo with daily content succeeds then emits MemoCreated`() =
    runTest {
      val dailyContent = "#habits/daily 2026-01-30\n\nexercise"
      val expectedMemo = Memo(name = "memos/1", content = dailyContent)
      val repository =
        FakeMemosRepository().apply {
          cachedMemosResult = emptyList() // No existing memo for the date
          createMemoResult = Result.success(expectedMemo)
        }
      val handler = createHandler(repository)

      val actions = handler(HabitsEffect.CreateMemo(content = dailyContent)).toList()

      assertEquals(listOf(HabitsAction.Response.MemoCreated(expectedMemo)), actions)
      assertEquals(1, repository.createMemoCalls)
    }

  @Test
  fun `when CreateMemo with config content succeeds then emits MemoCreated`() =
    runTest {
      val configContent = "#habits/config\n\nExercise | exercise | #4CAF50"
      val expectedMemo = Memo(name = "memos/1", content = configContent)
      val repository =
        FakeMemosRepository().apply {
          createMemoResult = Result.success(expectedMemo)
        }
      val handler = createHandler(repository)

      val actions = handler(HabitsEffect.CreateMemo(content = configContent)).toList()

      assertEquals(listOf(HabitsAction.Response.MemoCreated(expectedMemo)), actions)
      assertEquals(1, repository.createMemoCalls)
    }

  @Test
  fun `when CreateMemo with config content fails then emits MemoOperationFailed`() =
    runTest {
      val configContent = "#habits/config\n\nExercise | exercise | #4CAF50"
      val repository =
        FakeMemosRepository().apply {
          createMemoResult = Result.failure(Exception("Storage error"))
        }
      val handler = createHandler(repository)

      val actions = handler(HabitsEffect.CreateMemo(content = configContent)).toList()

      assertEquals(1, actions.size)
      assertTrue(actions[0] is HabitsAction.Response.MemoOperationFailed)
      assertEquals("Storage error", (actions[0] as HabitsAction.Response.MemoOperationFailed).error)
    }

  @Test
  fun `when CreateMemo with daily content fails then emits MemoOperationFailed`() =
    runTest {
      val dailyContent = "#habits/daily 2026-01-30\n\nexercise"
      val repository =
        FakeMemosRepository().apply {
          cachedMemosResult = emptyList()
          createMemoResult = Result.failure(Exception("Network error"))
        }
      val handler = createHandler(repository)

      val actions = handler(HabitsEffect.CreateMemo(content = dailyContent)).toList()

      assertEquals(1, actions.size)
      assertTrue(actions[0] is HabitsAction.Response.MemoOperationFailed)
      assertEquals("Network error", (actions[0] as HabitsAction.Response.MemoOperationFailed).error)
    }

  @Test
  fun `when CreateMemo with daily content finds existing memo for date then updates instead of creating`() =
    runTest {
      val existingMemo = Memo(name = "memos/existing", content = "#habits/daily 2026-01-30\n\nold-habit")
      val newContent = "#habits/daily 2026-01-30\n\nexercise\nmeditation"
      val updatedMemo = Memo(name = "memos/existing", content = newContent)
      val repository =
        FakeMemosRepository().apply {
          cachedMemosResult = listOf(existingMemo) // Existing memo for the same date
          updateMemoResult = Result.success(updatedMemo)
        }
      val handler = createHandler(repository)

      val actions = handler(HabitsEffect.CreateMemo(content = newContent)).toList()

      assertEquals(listOf(HabitsAction.Response.MemoUpdated(updatedMemo)), actions)
      assertEquals(0, repository.createMemoCalls, "Should not create when memo exists")
      assertEquals(1, repository.updateMemoCalls, "Should update existing memo")
    }

  @Test
  fun `when UpdateMemo effect succeeds then emits MemoUpdated`() =
    runTest {
      val expectedMemo = Memo(name = "memos/1", content = "updated")
      val repository =
        FakeMemosRepository().apply {
          updateMemoResult = Result.success(expectedMemo)
        }
      val handler = createHandler(repository)

      val actions =
        handler(
          HabitsEffect.UpdateMemo(name = "memos/1", content = "updated"),
        ).toList()

      assertEquals(listOf(HabitsAction.Response.MemoUpdated(expectedMemo)), actions)
      assertEquals(1, repository.updateMemoCalls)
    }

  @Test
  fun `when UpdateMemo effect fails then emits MemoOperationFailed`() =
    runTest {
      val repository =
        FakeMemosRepository().apply {
          updateMemoResult = Result.failure(Exception("Update failed"))
        }
      val handler = createHandler(repository)

      val actions =
        handler(
          HabitsEffect.UpdateMemo(name = "memos/1", content = "updated"),
        ).toList()

      assertEquals(1, actions.size)
      assertTrue(actions[0] is HabitsAction.Response.MemoOperationFailed)
    }

  @Test
  fun `when DeleteMemo effect succeeds then emits MemoDeleted`() =
    runTest {
      val repository =
        FakeMemosRepository().apply {
          deleteMemoResult = Result.success(Unit)
        }
      val handler = createHandler(repository)

      val actions = handler(HabitsEffect.DeleteMemo(name = "memos/1")).toList()

      assertEquals(listOf(HabitsAction.Response.MemoDeleted("memos/1")), actions)
      assertEquals(1, repository.deleteMemoCalls)
    }

  @Test
  fun `when DeleteMemo effect fails then emits MemoOperationFailed`() =
    runTest {
      val repository =
        FakeMemosRepository().apply {
          deleteMemoResult = Result.failure(Exception("Delete failed"))
        }
      val handler = createHandler(repository)

      val actions = handler(HabitsEffect.DeleteMemo(name = "memos/1")).toList()

      assertEquals(1, actions.size)
      assertTrue(actions[0] is HabitsAction.Response.MemoOperationFailed)
    }

  @Test
  fun `when RefreshMemos effect then calls onRefresh callback`() =
    runTest {
      var refreshCalled = false
      val handler = createHandler(onRefresh = { refreshCalled = true })

      handler(HabitsEffect.RefreshMemos).toList()

      assertTrue(refreshCalled)
    }

  @Test
  fun `when ToggleDailyHabit creates new memo then emits MemoCreated`() =
    runTest {
      val date = LocalDate(2026, 1, 30)
      val expectedMemo = Memo(name = "memos/1", content = "#habits/daily 2026-01-30\n\nexercise")
      val repository =
        FakeMemosRepository().apply {
          cachedMemosResult = emptyList()
          createMemoResult = Result.success(expectedMemo)
        }
      val handler = createHandler(repository)
      val habitsConfig =
        listOf(
          space.be1ski.vibits.feature.habits.domain.model.HabitConfig(
            tag = "exercise",
            label = "Exercise",
          ),
        )

      val actions =
        handler(
          HabitsEffect.ToggleDailyHabit(
            date = date,
            habitTag = "exercise",
            habitsConfig = habitsConfig,
          ),
        ).toList()

      assertEquals(1, actions.size)
      assertTrue(actions[0] is HabitsAction.Response.MemoCreated)
    }

  @Test
  fun `when ToggleDailyHabit updates existing memo then emits MemoUpdated`() =
    runTest {
      val date = LocalDate(2026, 1, 30)
      val existingMemo =
        Memo(name = "memos/existing", content = "#habits/daily 2026-01-30\n\n- [x] meditation")
      val updatedMemo = Memo(name = "memos/existing", content = "#habits/daily 2026-01-30\n\n- [x] exercise\n- [x] meditation")
      val repository =
        FakeMemosRepository().apply {
          cachedMemosResult = listOf(existingMemo)
          updateMemoResult = Result.success(updatedMemo)
        }
      val handler = createHandler(repository)
      val habitsConfig =
        listOf(
          space.be1ski.vibits.feature.habits.domain.model
            .HabitConfig(tag = "exercise", label = "Exercise"),
          space.be1ski.vibits.feature.habits.domain.model
            .HabitConfig(tag = "meditation", label = "Meditation"),
        )

      val actions =
        handler(
          HabitsEffect.ToggleDailyHabit(
            date = date,
            habitTag = "exercise",
            habitsConfig = habitsConfig,
          ),
        ).toList()

      assertEquals(1, actions.size)
      assertTrue(actions[0] is HabitsAction.Response.MemoUpdated)
    }

  @Test
  fun `when ToggleDailyHabit removes last habit then emits MemoDeleted`() =
    runTest {
      val date = LocalDate(2026, 1, 30)
      val existingMemo =
        Memo(name = "memos/existing", content = "#habits/daily 2026-01-30\n\n- [x] exercise")
      val repository =
        FakeMemosRepository().apply {
          cachedMemosResult = listOf(existingMemo)
          deleteMemoResult = Result.success(Unit)
        }
      val handler = createHandler(repository)
      val habitsConfig =
        listOf(
          space.be1ski.vibits.feature.habits.domain.model
            .HabitConfig(tag = "exercise", label = "Exercise"),
        )

      val actions =
        handler(
          HabitsEffect.ToggleDailyHabit(
            date = date,
            habitTag = "exercise",
            habitsConfig = habitsConfig,
          ),
        ).toList()

      assertEquals(1, actions.size)
      assertTrue(actions[0] is HabitsAction.Response.MemoDeleted)
    }

  @Test
  fun `when ToggleDailyHabit fails then emits MemoOperationFailed`() =
    runTest {
      val date = LocalDate(2026, 1, 30)
      val repository =
        FakeMemosRepository().apply {
          cachedMemosResult = emptyList()
          createMemoResult = Result.failure(Exception("Network error"))
        }
      val handler = createHandler(repository)
      val habitsConfig =
        listOf(
          space.be1ski.vibits.feature.habits.domain.model
            .HabitConfig(tag = "exercise", label = "Exercise"),
        )

      val actions =
        handler(
          HabitsEffect.ToggleDailyHabit(
            date = date,
            habitTag = "exercise",
            habitsConfig = habitsConfig,
          ),
        ).toList()

      assertEquals(1, actions.size)
      assertTrue(actions[0] is HabitsAction.Response.MemoOperationFailed)
    }

  @Test
  fun `when RecalculateActivityData effect then emits UpdateActivityData`() =
    runTest {
      val handler = createHandler()
      val memo = Memo(name = "memos/1", content = "#habits/exercise")
      val memos = listOf(memo)
      val range = ActivityRange.Week(LocalDate(2026, 1, 20))
      val mode = ActivityMode.HABITS
      val appMode = AppMode.ONLINE

      val actions =
        handler(
          HabitsEffect.RecalculateActivityData(
            range = range,
            mode = mode,
            appMode = appMode,
            memos = memos,
          ),
        ).toList()

      assertEquals(1, actions.size)
      val action = actions[0] as HabitsAction.Cache.UpdateActivityData
      assertEquals(range, action.range)
      assertEquals(mode, action.mode)
      assertEquals(appMode, action.appMode)
    }

  @Test
  fun `when RunPrewarmAllRanges effect then emits UpdateActivityData for all ranges and modes`() =
    runTest {
      val handler = createHandler()
      val memo =
        Memo(
          name = "memos/1",
          content = "#habits/exercise",
          createTime = kotlin.time.Instant.parse("2026-01-20T10:00:00Z"),
        )
      val memos = listOf(memo)
      val appMode = AppMode.ONLINE

      val actions =
        handler(
          HabitsEffect.RunPrewarmAllRanges(
            memos = memos,
            appMode = appMode,
          ),
        ).toList()

      val updateActions = actions.filterIsInstance<HabitsAction.Cache.UpdateActivityData>()
      val completedActions = actions.filterIsInstance<HabitsAction.Cache.PrewarmCompleted>()

      assertTrue(updateActions.isNotEmpty(), "Should emit UpdateActivityData actions")
      assertEquals(1, completedActions.size, "Should emit PrewarmCompleted once")
      assertTrue(
        updateActions.all { it.appMode == appMode },
        "All actions should have correct appMode",
      )
    }

  private fun createHandler(
    repository: FakeMemosRepository = FakeMemosRepository(),
    onRefresh: () -> Unit = {},
  ): HabitsEffectHandler {
    val calculateSuccessRateUseCase = CalculateSuccessRateUseCase()
    val calculateActivityDataUseCase = CalculateActivityDataUseCase(calculateSuccessRateUseCase)
    return HabitsEffectHandler(
      memoHandler =
        HabitsMemoEffectHandler(
          memosRepository = repository,
          saveDailyHabitMemo = SaveDailyHabitMemoUseCase(repository),
        ),
      refreshHandler =
        HabitsRefreshEffectHandler(
          onRefresh = onRefresh,
        ),
      activityHandler =
        HabitsActivityEffectHandler(
          calculateActivityDataUseCase = calculateActivityDataUseCase,
          prewarmActivityDataUseCase = PrewarmActivityDataUseCase(calculateActivityDataUseCase),
        ),
    )
  }
}
