package space.be1ski.vibits.shared.feature.habits.domain.usecase

import dev.zacsweers.metro.Inject
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import space.be1ski.vibits.shared.feature.habits.domain.model.ActivityWeekData
import space.be1ski.vibits.shared.feature.habits.domain.model.ContributionDay
import space.be1ski.vibits.shared.feature.habits.domain.model.StreakData

/**
 * Calculates streak statistics from activity data.
 */
@Inject
class CalculateStreakUseCase {
  operator fun invoke(
    weekData: ActivityWeekData,
    today: LocalDate,
    configStartDate: LocalDate? = null,
  ): StreakData {
    val allDays =
      weekData.weeks
        .flatMap { it.days }
        .filter { it.totalHabits > 0 } // Only days with habit configuration
        .filter { configStartDate == null || it.date >= configStartDate }
        .filter { it.date <= today } // Ignore future dates
        .sortedBy { it.date }

    if (allDays.isEmpty()) {
      return StreakData(current = 0, best = 0, currentStreakStart = null)
    }

    val current = calculateCurrentStreak(allDays, today)
    val best = calculateBestStreak(allDays)

    val currentStreakStart =
      if (current > 0) {
        allDays
          .asReversed()
          .take(current)
          .lastOrNull()
          ?.date
      } else {
        null
      }

    return StreakData(current = current, best = best, currentStreakStart = currentStreakStart)
  }

  private fun calculateCurrentStreak(
    days: List<ContributionDay>,
    today: LocalDate,
  ): Int {
    var streak = 0
    var currentDate = today

    // Count backwards from today, filter out future dates
    for (day in days.asReversed().filter { it.date <= today }) {
      // Stop if there's a gap or if the day has no completions
      if (day.date < currentDate || day.count == 0) break

      streak++
      currentDate = currentDate.minus(DatePeriod(days = 1))
    }

    return streak
  }

  private fun calculateBestStreak(days: List<ContributionDay>): Int {
    var bestStreak = 0
    var currentStreak = 0
    var previousDate: LocalDate? = null

    for (day in days) {
      val isConsecutive =
        previousDate == null ||
          day.date == previousDate.plus(DatePeriod(days = 1))

      if (day.count > 0) {
        currentStreak = if (isConsecutive) currentStreak + 1 else 1
        bestStreak = maxOf(bestStreak, currentStreak)
      } else {
        currentStreak = 0
      }

      previousDate = day.date
    }

    return bestStreak
  }
}
