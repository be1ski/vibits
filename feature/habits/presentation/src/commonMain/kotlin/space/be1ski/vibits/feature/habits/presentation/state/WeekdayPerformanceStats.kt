package space.be1ski.vibits.feature.habits.presentation.state

import kotlinx.datetime.DayOfWeek

internal data class WeekdayPerformanceStats(
  val dayOfWeek: DayOfWeek,
  val completionRate: Float?, // null = no observations; 0.0–1.0 otherwise
  val isBest: Boolean,
  val isWorst: Boolean,
) {
  init {
    require(!isBest || !isWorst) {
      "A single entry cannot have both isBest=true and isWorst=true"
    }
  }
}

internal data class WeekdayPerformanceCardState(
  val stats: List<WeekdayPerformanceStats>,
  val hasSufficientData: Boolean,
  val averageCompletionRate: Float?, // null when the avg line is hidden
)
