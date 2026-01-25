package space.be1ski.vibits.shared.feature.habits.domain

import kotlinx.datetime.LocalDate
import space.be1ski.vibits.shared.feature.habits.domain.model.ContributionDay
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitConfig
import space.be1ski.vibits.shared.feature.memos.domain.model.PostTags

/**
 * Builds the content for a daily habits memo.
 */
fun buildDailyContent(
  date: LocalDate,
  habitsConfig: List<HabitConfig>,
  selections: Map<HabitTag, IsSelected>,
): String =
  buildString {
    append(PostTags.HABITS_DAILY).append(" ").append(date).append("\n\n")
    habitsConfig.forEach { habit ->
      val done = selections[habit.tag] == true
      if (done) {
        append(habit.tag).append('\n')
      }
    }
  }

/**
 * Extracts the date from a tracking memo's content.
 * Returns null if the content is not a valid tracking memo.
 */
fun extractDateFromTrackingContent(content: String): LocalDate? {
  val firstLine = content.lineSequence().firstOrNull()?.trim()
  if (firstLine == null || !firstLine.startsWith(PostTags.HABITS_DAILY)) {
    return null
  }

  val dateString = firstLine.removePrefix(PostTags.HABITS_DAILY).trim()
  return runCatching { LocalDate.parse(dateString) }.getOrNull()
}

/**
 * Builds the content for a habits config memo from a list of HabitConfig entries.
 */
fun buildHabitsConfigContentFromList(entries: List<HabitConfig>): String =
  buildString {
    append(PostTags.HABITS_CONFIG).append("\n\n")
    entries.forEach { entry ->
      append(entry.label)
        .append(" | ")
        .append(entry.tag)
        .append(" | ")
        .append(formatHexColor(entry.color))
        .append('\n')
    }
  }

/**
 * Builds initial editor selections from a contribution day.
 */
fun buildHabitsEditorSelections(
  day: ContributionDay,
  habitsConfig: List<HabitConfig>,
): Map<HabitTag, IsSelected> =
  if (habitsConfig.isNotEmpty()) {
    habitsConfig.associate { habit ->
      habit.tag to (day.habitStatuses.firstOrNull { it.tag == habit.tag }?.done == true)
    }
  } else {
    day.habitStatuses.associate { it.tag to it.done }
  }
