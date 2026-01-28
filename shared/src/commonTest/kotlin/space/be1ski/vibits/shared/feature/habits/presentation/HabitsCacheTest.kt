package space.be1ski.vibits.shared.feature.habits.presentation

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import space.be1ski.vibits.shared.app.domain.model.ActivityMode
import space.be1ski.vibits.shared.app.domain.model.ActivityRange
import space.be1ski.vibits.shared.feature.habits.domain.model.ActivityWeek
import space.be1ski.vibits.shared.feature.habits.domain.model.ActivityWeekData
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
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
      CachedActivityData(
        weekData =
          ActivityWeekData(
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
    val (newState, _) = reducer.invoke(HabitsAction.InvalidateAllCache, initialState)

    // Then: cache should be cleared and refresh flagged
    assertTrue(newState.activityDataCache.isEmpty(), "Cache should be cleared on mode switch")
    assertTrue(newState.needsCacheRefresh, "needsCacheRefresh should be true")
    assertTrue(newState.isInitialLoading, "isInitialLoading should be true")
  }

  @Test
  fun `TC-03 switching weeks should not clear existing cache`() {
    // Given: week 1 cached
    val appMode = AppMode.DEMO
    val week1Range = ActivityRange.Week(LocalDate(2026, 1, 13))
    val week2Range = ActivityRange.Week(LocalDate(2026, 1, 20))
    val week1Key = ActivityCacheKey(week1Range, ActivityMode.HABITS, appMode)
    val cachedData1 =
      CachedActivityData(
        weekData = ActivityWeekData(weeks = emptyList(), maxDaily = 0, maxWeekly = 0),
        configTimeline = emptyList(),
        successRate = null,
      )
    val initialState = HabitsState(activityDataCache = mapOf(week1Key to cachedData1))

    // When: manually invalidate for week 2
    val memos = emptyList<space.be1ski.vibits.shared.feature.memos.domain.model.Memo>()
    val (newState, effects) =
      reducer.invoke(
        HabitsAction.InvalidateCache(
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
  fun `TC-06 memo operations should set needsCacheRefresh flag`() {
    val initialState = HabitsState(needsCacheRefresh = false)
    val testMemo =
      space.be1ski.vibits.shared.feature.memos.domain.model.Memo(
        name = "memos/1",
        content = "test",
      )

    val actions =
      listOf(
        HabitsAction.MemoCreated(testMemo),
        HabitsAction.MemoUpdated(testMemo),
        HabitsAction.MemoDeleted("test.md"),
      )

    actions.forEach { action ->
      val (newState, _) = reducer.invoke(action, initialState)
      // Note: This test will fail until we implement needsCacheRefresh in reducer
      // Currently MemoCreated/Updated/Deleted don't set this flag
      assertTrue(newState.needsCacheRefresh, "Action $action should set needsCacheRefresh")
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
      CachedActivityData(
        weekData = ActivityWeekData(weeks = emptyList(), maxDaily = 0, maxWeekly = 0),
        configTimeline = emptyList(),
        successRate = null,
      )
    val initialState =
      HabitsState(
        activityDataCache = mapOf(week1Key to cachedData, week2Key to cachedData),
      )

    // When: memo updated
    val testMemo =
      space.be1ski.vibits.shared.feature.memos.domain.model.Memo(
        name = "memos/1",
        content = "test",
      )
    val (newState, _) = reducer.invoke(HabitsAction.MemoUpdated(testMemo), initialState)

    // Then: cache should not be cleared
    assertEquals(2, newState.activityDataCache.size, "Cache size should remain unchanged")
    assertNotNull(newState.activityDataCache[week1Key], "Week 1 cache should exist")
    assertNotNull(newState.activityDataCache[week2Key], "Week 2 cache should exist")
    assertTrue(newState.needsCacheRefresh, "needsCacheRefresh should be true")
  }
}
