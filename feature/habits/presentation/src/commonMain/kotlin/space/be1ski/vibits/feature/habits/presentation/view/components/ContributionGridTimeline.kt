package space.be1ski.vibits.feature.habits.presentation.view.components

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import space.be1ski.vibits.core.ui.date.DateFormatter
import space.be1ski.vibits.core.utils.date.quarterIndex
import space.be1ski.vibits.feature.habits.domain.model.ActivityRange
import space.be1ski.vibits.feature.habits.domain.model.ActivityWeek

internal fun buildTimelineLabels(
  weeks: List<ActivityWeek>,
  range: ActivityRange,
  formatter: DateFormatter,
): List<String> {
  if (weeks.isEmpty()) {
    return emptyList()
  }
  return weeks.mapIndexed { index, week ->
    val start = week.inRangeDate()
    when (range) {
      is ActivityRange.Week -> formatter.monthInitial(start.month)
      is ActivityRange.Month -> {
        val prev = weeks.getOrNull(index - 1)?.inRangeDate()
        if (prev == null || prev.month != start.month || prev.year != start.year) {
          formatter.monthInitial(start.month)
        } else {
          ""
        }
      }
      is ActivityRange.Quarter -> {
        val prev = weeks.getOrNull(index - 1)?.inRangeDate()
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

private fun ActivityWeek.inRangeDate(): LocalDate = days.firstOrNull { it.inRange }?.date ?: startDate

private const val QUARTER_START_DAY_LIMIT = 7

private fun isQuarterStart(date: LocalDate): Boolean =
  date.day <= QUARTER_START_DAY_LIMIT &&
    date.month in setOf(Month.JANUARY, Month.APRIL, Month.JULY, Month.OCTOBER)
