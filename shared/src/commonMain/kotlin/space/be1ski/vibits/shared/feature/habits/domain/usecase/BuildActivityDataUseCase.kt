package space.be1ski.vibits.shared.feature.habits.domain.usecase

import kotlinx.datetime.LocalDate
import space.be1ski.vibits.shared.feature.habits.domain.model.ActivityWeekData
import space.be1ski.vibits.shared.feature.habits.domain.model.ContributionDay
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitConfig
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitStatus

private const val LAST_SEVEN_DAYS = 7

/**
 * Use case for building and filtering activity data.
 */
class BuildActivityDataUseCase {
  /**
   * Returns the last 7 in-range days from activity data.
   */
  fun lastSevenDays(weekData: ActivityWeekData): List<ContributionDay> {
    val days = weekData.weeks.flatMap { it.days }.filter { it.inRange }
    return days.takeLast(LAST_SEVEN_DAYS)
  }

  /**
   * Finds a contribution day by date.
   */
  fun findDayByDate(
    weekData: ActivityWeekData,
    date: LocalDate,
  ): ContributionDay? =
    weekData.weeks.firstNotNullOfOrNull { week ->
      week.days.firstOrNull { it.date == date }
    }

  /**
   * Filters activity data for a single habit.
   */
  fun activityWeekDataForHabit(
    weekData: ActivityWeekData,
    habit: HabitConfig,
  ): ActivityWeekData {
    val weeks =
      weekData.weeks.map { week ->
        val days =
          week.days.map { day ->
            val hasConfig = day.totalHabits > 0
            val done =
              if (hasConfig) {
                day.habitStatuses.firstOrNull { it.tag == habit.tag }?.done == true
              } else {
                false
              }
            val count = if (hasConfig && done) 1 else 0
            day.copy(
              count = count,
              totalHabits = if (hasConfig) 1 else 0,
              completionRatio = if (hasConfig && done) 1f else 0f,
              habitStatuses =
                if (hasConfig) {
                  listOf(HabitStatus(tag = habit.tag, label = habit.label, done = done))
                } else {
                  emptyList()
                },
            )
          }
        week.copy(
          days = days,
          weeklyCount = days.sumOf { it.count },
        )
      }
    val maxDaily = weeks.maxOfOrNull { week -> week.days.maxOfOrNull { it.count } ?: 0 } ?: 0
    val maxWeekly = weeks.maxOfOrNull { it.weeklyCount } ?: 0
    return ActivityWeekData(weeks = weeks, maxDaily = maxDaily, maxWeekly = maxWeekly)
  }
}
