package space.be1ski.vibits.feature.habits.domain.usecase

import kotlinx.datetime.LocalDate
import space.be1ski.vibits.feature.habits.domain.model.ActivityRange
import space.be1ski.vibits.feature.habits.domain.model.ActivitySummary
import space.be1ski.vibits.feature.habits.domain.model.SuccessRate

object CalculateSuccessRateUseCase {
  operator fun invoke(
    weekData: ActivitySummary,
    range: ActivityRange,
    today: LocalDate,
    configStartDate: LocalDate? = null,
  ): SuccessRate {
    val bounds = GetRangeBoundsUseCase(range)
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

    return SuccessRate(completed, total, rate)
  }
}
