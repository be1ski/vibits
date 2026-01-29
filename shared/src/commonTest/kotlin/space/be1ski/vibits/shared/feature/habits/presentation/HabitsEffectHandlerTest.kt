package space.be1ski.vibits.shared.feature.habits.presentation
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import space.be1ski.vibits.shared.app.domain.model.ActivityMode
import space.be1ski.vibits.shared.app.domain.model.ActivityRange
import space.be1ski.vibits.shared.feature.habits.presentation.action.HabitsAction
import space.be1ski.vibits.shared.feature.habits.presentation.effect.HabitsActivityEffectHandler
import space.be1ski.vibits.shared.feature.habits.presentation.effect.HabitsEffect
import space.be1ski.vibits.shared.feature.habits.presentation.effect.HabitsEffectHandler
import space.be1ski.vibits.shared.feature.habits.presentation.effect.HabitsMemoEffectHandler
import space.be1ski.vibits.shared.feature.habits.presentation.effect.HabitsRefreshEffectHandler
import space.be1ski.vibits.shared.feature.habits.presentation.reducer.habitsReducer
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import space.be1ski.vibits.shared.test.FakeMemosRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HabitsEffectHandlerTest {
  @Test
  fun `when CreateMemo effect succeeds then emits MemoCreated`() =
    runTest {
      val expectedMemo = Memo(name = "memos/1", content = "test")
      val repository =
        FakeMemosRepository().apply {
          createMemoResult = Result.success(expectedMemo)
        }
      val handler = createHandler(repository)

      val actions = handler(HabitsEffect.CreateMemo(content = "test")).toList()

      assertEquals(listOf(HabitsAction.Response.MemoCreated(expectedMemo)), actions)
      assertEquals(1, repository.createMemoCalls)
    }

  @Test
  fun `when CreateMemo effect fails then emits MemoOperationFailed`() =
    runTest {
      val repository =
        FakeMemosRepository().apply {
          createMemoResult = Result.failure(Exception("Network error"))
        }
      val handler = createHandler(repository)

      val actions = handler(HabitsEffect.CreateMemo(content = "test")).toList()

      assertEquals(1, actions.size)
      assertTrue(actions[0] is HabitsAction.Response.MemoOperationFailed)
      assertEquals("Network error", (actions[0] as HabitsAction.Response.MemoOperationFailed).error)
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
          createTime = kotlinx.datetime.Instant.parse("2026-01-20T10:00:00Z"),
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
    val buildActivityDataUseCase =
      space.be1ski.vibits.shared.feature.habits.domain.usecase.BuildActivityDataUseCase(
        buildDayDataUseCase =
          space.be1ski.vibits.shared.feature.habits.domain.usecase
            .BuildDayDataUseCase(),
      )
    val calculateSuccessRateUseCase =
      space.be1ski.vibits.shared.feature.habits.domain.usecase
        .CalculateSuccessRateUseCase()
    val calculateActivityDataUseCase =
      space.be1ski.vibits.shared.feature.habits.domain.usecase
        .CalculateActivityDataUseCase(
          buildActivityDataUseCase = buildActivityDataUseCase,
          calculateSuccessRateUseCase = calculateSuccessRateUseCase,
        )
    return HabitsEffectHandler(
      memoHandler =
        HabitsMemoEffectHandler(
          memosRepository = repository,
        ),
      refreshHandler =
        HabitsRefreshEffectHandler(
          onRefresh = onRefresh,
        ),
      activityHandler =
        HabitsActivityEffectHandler(
          calculateActivityDataUseCase = calculateActivityDataUseCase,
          prewarmActivityDataUseCase =
            space.be1ski.vibits.shared.feature.habits.domain.usecase
              .PrewarmActivityDataUseCase(
                calculateActivityDataUseCase = calculateActivityDataUseCase,
              ),
        ),
    )
  }
}
