package space.be1ski.vibits.core.date

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.minus

fun startOfWeek(date: LocalDate): LocalDate {
  var start = date
  while (start.dayOfWeek != DayOfWeek.MONDAY) {
    start = start.minus(DatePeriod(days = 1))
  }
  return start
}

fun quarterIndex(date: LocalDate): Int = quarterIndex(date.month)

fun quarterIndex(month: Month): Int = month.ordinal / MONTHS_IN_QUARTER + FIRST_QUARTER_INDEX
