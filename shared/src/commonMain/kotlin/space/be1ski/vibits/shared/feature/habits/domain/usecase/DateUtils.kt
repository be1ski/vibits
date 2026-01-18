package space.be1ski.vibits.shared.feature.habits.domain.usecase

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.minus

private const val MONTHS_IN_QUARTER = 3
private const val FIRST_QUARTER_INDEX = 1

/**
 * Returns the Monday of the week containing [date].
 */
fun startOfWeek(date: LocalDate): LocalDate {
  var start = date
  while (start.dayOfWeek != DayOfWeek.MONDAY) {
    start = start.minus(DatePeriod(days = 1))
  }
  return start
}

/**
 * Returns the quarter index (1-4) for a date.
 */
fun quarterIndex(date: LocalDate): Int = quarterIndex(date.month)

/**
 * Returns the quarter index (1-4) for a month.
 */
fun quarterIndex(month: Month): Int = month.ordinal / MONTHS_IN_QUARTER + FIRST_QUARTER_INDEX
