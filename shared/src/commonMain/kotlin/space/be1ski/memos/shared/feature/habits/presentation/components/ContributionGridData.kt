package space.be1ski.memos.shared.feature.habits.presentation.components

import kotlinx.datetime.TimeZone
import space.be1ski.memos.shared.feature.habits.domain.model.ActivityWeekData
import space.be1ski.memos.shared.feature.habits.domain.model.ContributionDay
import space.be1ski.memos.shared.feature.habits.domain.model.HabitConfig
import space.be1ski.memos.shared.feature.habits.domain.model.HabitStatus
import space.be1ski.memos.shared.feature.habits.domain.model.HabitsConfigEntry
import space.be1ski.memos.shared.feature.habits.domain.parseHabitConfigLine
import space.be1ski.memos.shared.feature.memos.domain.model.Memo

private const val LAST_SEVEN_DAYS = 7

/**
 * Returns the earliest memo date available in the dataset.
 */
fun earliestMemoDate(
  memos: List<Memo>,
  timeZone: TimeZone
): kotlinx.datetime.LocalDate? {
  return memos.mapNotNull { memo ->
    parseDailyDateFromContent(memo.content) ?: parseMemoDate(memo, timeZone)
  }.minOrNull()
}

/**
 * Returns the most recent habits config entry at or before [date].
 */
fun habitsConfigForDate(entries: List<HabitsConfigEntry>, date: kotlinx.datetime.LocalDate): HabitsConfigEntry? {
  return entries.lastOrNull { entry -> entry.date <= date }
}

internal fun extractHabitsConfigEntries(memos: List<Memo>, timeZone: TimeZone): List<HabitsConfigEntry> {
  val entries = memos.mapNotNull { memo ->
    if (!memo.content.contains("#habits/config") && !memo.content.contains("#habits_config")) {
      return@mapNotNull null
    }
    val instant = parseMemoInstant(memo) ?: return@mapNotNull null
    val date = parseMemoDate(memo, timeZone) ?: return@mapNotNull null
    val lines = memo.content.lineSequence()
      .map { it.trim() }
      .filter { it.isNotBlank() }
      .filterNot { it.startsWith("#habits/config") || it.startsWith("#habits_config") }
    val habits = lines.mapNotNull { line -> parseHabitConfigLine(line) }
      .distinctBy { it.tag }
      .toList()
    HabitsConfigEntry(date = date, habits = habits, memo = memo) to instant
  }
  return entries
    .sortedBy { it.second.toEpochMilliseconds() }
    .map { it.first }
}

/**
 * Returns the last 7 in-range days, newest last.
 */
fun lastSevenDays(weekData: ActivityWeekData): List<ContributionDay> {
  val days = weekData.weeks.flatMap { it.days }.filter { it.inRange }
  return days.takeLast(LAST_SEVEN_DAYS)
}

/**
 * Derives a week dataset for a single habit tag.
 */
fun activityWeekDataForHabit(
  weekData: ActivityWeekData,
  habit: HabitConfig
): ActivityWeekData {
  val weeks = weekData.weeks.map { week ->
    val days = week.days.map { day ->
      val hasConfig = day.totalHabits > 0
      val done = if (hasConfig) day.habitStatuses.firstOrNull { it.tag == habit.tag }?.done == true else false
      val count = if (hasConfig && done) 1 else 0
      day.copy(
        count = count,
        totalHabits = if (hasConfig) 1 else 0,
        completionRatio = if (hasConfig && done) 1f else 0f,
        habitStatuses = if (hasConfig) {
          listOf(HabitStatus(tag = habit.tag, label = habit.label, done = done))
        } else {
          emptyList()
        }
      )
    }
    week.copy(
      days = days,
      weeklyCount = days.sumOf { it.count }
    )
  }
  val maxDaily = weeks.maxOfOrNull { week -> week.days.maxOfOrNull { it.count } ?: 0 } ?: 0
  val maxWeekly = weeks.maxOfOrNull { it.weeklyCount } ?: 0
  return ActivityWeekData(weeks = weeks, maxDaily = maxDaily, maxWeekly = maxWeekly)
}
