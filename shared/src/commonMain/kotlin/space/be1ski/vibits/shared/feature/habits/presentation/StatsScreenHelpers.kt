package space.be1ski.vibits.shared.feature.habits.presentation

import kotlinx.datetime.LocalDate
import space.be1ski.vibits.shared.feature.habits.domain.model.ContributionDay
import space.be1ski.vibits.shared.feature.habits.domain.model.DailyMemoInfo
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitConfig
import space.be1ski.vibits.shared.feature.habits.domain.usecase.BuildHabitDayUseCase

private val buildHabitDayUseCase = BuildHabitDayUseCase()

internal fun buildHabitDay(
  date: LocalDate,
  habitsConfig: List<HabitConfig>,
  dailyMemo: DailyMemoInfo?,
): ContributionDay? = buildHabitDayUseCase(date, habitsConfig, dailyMemo)
