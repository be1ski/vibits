package space.be1ski.vibits.shared.feature.habits.domain.usecase

import kotlinx.datetime.Month
import space.be1ski.vibits.shared.app.domain.model.ActivityRange

/**
 * Checks if one activity range is before another.
 */
object IsActivityRangeBeforeUseCase {
  operator fun invoke(
    range: ActivityRange,
    other: ActivityRange,
  ): Boolean =
    when (range) {
      is ActivityRange.Week -> other is ActivityRange.Week && range.startDate < other.startDate
      is ActivityRange.Month ->
        other is ActivityRange.Month &&
          compareYearMonth(range.year, range.month, other.year, other.month) < 0
      is ActivityRange.Quarter ->
        other is ActivityRange.Quarter &&
          compareYearQuarter(range.year, range.index, other.year, other.index) < 0
      is ActivityRange.Year -> other is ActivityRange.Year && range.year < other.year
    }

  private fun compareYearMonth(
    year: Int,
    month: Month,
    otherYear: Int,
    otherMonth: Month,
  ): Int = if (year != otherYear) year - otherYear else month.ordinal - otherMonth.ordinal

  private fun compareYearQuarter(
    year: Int,
    quarter: Int,
    otherYear: Int,
    otherQuarter: Int,
  ): Int = if (year != otherYear) year - otherYear else quarter - otherQuarter
}
