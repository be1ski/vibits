package space.be1ski.vibits.feature.habits.domain.model

/**
 * Success rate calculation result.
 */
data class SuccessRate(
  val completed: Int,
  val total: Int,
  val rate: Float,
)
