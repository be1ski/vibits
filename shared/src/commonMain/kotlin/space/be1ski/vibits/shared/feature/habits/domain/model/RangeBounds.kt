package space.be1ski.vibits.shared.feature.habits.domain.model

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import space.be1ski.vibits.shared.core.ui.ActivityRange

private const val WEEK_END_OFFSET = 6
private const val MONTHS_IN_QUARTER = 3
private const val FIRST_QUARTER_INDEX = 1
private const val QUARTERS_IN_YEAR = 4
private const val YEAR_START_DAY = 1

/**
 * Date range bounds.
 */
data class RangeBounds(
  val start: LocalDate,
  val end: LocalDate,
)

/**
 * Calculate date bounds for an activity range.
 */
fun rangeBounds(range: ActivityRange): RangeBounds =
  when (range) {
    is ActivityRange.Week ->
      RangeBounds(
        start = range.startDate,
        end = range.startDate.plus(DatePeriod(days = WEEK_END_OFFSET)),
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
        start = LocalDate(range.year, Month.JANUARY, YEAR_START_DAY),
        end =
          LocalDate(range.year + 1, Month.JANUARY, YEAR_START_DAY)
            .minus(DatePeriod(days = 1)),
      )
  }

private fun quarterStartMonth(index: Int): Month {
  val safeIndex = index.coerceIn(FIRST_QUARTER_INDEX, QUARTERS_IN_YEAR)
  val monthIndex = (safeIndex - FIRST_QUARTER_INDEX) * MONTHS_IN_QUARTER
  return Month.entries[monthIndex]
}
