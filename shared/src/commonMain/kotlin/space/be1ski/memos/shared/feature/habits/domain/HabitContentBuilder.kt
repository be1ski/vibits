package space.be1ski.memos.shared.feature.habits.domain

import kotlinx.datetime.LocalDate
import space.be1ski.memos.shared.feature.habits.domain.model.ContributionDay
import space.be1ski.memos.shared.feature.habits.domain.model.HabitConfig

/**
 * Builds the content for a daily habits memo.
 */
fun buildDailyContent(
  date: LocalDate,
  habitsConfig: List<HabitConfig>,
  selections: Map<HabitTag, IsSelected>
): String {
  return buildString {
    append("#habits/daily ").append(date).append("\n\n")
    habitsConfig.forEach { habit ->
      val done = selections[habit.tag] == true
      if (done) {
        append(habit.tag).append('\n')
      }
    }
  }
}

/**
 * Builds the content for a habits config memo.
 */
fun buildHabitsConfigContent(rawText: String): String {
  val entries = rawText.lineSequence()
    .map { it.trim() }
    .filter { it.isNotBlank() }
    .mapNotNull { parseHabitConfigLine(it) }
    .toList()
  return buildHabitsConfigContentFromList(entries)
}

/**
 * Builds the content for a habits config memo from a list of HabitConfig entries.
 */
fun buildHabitsConfigContentFromList(entries: List<HabitConfig>): String {
  return buildString {
    append("#habits/config\n\n")
    entries.forEach { entry ->
      append(entry.label)
        .append(" | ")
        .append(entry.tag)
        .append(" | ")
        .append(formatHexColor(entry.color))
        .append('\n')
    }
  }
}

/**
 * Builds initial editor selections from a contribution day.
 */
fun buildHabitsEditorSelections(
  day: ContributionDay,
  habitsConfig: List<HabitConfig>
): Map<HabitTag, IsSelected> {
  return if (habitsConfig.isNotEmpty()) {
    habitsConfig.associate { habit ->
      habit.tag to (day.habitStatuses.firstOrNull { it.tag == habit.tag }?.done == true)
    }
  } else {
    day.habitStatuses.associate { it.tag to it.done }
  }
}
