package space.be1ski.vibits.feature.main.domain.usecase

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import space.be1ski.vibits.feature.habits.domain.model.ActivityRange
import space.be1ski.vibits.feature.main.domain.model.FIRST_DAY_OF_MONTH
import space.be1ski.vibits.feature.main.domain.model.MONTHS_PER_QUARTER

/**
 * Returns the first day of the given activity range.
 */
object GetActivityRangeStartDateUseCase {
  operator fun invoke(range: ActivityRange): LocalDate =
    when (range) {
      is ActivityRange.Week -> range.startDate
      is ActivityRange.Month -> LocalDate(range.year, range.month, FIRST_DAY_OF_MONTH)
      is ActivityRange.Quarter -> {
        val month = Month((range.index - 1) * MONTHS_PER_QUARTER + 1)
        LocalDate(range.year, month, FIRST_DAY_OF_MONTH)
      }
      is ActivityRange.Year -> LocalDate(range.year, Month.JANUARY, FIRST_DAY_OF_MONTH)
    }
}
