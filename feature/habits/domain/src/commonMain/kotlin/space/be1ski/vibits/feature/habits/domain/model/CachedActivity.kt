package space.be1ski.vibits.feature.habits.domain.model

/**
 * Cached activity data for a specific range and mode.
 */
data class CachedActivity(
  val weekData: ActivitySummary,
  val configTimeline: List<HabitsConfigEntry>,
  val successRate: SuccessRate?,
)
