package space.be1ski.vibits.shared.feature.habits.presentation.components

import kotlinx.datetime.LocalDate
import space.be1ski.vibits.shared.core.ui.ActivityMode
import space.be1ski.vibits.shared.feature.habits.domain.buildHabitStatuses
import space.be1ski.vibits.shared.feature.habits.domain.extractHabitTagsFromContent
import space.be1ski.vibits.shared.feature.habits.domain.model.ContributionDay
import space.be1ski.vibits.shared.feature.habits.domain.model.DailyMemoInfo
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitConfig
import space.be1ski.vibits.shared.feature.habits.domain.usecase.DateCalculationsUseCase

private val dateCalculationsUseCase = DateCalculationsUseCase()

internal fun startOfWeek(date: LocalDate): LocalDate =
  dateCalculationsUseCase.startOfWeek(date)

internal fun buildDayData(context: DayDataContext): ContributionDay {
  val dailyMemo = context.dailyMemos[context.date]
  val habitsForDay = habitsForDay(context)
  val habitState = buildHabitSelectionState(context, dailyMemo, habitsForDay)
  val completed = completedCount(context, habitState)
  val totalHabits = if (habitState.useHabits) habitsForDay.size else 0
  val ratio = completionRatio(totalHabits, completed)
  val inRange = context.date >= context.bounds.start && context.date <= context.bounds.end
  return ContributionDay(
    date = context.date,
    count = completed,
    totalHabits = totalHabits,
    completionRatio = ratio.coerceIn(0f, 1f),
    habitStatuses = habitState.habitStatuses,
    dailyMemo = dailyMemo,
    inRange = inRange
  )
}

private fun habitsForDay(context: DayDataContext): List<HabitConfig> {
  val configForDay = if (context.mode == ActivityMode.HABITS) {
    habitsConfigForDate(context.configTimeline, context.date)
  } else {
    null
  }
  return configForDay?.habits.orEmpty()
}

private fun buildHabitSelectionState(
  context: DayDataContext,
  dailyMemo: DailyMemoInfo?,
  habitsForDay: List<HabitConfig>
): HabitSelectionState {
  val useHabits = context.mode == ActivityMode.HABITS && habitsForDay.isNotEmpty()
  val habitStatuses = if (useHabits) {
    buildHabitStatuses(dailyMemo?.content, habitsForDay)
  } else {
    emptyList()
  }
  val memoHabitTags = if (useHabits) {
    extractHabitTagsFromContent(dailyMemo?.content)
  } else {
    emptySet()
  }
  val configTags = if (useHabits) habitsForDay.map { it.tag }.toSet() else emptySet()
  return HabitSelectionState(
    useHabits = useHabits,
    habitStatuses = habitStatuses,
    memoHabitTags = memoHabitTags,
    configTags = configTags
  )
}

private fun completedCount(
  context: DayDataContext,
  habitState: HabitSelectionState
): Int {
  return when {
    habitState.useHabits && habitState.memoHabitTags.isNotEmpty() -> {
      val memoRelevantTags = if (habitState.configTags.isNotEmpty()) {
        habitState.memoHabitTags.intersect(habitState.configTags)
      } else {
        habitState.memoHabitTags
      }
      memoRelevantTags.size
    }
    habitState.useHabits -> habitState.habitStatuses.count { it.done }
    context.mode == ActivityMode.POSTS -> context.counts[context.date] ?: 0
    else -> 0
  }
}

private fun completionRatio(totalHabits: Int, completed: Int): Float {
  return if (totalHabits > 0) completed.toFloat() / totalHabits.toFloat() else 0f
}
