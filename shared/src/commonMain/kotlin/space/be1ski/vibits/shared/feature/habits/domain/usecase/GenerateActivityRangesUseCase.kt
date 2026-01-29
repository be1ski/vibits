package space.be1ski.vibits.shared.feature.habits.domain.usecase

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import space.be1ski.vibits.shared.app.domain.model.ActivityRange

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
      cursor = cursor.plus(DatePeriod(days = 7))
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
      val nextDate = LocalDate(cursor.year, cursor.month, 1).plus(DatePeriod(months = 1))
      cursor = ActivityRange.Month(nextDate.year, nextDate.month)
    }
    return months
  }

  @Suppress("MagicNumber")
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
      if (quarterCursor > 4) {
        quarterCursor = 1
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
