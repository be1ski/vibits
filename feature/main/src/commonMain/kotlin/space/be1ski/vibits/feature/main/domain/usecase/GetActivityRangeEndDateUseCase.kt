package space.be1ski.vibits.feature.main.domain.usecase

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import space.be1ski.vibits.core.date.DAYS_IN_WEEK
import space.be1ski.vibits.core.date.FIRST_DAY_OF_MONTH
import space.be1ski.vibits.core.date.MONTHS_IN_QUARTER
import space.be1ski.vibits.core.date.MONTHS_IN_YEAR
import space.be1ski.vibits.feature.habits.domain.model.ActivityRange

/**
 * Returns the last day of the given activity range.
 */
object GetActivityRangeEndDateUseCase {
  operator fun invoke(range: ActivityRange): LocalDate =
    when (range) {
      is ActivityRange.Week -> range.startDate.plus(DatePeriod(days = DAYS_IN_WEEK - 1))
      is ActivityRange.Month -> {
        val nextMonth = LocalDate(range.year, range.month, FIRST_DAY_OF_MONTH).plus(DatePeriod(months = 1))
        nextMonth.minus(DatePeriod(days = 1))
      }
      is ActivityRange.Quarter -> {
        val lastMonthOfQuarter = range.index * MONTHS_IN_QUARTER
        val firstOfNextQuarter =
          if (lastMonthOfQuarter == MONTHS_IN_YEAR) {
            LocalDate(range.year + 1, Month.JANUARY, FIRST_DAY_OF_MONTH)
          } else {
            LocalDate(range.year, Month(lastMonthOfQuarter + 1), FIRST_DAY_OF_MONTH)
          }
        firstOfNextQuarter.minus(DatePeriod(days = 1))
      }
      is ActivityRange.Year -> {
        val firstOfNextYear = LocalDate(range.year + 1, Month.JANUARY, FIRST_DAY_OF_MONTH)
        firstOfNextYear.minus(DatePeriod(days = 1))
      }
    }
}
