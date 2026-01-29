package space.be1ski.vibits.shared.feature.habits.presentation.state

import space.be1ski.vibits.shared.feature.habits.domain.model.ActivityWeekData
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitsConfigEntry
import space.be1ski.vibits.shared.feature.habits.domain.model.SuccessRateData

/**
 * Cached activity data for a specific range and mode.
 */
data class CachedActivityData(
  val weekData: ActivityWeekData,
  val configTimeline: List<HabitsConfigEntry>,
  val successRate: SuccessRateData?,
)
