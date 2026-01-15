package space.be1ski.memos.shared.feature.habits.presentation.components

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import space.be1ski.memos.shared.feature.habits.domain.model.ActivityWeekData
import space.be1ski.memos.shared.feature.habits.domain.model.ContributionDay
import space.be1ski.memos.shared.feature.habits.domain.model.HabitConfig
import space.be1ski.memos.shared.feature.habits.domain.model.HabitsConfigEntry
import space.be1ski.memos.shared.feature.habits.domain.usecase.BuildActivityDataUseCase
import space.be1ski.memos.shared.feature.habits.domain.usecase.DateCalculationsUseCase
import space.be1ski.memos.shared.feature.habits.domain.usecase.ExtractHabitsConfigUseCase
import space.be1ski.memos.shared.feature.memos.domain.model.Memo

private val dateCalculationsUseCase = DateCalculationsUseCase()
private val extractHabitsConfigUseCase = ExtractHabitsConfigUseCase()
private val buildActivityDataUseCase = BuildActivityDataUseCase()

/**
 * Returns the earliest memo date available in the dataset.
 */
fun earliestMemoDate(
  memos: List<Memo>,
  timeZone: TimeZone
): LocalDate? = dateCalculationsUseCase.earliestMemoDate(memos, timeZone)

/**
 * Returns the most recent habits config entry at or before [date].
 */
fun habitsConfigForDate(entries: List<HabitsConfigEntry>, date: LocalDate): HabitsConfigEntry? =
  extractHabitsConfigUseCase.forDate(entries, date)

internal fun extractHabitsConfigEntries(memos: List<Memo>, timeZone: TimeZone): List<HabitsConfigEntry> =
  extractHabitsConfigUseCase(memos, timeZone)

/**
 * Returns the last 7 in-range days, newest last.
 */
fun lastSevenDays(weekData: ActivityWeekData): List<ContributionDay> =
  buildActivityDataUseCase.lastSevenDays(weekData)

/**
 * Derives a week dataset for a single habit tag.
 */
fun activityWeekDataForHabit(
  weekData: ActivityWeekData,
  habit: HabitConfig
): ActivityWeekData = buildActivityDataUseCase.activityWeekDataForHabit(weekData, habit)
