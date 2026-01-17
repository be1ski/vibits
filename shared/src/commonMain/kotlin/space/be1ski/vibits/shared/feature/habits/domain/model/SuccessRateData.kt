package space.be1ski.vibits.shared.feature.habits.domain.model

/**
 * Success rate calculation result.
 */
data class SuccessRateData(
  val completed: Int,
  val total: Int,
  val rate: Float,
)
