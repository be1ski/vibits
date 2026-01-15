package space.be1ski.memos.shared.feature.habits.presentation

import kotlinx.datetime.LocalDate
import space.be1ski.memos.shared.feature.habits.domain.model.ActivityWeekData
import space.be1ski.memos.shared.feature.habits.domain.model.ContributionDay
import space.be1ski.memos.shared.feature.habits.domain.model.DailyMemoInfo
import space.be1ski.memos.shared.feature.habits.domain.model.HabitConfig
import space.be1ski.memos.shared.feature.habits.domain.usecase.BuildActivityDataUseCase
import space.be1ski.memos.shared.feature.habits.domain.usecase.BuildHabitDayUseCase

private val buildActivityDataUseCase = BuildActivityDataUseCase()
private val buildHabitDayUseCase = BuildHabitDayUseCase()

internal fun findDayByDate(
  weekData: ActivityWeekData,
  date: LocalDate
): ContributionDay? = buildActivityDataUseCase.findDayByDate(weekData, date)

internal fun buildHabitDay(
  date: LocalDate,
  habitsConfig: List<HabitConfig>,
  dailyMemo: DailyMemoInfo?
): ContributionDay? = buildHabitDayUseCase(date, habitsConfig, dailyMemo)
