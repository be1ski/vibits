package space.be1ski.vibits.shared.feature.habits.presentation.state

import space.be1ski.vibits.shared.app.domain.model.ActivityMode
import space.be1ski.vibits.shared.app.domain.model.ActivityRange
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode

/**
 * Key for caching activity data.
 * Includes AppMode to prevent mixing Demo/Offline/Online data.
 */
data class ActivityCacheKey(
  val range: ActivityRange,
  val mode: ActivityMode,
  val appMode: AppMode,
)
