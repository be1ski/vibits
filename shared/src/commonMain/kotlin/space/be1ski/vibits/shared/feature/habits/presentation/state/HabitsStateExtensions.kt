package space.be1ski.vibits.shared.feature.habits.presentation.state
import space.be1ski.vibits.shared.app.domain.model.ActivityMode
import space.be1ski.vibits.shared.app.domain.model.ActivityRange
import space.be1ski.vibits.shared.feature.habits.presentation.state.HabitsState
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode

/**
 * Checks if data for the given key is currently being loaded or recalculated.
 */
fun HabitsState.isDataLoading(key: ActivityCacheKey): Boolean =
  key in isRecalculating || ((isInitialLoading || needsCacheRefresh) && key !in activityDataCache)

/**
 * Gets cached activity data for the given range, mode, and app mode.
 */
fun HabitsState.getActivityData(
  range: ActivityRange,
  mode: ActivityMode,
  appMode: AppMode,
): CachedActivityData? = activityDataCache[ActivityCacheKey(range, mode, appMode)]
