package space.be1ski.vibits.feature.habits.presentation.state

import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.feature.habits.domain.model.ActivityMode
import space.be1ski.vibits.feature.habits.domain.model.ActivityRange
import space.be1ski.vibits.feature.habits.domain.model.CachedActivity

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
): CachedActivity? = activityDataCache[ActivityCacheKey(range, mode, appMode)]
