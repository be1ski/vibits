package space.be1ski.memos.shared.presentation.components

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

internal fun buildTimelineLabels(weeks: List<ActivityWeek>, range: ActivityRange): List<String> {
  if (weeks.isEmpty()) {
    return emptyList()
  }
  return weeks.mapIndexed { index, week ->
    val start = week.startDate
    when (range) {
      is ActivityRange.Last7Days -> start.dayOfWeek.name.take(1)
      is ActivityRange.Last6Months,
      is ActivityRange.Last90Days -> {
        val prev = weeks.getOrNull(index - 1)?.startDate
        if (prev == null || prev.month != start.month || prev.year != start.year) {
          monthInitial(start.month)
        } else {
          ""
        }
      }
      is ActivityRange.LastYear,
      is ActivityRange.Year -> {
        if (isQuarterStart(start)) {
          quarterOf(start.month).toString()
        } else {
          ""
        }
      }
    }
  }
}

private fun monthInitial(month: Month): String {
  return month.name.take(1)
}

private fun isQuarterStart(date: LocalDate): Boolean {
  return date.day <= QUARTER_START_DAY_LIMIT &&
    date.month in setOf(Month.JANUARY, Month.APRIL, Month.JULY, Month.OCTOBER)
}

private fun quarterOf(month: Month): Int {
  return month.ordinal / MONTHS_IN_QUARTER + FIRST_QUARTER_INDEX
}
