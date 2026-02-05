package space.be1ski.vibits.feature.habits.presentation.view

import kotlinx.datetime.LocalDate
import space.be1ski.vibits.feature.habits.domain.model.ContributionDay
import space.be1ski.vibits.feature.habits.domain.model.DailyMemo
import space.be1ski.vibits.feature.habits.domain.model.HabitConfig
import space.be1ski.vibits.feature.habits.domain.usecase.BuildHabitDayUseCase

fun buildHabitDay(
  date: LocalDate,
  habitsConfig: List<HabitConfig>,
  dailyMemo: DailyMemo?,
): ContributionDay? = BuildHabitDayUseCase(date, habitsConfig, dailyMemo)
