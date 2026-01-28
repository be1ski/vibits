package space.be1ski.vibits.shared.feature.habits.presentation

import kotlinx.datetime.LocalDate
import space.be1ski.vibits.shared.app.domain.model.ActivityMode
import space.be1ski.vibits.shared.app.domain.model.ActivityRange
import space.be1ski.vibits.shared.feature.habits.domain.model.ActivityWeekData
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HabitsStateExtensionsTest {
  @Test
  fun `isDataLoading returns true when key is in isRecalculating`() {
    val key = ActivityCacheKey(ActivityRange.Week(LocalDate(2026, 1, 20)), ActivityMode.HABITS, AppMode.ONLINE)
    val state = HabitsState(isRecalculating = setOf(key))

    assertTrue(state.isDataLoading(key))
  }

  @Test
  fun `isDataLoading returns true when isInitialLoading and key not in cache`() {
    val key = ActivityCacheKey(ActivityRange.Week(LocalDate(2026, 1, 20)), ActivityMode.HABITS, AppMode.ONLINE)
    val state = HabitsState(isInitialLoading = true, activityDataCache = emptyMap())

    assertTrue(state.isDataLoading(key))
  }

  @Test
  fun `isDataLoading returns false when isInitialLoading but key is in cache`() {
    val key = ActivityCacheKey(ActivityRange.Week(LocalDate(2026, 1, 20)), ActivityMode.HABITS, AppMode.ONLINE)
    val data = CachedActivityData(ActivityWeekData(emptyList(), 0, 0), emptyList(), null)
    val state = HabitsState(isInitialLoading = true, activityDataCache = mapOf(key to data))

    assertFalse(state.isDataLoading(key))
  }

  @Test
  fun `isDataLoading returns true when needsCacheRefresh and key not in cache`() {
    val key = ActivityCacheKey(ActivityRange.Week(LocalDate(2026, 1, 20)), ActivityMode.HABITS, AppMode.ONLINE)
    val state = HabitsState(needsCacheRefresh = true, activityDataCache = emptyMap())

    assertTrue(state.isDataLoading(key))
  }

  @Test
  fun `isDataLoading returns false when needsCacheRefresh but key is in cache`() {
    val key = ActivityCacheKey(ActivityRange.Week(LocalDate(2026, 1, 20)), ActivityMode.HABITS, AppMode.ONLINE)
    val data = CachedActivityData(ActivityWeekData(emptyList(), 0, 0), emptyList(), null)
    val state = HabitsState(needsCacheRefresh = true, activityDataCache = mapOf(key to data))

    assertFalse(state.isDataLoading(key))
  }

  @Test
  fun `isDataLoading returns false when not loading and key in cache`() {
    val key = ActivityCacheKey(ActivityRange.Week(LocalDate(2026, 1, 20)), ActivityMode.HABITS, AppMode.ONLINE)
    val data = CachedActivityData(ActivityWeekData(emptyList(), 0, 0), emptyList(), null)
    val state = HabitsState(isInitialLoading = false, activityDataCache = mapOf(key to data))

    assertFalse(state.isDataLoading(key))
  }

  @Test
  fun `getActivityData returns cached data when present`() {
    val range = ActivityRange.Week(LocalDate(2026, 1, 20))
    val mode = ActivityMode.HABITS
    val appMode = AppMode.ONLINE
    val key = ActivityCacheKey(range, mode, appMode)
    val data = CachedActivityData(ActivityWeekData(emptyList(), 0, 0), emptyList(), null)
    val state = HabitsState(activityDataCache = mapOf(key to data))

    val result = state.getActivityData(range, mode, appMode)

    assertNotNull(result)
    assertEquals(data, result)
  }

  @Test
  fun `getActivityData returns null when not cached`() {
    val state = HabitsState(activityDataCache = emptyMap())

    val result = state.getActivityData(ActivityRange.Week(LocalDate(2026, 1, 20)), ActivityMode.HABITS, AppMode.ONLINE)

    assertNull(result)
  }

  @Test
  fun `getActivityData returns null for different range`() {
    val range1 = ActivityRange.Week(LocalDate(2026, 1, 20))
    val range2 = ActivityRange.Week(LocalDate(2026, 1, 27))
    val key = ActivityCacheKey(range1, ActivityMode.HABITS, AppMode.ONLINE)
    val data = CachedActivityData(ActivityWeekData(emptyList(), 0, 0), emptyList(), null)
    val state = HabitsState(activityDataCache = mapOf(key to data))

    val result = state.getActivityData(range2, ActivityMode.HABITS, AppMode.ONLINE)

    assertNull(result)
  }

  @Test
  fun `getActivityData returns null for different mode`() {
    val range = ActivityRange.Week(LocalDate(2026, 1, 20))
    val key = ActivityCacheKey(range, ActivityMode.HABITS, AppMode.ONLINE)
    val data = CachedActivityData(ActivityWeekData(emptyList(), 0, 0), emptyList(), null)
    val state = HabitsState(activityDataCache = mapOf(key to data))

    val result = state.getActivityData(range, ActivityMode.POSTS, AppMode.ONLINE)

    assertNull(result)
  }

  @Test
  fun `getActivityData returns null for different app mode`() {
    val range = ActivityRange.Week(LocalDate(2026, 1, 20))
    val key = ActivityCacheKey(range, ActivityMode.HABITS, AppMode.ONLINE)
    val data = CachedActivityData(ActivityWeekData(emptyList(), 0, 0), emptyList(), null)
    val state = HabitsState(activityDataCache = mapOf(key to data))

    val result = state.getActivityData(range, ActivityMode.HABITS, AppMode.DEMO)

    assertNull(result)
  }
}
