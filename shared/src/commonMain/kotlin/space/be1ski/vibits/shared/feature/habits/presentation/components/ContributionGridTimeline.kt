package space.be1ski.vibits.shared.feature.habits.presentation.components

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import space.be1ski.vibits.shared.core.platform.DateFormatter
import space.be1ski.vibits.shared.core.ui.ActivityRange
import space.be1ski.vibits.shared.feature.habits.domain.model.ActivityWeek

internal fun buildTimelineLabels(
  weeks: List<ActivityWeek>,
  range: ActivityRange,
  formatter: DateFormatter
): List<String> {
  if (weeks.isEmpty()) {
    return emptyList()
  }
  return weeks.mapIndexed { index, week ->
    val start = week.startDate
    when (range) {
      is ActivityRange.Week -> formatter.monthInitial(start.month)
      is ActivityRange.Month -> {
        val prev = weeks.getOrNull(index - 1)?.startDate
        if (prev == null || prev.month != start.month || prev.year != start.year) {
          formatter.monthInitial(start.month)
        } else {
          ""
        }
      }
      is ActivityRange.Quarter -> {
        val prev = weeks.getOrNull(index - 1)?.startDate
        if (prev == null || prev.month != start.month || prev.year != start.year) {
          formatter.monthInitial(start.month)
        } else {
          ""
        }
      }
      is ActivityRange.Year -> {
        if (isQuarterStart(start)) {
          quarterIndex(start.month).toString()
        } else {
          ""
        }
      }
    }
  }
}

private fun isQuarterStart(date: LocalDate): Boolean {
  return date.day <= QUARTER_START_DAY_LIMIT &&
    date.month in setOf(Month.JANUARY, Month.APRIL, Month.JULY, Month.OCTOBER)
}

