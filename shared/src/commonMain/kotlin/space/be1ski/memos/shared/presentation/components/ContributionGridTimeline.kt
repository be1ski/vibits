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
      is ActivityRange.Week -> monthInitial(start.month)
      is ActivityRange.Month -> {
        val prev = weeks.getOrNull(index - 1)?.startDate
        if (prev == null || prev.month != start.month || prev.year != start.year) {
          monthInitial(start.month)
        } else {
          ""
        }
      }
      is ActivityRange.Quarter -> {
        val prev = weeks.getOrNull(index - 1)?.startDate
        if (prev == null || prev.month != start.month || prev.year != start.year) {
          monthInitial(start.month)
        } else {
          ""
        }
      }
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
