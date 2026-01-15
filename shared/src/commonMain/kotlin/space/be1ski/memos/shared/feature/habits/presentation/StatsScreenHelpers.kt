package space.be1ski.memos.shared.feature.habits.presentation

import kotlinx.datetime.LocalDate
import space.be1ski.memos.shared.core.ui.ActivityRange
import space.be1ski.memos.shared.feature.habits.domain.buildHabitStatuses
import space.be1ski.memos.shared.feature.habits.domain.model.ActivityWeekData
import space.be1ski.memos.shared.feature.habits.domain.model.ContributionDay
import space.be1ski.memos.shared.feature.habits.domain.model.DailyMemoInfo
import space.be1ski.memos.shared.feature.habits.domain.model.HabitConfig
import space.be1ski.memos.shared.feature.habits.presentation.components.rangeBounds

internal fun findDayByDate(
  weekData: ActivityWeekData,
  date: kotlinx.datetime.LocalDate
): ContributionDay? {
  return weekData.weeks.firstNotNullOfOrNull { week ->
    week.days.firstOrNull { it.date == date }
  }
}

internal fun buildHabitDay(
  date: LocalDate,
  habitsConfig: List<HabitConfig>,
  dailyMemo: DailyMemoInfo?
): ContributionDay? {
  if (habitsConfig.isEmpty()) {
    return null
  }
  val statuses = buildHabitStatuses(dailyMemo?.content, habitsConfig)
  val completed = statuses.count { it.done }
  val total = habitsConfig.size
  val ratio = if (total > 0) completed.toFloat() / total.toFloat() else 0f
  return ContributionDay(
    date = date,
    count = completed,
    totalHabits = total,
    completionRatio = ratio.coerceIn(0f, 1f),
    habitStatuses = statuses,
    dailyMemo = dailyMemo,
    inRange = true
  )
}

internal fun calculateSuccessRate(
  weekData: ActivityWeekData,
  range: ActivityRange,
  today: LocalDate,
  configStartDate: LocalDate? = null
): SuccessRateData {
  val bounds = rangeBounds(range)
  val effectiveStart = if (configStartDate != null && configStartDate > bounds.start) {
    configStartDate
  } else {
    bounds.start
  }
  val effectiveEnd = if (today in bounds.start..bounds.end) today else bounds.end

  val days = weekData.weeks
    .flatMap { it.days }
    .filter { it.date >= effectiveStart && it.date <= effectiveEnd && it.totalHabits > 0 }

  val completed = days.sumOf { it.count }
  val total = days.sumOf { it.totalHabits }
  val rate = if (total > 0) completed.toFloat() / total else 0f

  return SuccessRateData(completed, total, rate)
}
