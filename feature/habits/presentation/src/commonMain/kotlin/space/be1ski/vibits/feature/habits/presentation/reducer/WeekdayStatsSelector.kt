package space.be1ski.vibits.feature.habits.presentation.reducer

import kotlinx.datetime.DayOfWeek
import space.be1ski.vibits.feature.habits.domain.model.ActivitySummary
import space.be1ski.vibits.feature.habits.domain.model.ContributionDay
import space.be1ski.vibits.feature.habits.presentation.state.WeekdayPerformanceCardState
import space.be1ski.vibits.feature.habits.presentation.state.WeekdayPerformanceStats

private const val MIN_OBSERVATIONS = 2

internal object WeekdayStatsSelector {
  operator fun invoke(
    summary: ActivitySummary,
    habitTag: String,
  ): WeekdayPerformanceCardState {
    val allDays = summary.weeks.flatMap { it.days }
    val observableDays = allDays.filter { it.isObservable(habitTag) }
    val byWeekday =
      DayOfWeek.entries.associateWith { dow ->
        observableDays.filter { it.date.dayOfWeek == dow }
      }
    val hasSufficientData = byWeekday.values.all { it.size >= MIN_OBSERVATIONS }
    val rates =
      byWeekday.mapValues { (_, days) ->
        if (days.isEmpty()) {
          null
        } else {
          days.count { day -> day.habitStatuses.any { it.tag == habitTag && it.done } }.toFloat() / days.size
        }
      }
    val (isBest, isWorst) =
      if (hasSufficientData) {
        resolveHighlights(rates.mapValues { requireNotNull(it.value) })
      } else {
        Pair(null, null)
      }
    val stats =
      DayOfWeek.entries.map { dow ->
        WeekdayPerformanceStats(
          dayOfWeek = dow,
          completionRate = rates.getValue(dow),
          isBest = dow == isBest,
          isWorst = dow == isWorst,
        )
      }
    val averageCompletionRate =
      if (hasSufficientData) {
        rates.values.filterNotNull().average().toFloat()
      } else {
        null
      }
    return WeekdayPerformanceCardState(
      stats = stats,
      hasSufficientData = hasSufficientData,
      averageCompletionRate = averageCompletionRate,
    )
  }

  private fun ContributionDay.isObservable(habitTag: String): Boolean =
    inRange &&
      isClickable &&
      habitStatuses.any { it.tag == habitTag }

  private fun resolveHighlights(rates: Map<DayOfWeek, Float>): Pair<DayOfWeek?, DayOfWeek?> {
    val maxRate = rates.values.maxOrNull() ?: 0f
    val minRate = rates.values.minOrNull() ?: 0f
    val best =
      if (rates.values.count { it == maxRate } == 1) {
        rates.entries.first { it.value == maxRate }.key
      } else {
        null
      }
    val worst =
      if (rates.values.count { it == minRate } == 1) {
        rates.entries.first { it.value == minRate }.key
      } else {
        null
      }
    return if (best == worst) Pair(null, null) else Pair(best, worst)
  }
}
