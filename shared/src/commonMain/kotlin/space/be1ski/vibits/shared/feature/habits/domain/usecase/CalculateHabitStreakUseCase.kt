package space.be1ski.vibits.shared.feature.habits.domain.usecase

import dev.zacsweers.metro.Inject
import kotlinx.datetime.LocalDate
import space.be1ski.vibits.shared.feature.habits.domain.model.ActivityWeekData
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitConfig
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitStreakData
import space.be1ski.vibits.shared.feature.habits.domain.model.forHabit

/**
 * Calculates streak statistics for a specific habit.
 */
@Inject
class CalculateHabitStreakUseCase(
  private val calculateStreakUseCase: CalculateStreakUseCase,
) {
  operator fun invoke(
    weekData: ActivityWeekData,
    habit: HabitConfig,
    today: LocalDate,
    configStartDate: LocalDate? = null,
  ): HabitStreakData {
    val habitWeekData = weekData.forHabit(habit)
    val streakData = calculateStreakUseCase(habitWeekData, today, configStartDate)

    return HabitStreakData(
      habitTag = habit.tag,
      current = streakData.current,
      best = streakData.best,
      currentStreakStart = streakData.currentStreakStart,
    )
  }
}
