package space.be1ski.vibits.feature.habits.domain.model

/**
 * Cached activity data for a specific range and mode.
 */
data class CachedActivityData(
  val weekData: ActivityWeekData,
  val configTimeline: List<HabitsConfigEntry>,
  val successRate: SuccessRateData?,
)
