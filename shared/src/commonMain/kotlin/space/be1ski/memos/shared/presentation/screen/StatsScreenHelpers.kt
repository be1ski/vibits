package space.be1ski.memos.shared.presentation.screen

import kotlinx.datetime.LocalDate
import space.be1ski.memos.shared.presentation.components.ContributionDay
import space.be1ski.memos.shared.presentation.components.DailyMemoInfo
import space.be1ski.memos.shared.presentation.components.HabitConfig
import space.be1ski.memos.shared.presentation.components.buildHabitStatuses

internal fun findDayByDate(
  weekData: space.be1ski.memos.shared.presentation.components.ActivityWeekData,
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
