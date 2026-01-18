package space.be1ski.vibits.shared.feature.habits.domain.model

import kotlinx.datetime.LocalDate

private const val LAST_SEVEN_DAYS = 7

/**
 * Fully prepared dataset for activity charts.
 */
data class ActivityWeekData(
  /** Ordered list of week entries. */
  val weeks: List<ActivityWeek>,
  /** Maximum posts for a single day in range. */
  val maxDaily: Int,
  /** Maximum posts in a week in range. */
  val maxWeekly: Int,
)

/**
 * Returns the last 7 in-range days.
 */
fun ActivityWeekData.lastSevenDays(): List<ContributionDay> {
  val days = weeks.flatMap { it.days }.filter { it.inRange }
  return days.takeLast(LAST_SEVEN_DAYS)
}

/**
 * Finds a contribution day by date.
 */
fun ActivityWeekData.findDayByDate(date: LocalDate): ContributionDay? =
  weeks.firstNotNullOfOrNull { week ->
    week.days.firstOrNull { it.date == date }
  }

/**
 * Filters activity data for a single habit.
 */
fun ActivityWeekData.forHabit(habit: HabitConfig): ActivityWeekData {
  val filteredWeeks =
    weeks.map { week ->
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
  val newMaxDaily = filteredWeeks.maxOfOrNull { week -> week.days.maxOfOrNull { it.count } ?: 0 } ?: 0
  val newMaxWeekly = filteredWeeks.maxOfOrNull { it.weeklyCount } ?: 0
  return ActivityWeekData(weeks = filteredWeeks, maxDaily = newMaxDaily, maxWeekly = newMaxWeekly)
}
