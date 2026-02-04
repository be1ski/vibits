package space.be1ski.vibits.feature.habits.domain.model

import space.be1ski.vibits.core.platform.mode.AppMode

/**
 * Result from prewarming activity data for a specific range and mode.
 */
data class PrewarmResult(
  val range: ActivityRange,
  val mode: ActivityMode,
  val appMode: AppMode,
  val weekData: ActivityWeekData,
  val configTimeline: List<HabitsConfigEntry>,
  val successRate: SuccessRateData?,
)
