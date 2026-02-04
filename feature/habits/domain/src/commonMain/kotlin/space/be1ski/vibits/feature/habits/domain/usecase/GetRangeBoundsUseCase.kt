package space.be1ski.vibits.feature.habits.domain.usecase

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import space.be1ski.vibits.core.date.DAYS_IN_WEEK
import space.be1ski.vibits.core.date.FIRST_DAY_OF_MONTH
import space.be1ski.vibits.core.date.FIRST_QUARTER_INDEX
import space.be1ski.vibits.core.date.MONTHS_IN_QUARTER
import space.be1ski.vibits.core.date.QUARTERS_IN_YEAR
import space.be1ski.vibits.feature.habits.domain.model.ActivityRange
import space.be1ski.vibits.feature.habits.domain.model.RangeBounds

/**
 * Calculates date bounds for an activity range.
 */
object GetRangeBoundsUseCase {
  operator fun invoke(range: ActivityRange): RangeBounds =
    when (range) {
      is ActivityRange.Week ->
        RangeBounds(
          start = range.startDate,
          end = range.startDate.plus(DatePeriod(days = DAYS_IN_WEEK - 1)),
        )
      is ActivityRange.Month ->
        RangeBounds(
          start = LocalDate(range.year, range.month, 1),
          end =
            LocalDate(range.year, range.month, 1)
              .plus(DatePeriod(months = 1))
              .minus(DatePeriod(days = 1)),
        )
      is ActivityRange.Quarter -> {
        val quarterStartMonth = quarterStartMonth(range.index)
        RangeBounds(
          start = LocalDate(range.year, quarterStartMonth, 1),
          end =
            LocalDate(range.year, quarterStartMonth, 1)
              .plus(DatePeriod(months = MONTHS_IN_QUARTER))
              .minus(DatePeriod(days = 1)),
        )
      }
      is ActivityRange.Year ->
        RangeBounds(
          start = LocalDate(range.year, Month.JANUARY, FIRST_DAY_OF_MONTH),
          end =
            LocalDate(range.year + 1, Month.JANUARY, FIRST_DAY_OF_MONTH)
              .minus(DatePeriod(days = 1)),
        )
    }

  private fun quarterStartMonth(index: Int): Month {
    val safeIndex = index.coerceIn(FIRST_QUARTER_INDEX, QUARTERS_IN_YEAR)
    val monthIndex = (safeIndex - FIRST_QUARTER_INDEX) * MONTHS_IN_QUARTER
    return Month.entries[monthIndex]
  }
}
