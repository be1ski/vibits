package space.be1ski.vibits.feature.habits.domain.usecase

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import space.be1ski.vibits.core.date.DAYS_IN_WEEK
import space.be1ski.vibits.core.date.FIRST_DAY_OF_MONTH
import space.be1ski.vibits.core.date.FIRST_QUARTER_INDEX
import space.be1ski.vibits.core.date.QUARTERS_IN_YEAR
import space.be1ski.vibits.core.date.quarterIndex
import space.be1ski.vibits.core.date.startOfWeek
import space.be1ski.vibits.feature.habits.domain.model.ActivityRange

object GenerateActivityRangesUseCase {
  operator fun invoke(
    startDate: LocalDate,
    endDate: LocalDate,
  ): List<ActivityRange> =
    buildList {
      addAll(generateWeeks(startDate, endDate))
      addAll(generateMonths(startDate, endDate))
      addAll(generateQuarters(startDate, endDate))
      addAll(generateYears(startDate, endDate))
    }

  private fun generateWeeks(
    startDate: LocalDate,
    endDate: LocalDate,
  ): List<ActivityRange.Week> {
    val weeks = mutableListOf<ActivityRange.Week>()
    var cursor = startOfWeek(startDate)
    while (cursor <= endDate) {
      weeks.add(ActivityRange.Week(cursor))
      cursor = cursor.plus(DatePeriod(days = DAYS_IN_WEEK))
    }
    return weeks
  }

  private fun generateMonths(
    startDate: LocalDate,
    endDate: LocalDate,
  ): List<ActivityRange.Month> {
    val months = mutableListOf<ActivityRange.Month>()
    var cursor = ActivityRange.Month(startDate.year, startDate.month)
    val end = ActivityRange.Month(endDate.year, endDate.month)
    while (cursor.year < end.year || (cursor.year == end.year && cursor.month <= end.month)) {
      months.add(cursor)
      val nextDate = LocalDate(cursor.year, cursor.month, FIRST_DAY_OF_MONTH).plus(DatePeriod(months = 1))
      cursor = ActivityRange.Month(nextDate.year, nextDate.month)
    }
    return months
  }

  private fun generateQuarters(
    startDate: LocalDate,
    endDate: LocalDate,
  ): List<ActivityRange.Quarter> {
    val quarters = mutableListOf<ActivityRange.Quarter>()
    val startQuarter = quarterIndex(startDate)
    val endQuarter = quarterIndex(endDate)
    var yearCursor = startDate.year
    var quarterCursor = startQuarter
    while (yearCursor < endDate.year || (yearCursor == endDate.year && quarterCursor <= endQuarter)) {
      quarters.add(ActivityRange.Quarter(yearCursor, quarterCursor))
      quarterCursor++
      if (quarterCursor > QUARTERS_IN_YEAR) {
        quarterCursor = FIRST_QUARTER_INDEX
        yearCursor++
      }
    }
    return quarters
  }

  private fun generateYears(
    startDate: LocalDate,
    endDate: LocalDate,
  ): List<ActivityRange.Year> = (startDate.year..endDate.year).map { ActivityRange.Year(it) }
}
