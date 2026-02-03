package space.be1ski.vibits.feature.habits.presentation
import kotlinx.datetime.LocalDate
import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.feature.habits.domain.model.ActivityMode
import space.be1ski.vibits.feature.habits.domain.model.ActivityRange
import space.be1ski.vibits.feature.habits.domain.model.ActivityWeekData
import space.be1ski.vibits.feature.habits.presentation.state.ActivityCacheKey
import space.be1ski.vibits.feature.habits.presentation.state.CachedActivityData
import space.be1ski.vibits.feature.habits.presentation.state.HabitsState
import space.be1ski.vibits.feature.habits.presentation.state.getActivityData
import space.be1ski.vibits.feature.habits.presentation.state.isDataLoading
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HabitsStateExtensionsTest {
  @Test
  fun `when key is in isRecalculating then isDataLoading returns true`() {
    val key = ActivityCacheKey(ActivityRange.Week(LocalDate(2026, 1, 20)), ActivityMode.HABITS, AppMode.ONLINE)
    val state = HabitsState(isRecalculating = setOf(key))

    assertTrue(state.isDataLoading(key))
  }

  @Test
  fun `when isInitialLoading and key not in cache then isDataLoading returns true`() {
    val key = ActivityCacheKey(ActivityRange.Week(LocalDate(2026, 1, 20)), ActivityMode.HABITS, AppMode.ONLINE)
    val state = HabitsState(isInitialLoading = true, activityDataCache = emptyMap())

    assertTrue(state.isDataLoading(key))
  }

  @Test
  fun `when isInitialLoading but key is in cache then isDataLoading returns false`() {
    val key = ActivityCacheKey(ActivityRange.Week(LocalDate(2026, 1, 20)), ActivityMode.HABITS, AppMode.ONLINE)
    val data = CachedActivityData(ActivityWeekData(emptyList(), 0, 0), emptyList(), null)
    val state = HabitsState(isInitialLoading = true, activityDataCache = mapOf(key to data))

    assertFalse(state.isDataLoading(key))
  }

  @Test
  fun `when needsCacheRefresh and key not in cache then isDataLoading returns true`() {
    val key = ActivityCacheKey(ActivityRange.Week(LocalDate(2026, 1, 20)), ActivityMode.HABITS, AppMode.ONLINE)
    val state = HabitsState(needsCacheRefresh = true, activityDataCache = emptyMap())

    assertTrue(state.isDataLoading(key))
  }

  @Test
  fun `when needsCacheRefresh but key is in cache then isDataLoading returns false`() {
    val key = ActivityCacheKey(ActivityRange.Week(LocalDate(2026, 1, 20)), ActivityMode.HABITS, AppMode.ONLINE)
    val data = CachedActivityData(ActivityWeekData(emptyList(), 0, 0), emptyList(), null)
    val state = HabitsState(needsCacheRefresh = true, activityDataCache = mapOf(key to data))

    assertFalse(state.isDataLoading(key))
  }

  @Test
  fun `when not loading and key in cache then isDataLoading returns false`() {
    val key = ActivityCacheKey(ActivityRange.Week(LocalDate(2026, 1, 20)), ActivityMode.HABITS, AppMode.ONLINE)
    val data = CachedActivityData(ActivityWeekData(emptyList(), 0, 0), emptyList(), null)
    val state = HabitsState(isInitialLoading = false, activityDataCache = mapOf(key to data))

    assertFalse(state.isDataLoading(key))
  }

  @Test
  fun `when cached data present then getActivityData returns it`() {
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
  fun `when not cached then getActivityData returns null`() {
    val state = HabitsState(activityDataCache = emptyMap())

    val result = state.getActivityData(ActivityRange.Week(LocalDate(2026, 1, 20)), ActivityMode.HABITS, AppMode.ONLINE)

    assertNull(result)
  }

  @Test
  fun `when different range then getActivityData returns null`() {
    val range1 = ActivityRange.Week(LocalDate(2026, 1, 20))
    val range2 = ActivityRange.Week(LocalDate(2026, 1, 27))
    val key = ActivityCacheKey(range1, ActivityMode.HABITS, AppMode.ONLINE)
    val data = CachedActivityData(ActivityWeekData(emptyList(), 0, 0), emptyList(), null)
    val state = HabitsState(activityDataCache = mapOf(key to data))

    val result = state.getActivityData(range2, ActivityMode.HABITS, AppMode.ONLINE)

    assertNull(result)
  }

  @Test
  fun `when different mode then getActivityData returns null`() {
    val range = ActivityRange.Week(LocalDate(2026, 1, 20))
    val key = ActivityCacheKey(range, ActivityMode.HABITS, AppMode.ONLINE)
    val data = CachedActivityData(ActivityWeekData(emptyList(), 0, 0), emptyList(), null)
    val state = HabitsState(activityDataCache = mapOf(key to data))

    val result = state.getActivityData(range, ActivityMode.POSTS, AppMode.ONLINE)

    assertNull(result)
  }

  @Test
  fun `when different app mode then getActivityData returns null`() {
    val range = ActivityRange.Week(LocalDate(2026, 1, 20))
    val key = ActivityCacheKey(range, ActivityMode.HABITS, AppMode.ONLINE)
    val data = CachedActivityData(ActivityWeekData(emptyList(), 0, 0), emptyList(), null)
    val state = HabitsState(activityDataCache = mapOf(key to data))

    val result = state.getActivityData(range, ActivityMode.HABITS, AppMode.DEMO)

    assertNull(result)
  }
}
