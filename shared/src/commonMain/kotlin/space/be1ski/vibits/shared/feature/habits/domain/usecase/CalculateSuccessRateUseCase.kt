package space.be1ski.vibits.shared.feature.habits.domain.usecase

import dev.zacsweers.metro.Inject
import kotlinx.datetime.LocalDate
import space.be1ski.vibits.shared.app.domain.model.ActivityRange
import space.be1ski.vibits.shared.core.logging.Log
import space.be1ski.vibits.shared.feature.habits.domain.model.ActivityWeekData
import space.be1ski.vibits.shared.feature.habits.domain.model.SuccessRateData

private const val TAG = "SuccessRate"

/**
 * Calculates success rate for habits within a given time range.
 */
@Inject
class CalculateSuccessRateUseCase {
  operator fun invoke(
    weekData: ActivityWeekData,
    range: ActivityRange,
    today: LocalDate,
    configStartDate: LocalDate? = null,
  ): SuccessRateData {
    val bounds = GetRangeBoundsUseCase(range)
    val effectiveStart =
      if (configStartDate != null && configStartDate > bounds.start) {
        configStartDate
      } else {
        bounds.start
      }
    val effectiveEnd = if (today in bounds.start..bounds.end) today else bounds.end

    Log.d(
      TAG,
      "INPUT: range=$range today=$today configStartDate=$configStartDate bounds=$bounds " +
        "effectiveStart=$effectiveStart effectiveEnd=$effectiveEnd " +
        "weekDataWeeks=${weekData.weeks.size} weekDataMaxDaily=${weekData.maxDaily}",
    )

    val allDays = weekData.weeks.flatMap { it.days }

    @Suppress("MagicNumber")
    val sampleSize = 5
    Log.d(
      TAG,
      "ALL DAYS: count=${allDays.size} " +
        "daysWithHabits=${allDays.count { it.totalHabits > 0 }} " +
        "sample=${allDays.take(sampleSize).map { "${it.date}:${it.count}/${it.totalHabits}" }}",
    )

    val days =
      weekData.weeks
        .flatMap { it.days }
        .filter { it.date in effectiveStart..effectiveEnd && it.totalHabits > 0 }

    val completed = days.sumOf { it.count }
    val total = days.sumOf { it.totalHabits }
    val rate = if (total > 0) completed.toFloat() / total else 0f

    Log.d(
      TAG,
      "RESULT: daysCount=${days.size} completed=$completed total=$total rate=$rate " +
        "days=[${days.joinToString { "${it.date}:${it.count}/${it.totalHabits}" }}]",
    )

    return SuccessRateData(completed, total, rate)
  }
}
