package space.be1ski.vibits.feature.habits.presentation.effect

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.feature.habits.domain.model.ActivityMode
import space.be1ski.vibits.feature.habits.domain.model.ActivityRange
import space.be1ski.vibits.feature.habits.domain.usecase.CalculateActivityDataUseCase
import space.be1ski.vibits.feature.habits.domain.usecase.CalculateSuccessRateUseCase
import space.be1ski.vibits.feature.habits.domain.usecase.PrewarmActivityDataUseCase
import space.be1ski.vibits.feature.habits.presentation.action.HabitsAction
import space.be1ski.vibits.feature.memos.domain.model.Memo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HabitsActivityEffectHandlerTest {
  private val timeZone = TimeZone.UTC

  private fun createHandler(): HabitsActivityEffectHandler {
    val calculateSuccessRateUseCase = CalculateSuccessRateUseCase()
    val calculateActivityDataUseCase = CalculateActivityDataUseCase(calculateSuccessRateUseCase)
    val prewarmActivityDataUseCase = PrewarmActivityDataUseCase(calculateActivityDataUseCase)
    return HabitsActivityEffectHandler(
      calculateActivityDataUseCase = calculateActivityDataUseCase,
      prewarmActivityDataUseCase = prewarmActivityDataUseCase,
    )
  }

  private fun createMemo(
    name: String,
    content: String,
    date: LocalDate,
  ): Memo =
    Memo(
      name = name,
      content = content,
      createTime = date.atStartOfDayIn(timeZone),
    )

  // ========== RunPrewarmAllRanges Tests ==========

  @Test
  fun `when RunPrewarmAllRanges with empty memos then emits PrewarmCompleted only`() =
    runTest {
      val handler = createHandler()
      val effect = HabitsEffect.RunPrewarmAllRanges(memos = emptyList(), appMode = AppMode.OFFLINE)

      val actions = handler(effect).toList()

      assertEquals(1, actions.size)
      assertIs<HabitsAction.Cache.PrewarmCompleted>(actions[0])
    }

  @Test
  fun `when RunPrewarmAllRanges with memos then emits UpdateActivityData for each range and mode`() =
    runTest {
      val handler = createHandler()
      val configMemo =
        createMemo(
          name = "memos/1",
          content = "#habits/config\nExercise | #habits/exercise",
          date = LocalDate(2026, 1, 1),
        )
      val dailyMemo =
        createMemo(
          name = "memos/2",
          content = "#habits/daily 2026-01-30\n\n#habits/exercise",
          date = LocalDate(2026, 1, 30),
        )
      val effect =
        HabitsEffect.RunPrewarmAllRanges(
          memos = listOf(configMemo, dailyMemo),
          appMode = AppMode.OFFLINE,
        )

      val actions = handler(effect).toList()

      val updateActions = actions.filterIsInstance<HabitsAction.Cache.UpdateActivityData>()
      val completedActions = actions.filterIsInstance<HabitsAction.Cache.PrewarmCompleted>()

      assertTrue(updateActions.isNotEmpty(), "Should emit UpdateActivityData actions")
      assertEquals(1, completedActions.size, "Should emit PrewarmCompleted once")
      assertTrue(
        updateActions.all { it.appMode == AppMode.OFFLINE },
        "All actions should have appMode OFFLINE",
      )
    }

  @Test
  fun `when RunPrewarmAllRanges then emits both HABITS and POSTS modes`() =
    runTest {
      val handler = createHandler()
      val memo =
        createMemo(
          name = "memos/1",
          content = "Some content",
          date = LocalDate(2026, 1, 15),
        )
      val effect = HabitsEffect.RunPrewarmAllRanges(memos = listOf(memo), appMode = AppMode.ONLINE)

      val actions = handler(effect).toList()

      val updateActions = actions.filterIsInstance<HabitsAction.Cache.UpdateActivityData>()
      val habitsModeActions = updateActions.filter { it.mode == ActivityMode.HABITS }
      val postsModeActions = updateActions.filter { it.mode == ActivityMode.POSTS }

      assertTrue(habitsModeActions.isNotEmpty(), "Should have HABITS mode actions")
      assertTrue(postsModeActions.isNotEmpty(), "Should have POSTS mode actions")
    }

  @Test
  fun `when RunPrewarmAllRanges with online mode then passes online mode to all actions`() =
    runTest {
      val handler = createHandler()
      val memo =
        createMemo(
          name = "memos/1",
          content = "Content",
          date = LocalDate(2026, 1, 20),
        )
      val effect = HabitsEffect.RunPrewarmAllRanges(memos = listOf(memo), appMode = AppMode.ONLINE)

      val actions = handler(effect).toList()

      val updateActions = actions.filterIsInstance<HabitsAction.Cache.UpdateActivityData>()
      assertTrue(updateActions.all { it.appMode == AppMode.ONLINE })
    }

  @Test
  fun `when RunPrewarmAllRanges with demo mode then passes demo mode to all actions`() =
    runTest {
      val handler = createHandler()
      val memo =
        createMemo(
          name = "memos/1",
          content = "Demo content",
          date = LocalDate(2026, 1, 10),
        )
      val effect = HabitsEffect.RunPrewarmAllRanges(memos = listOf(memo), appMode = AppMode.DEMO)

      val actions = handler(effect).toList()

      val updateActions = actions.filterIsInstance<HabitsAction.Cache.UpdateActivityData>()
      assertTrue(updateActions.all { it.appMode == AppMode.DEMO })
    }

  @Test
  fun `when RunPrewarmAllRanges with habits config then includes config timeline in habits mode`() =
    runTest {
      val handler = createHandler()
      val configMemo =
        createMemo(
          name = "memos/1",
          content = "#habits/config\nExercise | #habits/exercise | #4CAF50",
          date = LocalDate(2026, 1, 1),
        )
      val effect =
        HabitsEffect.RunPrewarmAllRanges(
          memos = listOf(configMemo),
          appMode = AppMode.OFFLINE,
        )

      val actions = handler(effect).toList()

      val habitsActions =
        actions
          .filterIsInstance<HabitsAction.Cache.UpdateActivityData>()
          .filter { it.mode == ActivityMode.HABITS }
      assertTrue(
        habitsActions.any { it.configTimeline.isNotEmpty() },
        "At least one habits action should have config timeline",
      )
    }

  @Test
  fun `when RunPrewarmAllRanges with daily memos then calculates success rate for habits mode`() =
    runTest {
      val handler = createHandler()
      val configMemo =
        createMemo(
          name = "memos/1",
          content = "#habits/config\nExercise | #habits/exercise",
          date = LocalDate(2026, 1, 1),
        )
      val dailyMemos =
        (1..5).map { day ->
          createMemo(
            name = "memos/${day + 1}",
            content = "#habits/daily 2026-01-0$day\n\n#habits/exercise",
            date = LocalDate(2026, 1, day),
          )
        }
      val effect =
        HabitsEffect.RunPrewarmAllRanges(
          memos = listOf(configMemo) + dailyMemos,
          appMode = AppMode.OFFLINE,
        )

      val actions = handler(effect).toList()

      val habitsActions =
        actions
          .filterIsInstance<HabitsAction.Cache.UpdateActivityData>()
          .filter { it.mode == ActivityMode.HABITS }
      assertTrue(
        habitsActions.any { it.successRate != null },
        "At least one habits action should have success rate",
      )
    }

  // ========== RecalculateActivityData Tests ==========

  @Test
  fun `when RecalculateActivityData then emits UpdateActivityData`() =
    runTest {
      val handler = createHandler()
      val memo =
        createMemo(
          name = "memos/1",
          content = "Content",
          date = LocalDate(2026, 1, 15),
        )
      val range = ActivityRange.Month(year = 2026, month = Month.JANUARY)
      val effect =
        HabitsEffect.RecalculateActivityData(
          range = range,
          mode = ActivityMode.HABITS,
          appMode = AppMode.OFFLINE,
          memos = listOf(memo),
        )

      val actions = handler(effect).toList()

      assertEquals(1, actions.size)
      val action = actions[0]
      assertIs<HabitsAction.Cache.UpdateActivityData>(action)
      assertEquals(range, action.range)
      assertEquals(ActivityMode.HABITS, action.mode)
      assertEquals(AppMode.OFFLINE, action.appMode)
    }

  @Test
  fun `when RecalculateActivityData for posts mode then success rate is null`() =
    runTest {
      val handler = createHandler()
      val memo =
        createMemo(
          name = "memos/1",
          content = "Some post",
          date = LocalDate(2026, 1, 15),
        )
      val effect =
        HabitsEffect.RecalculateActivityData(
          range = ActivityRange.Month(year = 2026, month = Month.JANUARY),
          mode = ActivityMode.POSTS,
          appMode = AppMode.ONLINE,
          memos = listOf(memo),
        )

      val actions = handler(effect).toList()

      val action = actions[0]
      assertIs<HabitsAction.Cache.UpdateActivityData>(action)
      assertNull(action.successRate, "Posts mode should not have success rate")
    }

  @Test
  fun `when RecalculateActivityData for habits mode with config then includes config timeline`() =
    runTest {
      val handler = createHandler()
      val configMemo =
        createMemo(
          name = "memos/1",
          content = "#habits/config\nExercise | #habits/exercise",
          date = LocalDate(2026, 1, 1),
        )
      val effect =
        HabitsEffect.RecalculateActivityData(
          range = ActivityRange.Month(year = 2026, month = Month.JANUARY),
          mode = ActivityMode.HABITS,
          appMode = AppMode.OFFLINE,
          memos = listOf(configMemo),
        )

      val actions = handler(effect).toList()

      val action = actions[0]
      assertIs<HabitsAction.Cache.UpdateActivityData>(action)
      assertTrue(action.configTimeline.isNotEmpty(), "Should have config timeline")
    }

  @Test
  fun `when RecalculateActivityData with week range then uses correct range`() =
    runTest {
      val handler = createHandler()
      val weekRange = ActivityRange.Week(startDate = LocalDate(2026, 1, 27))
      val effect =
        HabitsEffect.RecalculateActivityData(
          range = weekRange,
          mode = ActivityMode.POSTS,
          appMode = AppMode.DEMO,
          memos = emptyList(),
        )

      val actions = handler(effect).toList()

      val action = actions[0]
      assertIs<HabitsAction.Cache.UpdateActivityData>(action)
      assertEquals(weekRange, action.range)
      assertEquals(AppMode.DEMO, action.appMode)
    }

  @Test
  fun `when RecalculateActivityData with quarter range then uses correct range`() =
    runTest {
      val handler = createHandler()
      val quarterRange = ActivityRange.Quarter(year = 2026, index = 0)
      val effect =
        HabitsEffect.RecalculateActivityData(
          range = quarterRange,
          mode = ActivityMode.HABITS,
          appMode = AppMode.OFFLINE,
          memos = emptyList(),
        )

      val actions = handler(effect).toList()

      val action = actions[0]
      assertIs<HabitsAction.Cache.UpdateActivityData>(action)
      assertEquals(quarterRange, action.range)
    }

  @Test
  fun `when RecalculateActivityData with year range then uses correct range`() =
    runTest {
      val handler = createHandler()
      val yearRange = ActivityRange.Year(year = 2026)
      val effect =
        HabitsEffect.RecalculateActivityData(
          range = yearRange,
          mode = ActivityMode.POSTS,
          appMode = AppMode.ONLINE,
          memos = emptyList(),
        )

      val actions = handler(effect).toList()

      val action = actions[0]
      assertIs<HabitsAction.Cache.UpdateActivityData>(action)
      assertEquals(yearRange, action.range)
    }

  @Test
  fun `when RecalculateActivityData for habits mode with daily check-ins then calculates week data`() =
    runTest {
      val handler = createHandler()
      val configMemo =
        createMemo(
          name = "memos/1",
          content = "#habits/config\nExercise | #habits/exercise",
          date = LocalDate(2026, 1, 1),
        )
      val dailyMemo =
        createMemo(
          name = "memos/2",
          content = "#habits/daily 2026-01-15\n\n#habits/exercise",
          date = LocalDate(2026, 1, 15),
        )
      val effect =
        HabitsEffect.RecalculateActivityData(
          range = ActivityRange.Month(year = 2026, month = Month.JANUARY),
          mode = ActivityMode.HABITS,
          appMode = AppMode.OFFLINE,
          memos = listOf(configMemo, dailyMemo),
        )

      val actions = handler(effect).toList()

      val action = actions[0]
      assertIs<HabitsAction.Cache.UpdateActivityData>(action)
      assertNotNull(action.weekData, "Should have week data")
      assertTrue(action.weekData.weeks.isNotEmpty(), "Week data should have weeks")
    }

  @Test
  fun `when RecalculateActivityData for posts mode then counts posts per day`() =
    runTest {
      val handler = createHandler()
      val postMemo =
        createMemo(
          name = "memos/1",
          content = "Regular post content",
          date = LocalDate(2026, 1, 15),
        )
      val effect =
        HabitsEffect.RecalculateActivityData(
          range = ActivityRange.Month(year = 2026, month = Month.JANUARY),
          mode = ActivityMode.POSTS,
          appMode = AppMode.OFFLINE,
          memos = listOf(postMemo),
        )

      val actions = handler(effect).toList()

      val action = actions[0]
      assertIs<HabitsAction.Cache.UpdateActivityData>(action)
      assertTrue(action.weekData.maxDaily >= 0, "Max daily should be calculated")
    }
}
