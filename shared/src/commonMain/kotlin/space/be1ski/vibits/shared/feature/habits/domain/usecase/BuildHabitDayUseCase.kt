package space.be1ski.vibits.shared.feature.habits.domain.usecase

import kotlinx.datetime.LocalDate
import space.be1ski.vibits.shared.feature.habits.domain.buildHabitStatuses
import space.be1ski.vibits.shared.feature.habits.domain.model.ContributionDay
import space.be1ski.vibits.shared.feature.habits.domain.model.DailyMemoInfo
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitConfig

/**
 * Builds a ContributionDay for a specific date with habits configuration.
 */
class BuildHabitDayUseCase {

  operator fun invoke(
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
}
