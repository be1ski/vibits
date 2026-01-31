package space.be1ski.vibits.feature.habits.presentation.state

import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.feature.habits.domain.model.ActivityMode
import space.be1ski.vibits.feature.habits.domain.model.ActivityRange

/**
 * Key for caching activity data.
 * Includes AppMode to prevent mixing Demo/Offline/Online data.
 */
data class ActivityCacheKey(
  val range: ActivityRange,
  val mode: ActivityMode,
  val appMode: AppMode,
)
