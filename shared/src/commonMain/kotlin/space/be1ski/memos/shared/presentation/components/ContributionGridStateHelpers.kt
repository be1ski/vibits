package space.be1ski.memos.shared.presentation.components

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.minus
import kotlinx.datetime.plus

private const val WEEK_END_OFFSET = 6
private const val QUARTERS_IN_YEAR = 4
private const val YEAR_START_DAY = 1

internal fun startOfWeek(date: LocalDate): LocalDate {
  var start = date
  while (start.dayOfWeek != DayOfWeek.MONDAY) {
    start = start.minus(DatePeriod(days = 1))
  }
  return start
}

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

internal fun rangeBounds(range: ActivityRange): RangeBounds {
  return when (range) {
    is ActivityRange.Week -> RangeBounds(
      start = range.startDate,
      end = range.startDate.plus(DatePeriod(days = WEEK_END_OFFSET))
    )
    is ActivityRange.Month -> RangeBounds(
      start = LocalDate(range.year, range.month, 1),
      end = LocalDate(range.year, range.month, 1)
        .plus(DatePeriod(months = 1))
        .minus(DatePeriod(days = 1))
    )
    is ActivityRange.Quarter -> {
      val quarterStartMonth = quarterStartMonth(range.index)
      RangeBounds(
        start = LocalDate(range.year, quarterStartMonth, 1),
        end = LocalDate(range.year, quarterStartMonth, 1)
          .plus(DatePeriod(months = MONTHS_IN_QUARTER))
          .minus(DatePeriod(days = 1))
      )
    }
    is ActivityRange.Year -> RangeBounds(
      start = LocalDate(range.year, Month.JANUARY, YEAR_START_DAY),
      end = LocalDate(range.year + 1, Month.JANUARY, YEAR_START_DAY)
        .minus(DatePeriod(days = 1))
    )
  }
}

internal fun quarterStartMonth(index: Int): Month {
  val safeIndex = index.coerceIn(FIRST_QUARTER_INDEX, QUARTERS_IN_YEAR)
  val monthIndex = (safeIndex - FIRST_QUARTER_INDEX) * MONTHS_IN_QUARTER
  return Month.entries[monthIndex]
}

private fun habitsForDay(context: DayDataContext): List<HabitConfig> {
  val configForDay = if (context.mode == ActivityMode.Habits) {
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
  val useHabits = context.mode == ActivityMode.Habits && habitsForDay.isNotEmpty()
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
    context.mode == ActivityMode.Posts -> context.counts[context.date] ?: 0
    else -> 0
  }
}

private fun completionRatio(totalHabits: Int, completed: Int): Float {
  return if (totalHabits > 0) completed.toFloat() / totalHabits.toFloat() else 0f
}
