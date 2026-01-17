package space.be1ski.vibits.shared.feature.habits.domain.usecase

import kotlinx.datetime.LocalDate
import space.be1ski.vibits.shared.core.ui.ActivityRange
import space.be1ski.vibits.shared.feature.habits.domain.model.ActivityWeekData
import space.be1ski.vibits.shared.feature.habits.domain.model.SuccessRateData
import space.be1ski.vibits.shared.feature.habits.domain.model.rangeBounds

/**
 * Calculates success rate for habits within a given time range.
 */
class CalculateSuccessRateUseCase {
  operator fun invoke(
    weekData: ActivityWeekData,
    range: ActivityRange,
    today: LocalDate,
    configStartDate: LocalDate? = null,
  ): SuccessRateData {
    val bounds = rangeBounds(range)
    val effectiveStart =
      if (configStartDate != null && configStartDate > bounds.start) {
        configStartDate
      } else {
        bounds.start
      }
    val effectiveEnd = if (today in bounds.start..bounds.end) today else bounds.end

    val days =
      weekData.weeks
        .flatMap { it.days }
        .filter { it.date in effectiveStart..effectiveEnd && it.totalHabits > 0 }

    val completed = days.sumOf { it.count }
    val total = days.sumOf { it.totalHabits }
    val rate = if (total > 0) completed.toFloat() / total else 0f

    return SuccessRateData(completed, total, rate)
  }
}
