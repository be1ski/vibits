package space.be1ski.vibits.feature.habits.presentation
import kotlinx.datetime.LocalDate
import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.feature.habits.domain.model.ActivityMode
import space.be1ski.vibits.feature.habits.domain.model.ActivityRange
import space.be1ski.vibits.feature.habits.domain.model.ActivitySummary
import space.be1ski.vibits.feature.habits.domain.model.ActivityWeek
import space.be1ski.vibits.feature.habits.domain.model.CachedActivity
import space.be1ski.vibits.feature.habits.presentation.action.HabitsAction
import space.be1ski.vibits.feature.habits.presentation.effect.HabitsEffect
import space.be1ski.vibits.feature.habits.presentation.reducer.habitsReducer
import space.be1ski.vibits.feature.habits.presentation.state.ActivityCacheKey
import space.be1ski.vibits.feature.habits.presentation.state.HabitsState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests for TEA cache functionality (TC-01..TC-10).
 */
class HabitsCacheTest {
  private val reducer = habitsReducer

  @Test
  fun `TC-10 switching app mode should invalidate cache and trigger prewarm`() {
    // Given: Demo mode with cached data
    val week1Range = ActivityRange.Week(LocalDate(2026, 1, 20))
    val demoKey = ActivityCacheKey(week1Range, ActivityMode.HABITS, AppMode.DEMO)
    val demoData =
      CachedActivity(
        weekData =
          ActivitySummary(
            weeks = listOf(ActivityWeek(startDate = week1Range.startDate, days = emptyList(), weeklyCount = 0)),
            maxDaily = 0,
            maxWeekly = 0,
          ),
        configTimeline = emptyList(),
        successRate = null,
      )
    val initialState =
      HabitsState(
        activityDataCache = mapOf(demoKey to demoData),
        isInitialLoading = false,
      )

    // When: InvalidateAllCache action dispatched (app shell does this on mode switch)
    val (newState, _) = reducer.invoke(HabitsAction.Cache.InvalidateAllCache, initialState)

    // Then: cache should be cleared and refresh flagged
    assertTrue(newState.activityDataCache.isEmpty(), "Cache should be cleared on mode switch")
    assertTrue(newState.needsCacheRefresh, "needsCacheRefresh should be true")
  }

  @Test
  fun `TC-03 switching weeks should not clear existing cache`() {
    // Given: week 1 cached
    val appMode = AppMode.DEMO
    val week1Range = ActivityRange.Week(LocalDate(2026, 1, 13))
    val week2Range = ActivityRange.Week(LocalDate(2026, 1, 20))
    val week1Key = ActivityCacheKey(week1Range, ActivityMode.HABITS, appMode)
    val cachedData1 =
      CachedActivity(
        weekData = ActivitySummary(weeks = emptyList(), maxDaily = 0, maxWeekly = 0),
        configTimeline = emptyList(),
        successRate = null,
      )
    val initialState = HabitsState(activityDataCache = mapOf(week1Key to cachedData1))

    // When: manually invalidate for week 2
    val memos = emptyList<space.be1ski.vibits.feature.memos.domain.model.Memo>()
    val (newState, _) =
      reducer.invoke(
        HabitsAction.Cache.InvalidateCache(
          range = week2Range,
          mode = ActivityMode.HABITS,
          appMode = appMode,
          memos = memos,
        ),
        initialState,
      )

    // Then: week 1 data should still exist
    assertNotNull(newState.activityDataCache[week1Key], "Week 1 cache should not be cleared")
    val week2Key = ActivityCacheKey(week2Range, ActivityMode.HABITS, appMode)
    assertTrue(week2Key in newState.isRecalculating, "Week 2 should be marked as recalculating")
  }

  @Test
  fun `TC-06 memo operations should close editor and trigger refresh`() {
    val initialState = HabitsState(needsCacheRefresh = false, isLoading = true)
    val testMemo =
      space.be1ski.vibits.feature.memos.domain.model.Memo(
        name = "memos/1",
        content = "test",
      )

    val actions =
      listOf(
        HabitsAction.Response.MemoCreated(testMemo),
        HabitsAction.Response.MemoUpdated(testMemo),
        HabitsAction.Response.MemoDeleted("test.md"),
      )

    actions.forEach { action ->
      val (newState, effects) = reducer.invoke(action, initialState)
      assertEquals(false, newState.isLoading, "Action $action should stop loading")
      assertTrue(effects.commands.any { it is HabitsEffect.RefreshMemos }, "Action $action should trigger RefreshMemos")
    }
  }

  @Test
  fun `TC-07 memo update should not clear cache`() {
    // Given: cache with data
    val appMode = AppMode.DEMO
    val week1Range = ActivityRange.Week(LocalDate(2026, 1, 13))
    val week2Range = ActivityRange.Week(LocalDate(2026, 1, 20))
    val week1Key = ActivityCacheKey(week1Range, ActivityMode.HABITS, appMode)
    val week2Key = ActivityCacheKey(week2Range, ActivityMode.HABITS, appMode)
    val cachedData =
      CachedActivity(
        weekData = ActivitySummary(weeks = emptyList(), maxDaily = 0, maxWeekly = 0),
        configTimeline = emptyList(),
        successRate = null,
      )
    val initialState =
      HabitsState(
        activityDataCache = mapOf(week1Key to cachedData, week2Key to cachedData),
      )

    // When: memo updated
    val testMemo =
      space.be1ski.vibits.feature.memos.domain.model.Memo(
        name = "memos/1",
        content = "test",
      )
    val (newState, effects) = reducer.invoke(HabitsAction.Response.MemoUpdated(testMemo), initialState)

    // Then: cache should not be cleared
    assertEquals(2, newState.activityDataCache.size, "Cache size should remain unchanged")
    assertNotNull(newState.activityDataCache[week1Key], "Week 1 cache should exist")
    assertNotNull(newState.activityDataCache[week2Key], "Week 2 cache should exist")
    assertTrue(effects.commands.any { it is HabitsEffect.RefreshMemos }, "Should trigger memos refresh")
  }
}
