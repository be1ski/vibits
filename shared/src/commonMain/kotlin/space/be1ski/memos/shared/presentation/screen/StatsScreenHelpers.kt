package space.be1ski.memos.shared.presentation.screen

import kotlinx.datetime.LocalDate
import space.be1ski.memos.shared.presentation.components.ContributionDay
import space.be1ski.memos.shared.presentation.components.DailyMemoInfo
import space.be1ski.memos.shared.presentation.components.HabitConfig
import space.be1ski.memos.shared.presentation.components.buildHabitStatuses

internal fun buildHabitsEditorSelections(
  day: ContributionDay,
  habitsConfig: List<HabitConfig>
): Map<String, Boolean> {
  return if (habitsConfig.isNotEmpty()) {
    habitsConfig.associate { habit ->
      habit.tag to (day.habitStatuses.firstOrNull { it.tag == habit.tag }?.done == true)
    }
  } else {
    day.habitStatuses.associate { it.tag to it.done }
  }
}

internal fun findDayByDate(
  weekData: space.be1ski.memos.shared.presentation.components.ActivityWeekData,
  date: kotlinx.datetime.LocalDate
): ContributionDay? {
  return weekData.weeks.firstNotNullOfOrNull { week ->
    week.days.firstOrNull { it.date == date }
  }
}

internal fun buildDailyContent(
  date: LocalDate,
  habitsConfig: List<HabitConfig>,
  selections: Map<String, Boolean>
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

internal fun buildHabitsConfigContent(rawText: String): String {
  val entries = rawText.lineSequence()
    .map { it.trim() }
    .filter { it.isNotBlank() }
    .mapNotNull { parseHabitConfigLine(it) }
    .toList()
  return buildString {
    append("#habits/config\n\n")
    entries.forEach { entry ->
      append(entry.label).append(" | ").append(entry.tag).append('\n')
    }
  }
}

internal fun parseHabitConfigLine(line: String): HabitConfig? {
  val parts = line.split("|", limit = 2).map { it.trim() }.filter { it.isNotBlank() }
  if (parts.isEmpty()) {
    return null
  }
  val (label, tagRaw) = if (parts.size == 1) {
    val raw = parts.first()
    val tag = normalizeHabitTag(raw)
    val label = if (raw.startsWith("#habits/") || raw.startsWith("#habit/")) labelFromTag(tag) else raw
    label to tag
  } else {
    val label = parts[0]
    val tag = normalizeHabitTag(parts[1])
    label to tag
  }
  return HabitConfig(tag = tagRaw, label = label)
}

private fun normalizeHabitTag(raw: String): String {
  val trimmed = raw.trim()
  val withoutPrefix = trimmed.removePrefix("#habits/").removePrefix("#habit/")
  val sanitized = withoutPrefix.replace("\\s+".toRegex(), "_")
  return "#habits/$sanitized"
}

private fun labelFromTag(tag: String): String {
  return tag.removePrefix("#habits/").removePrefix("#habit/").replace('_', ' ')
}

internal fun buildHabitDay(
  date: LocalDate,
  habitsConfig: List<HabitConfig>,
  dailyMemo: DailyMemoInfo?
): ContributionDay? {
  if (habitsConfig.isEmpty()) {
    return null
  }
  val statuses = buildHabitStatuses(dailyMemo?.content, habitsConfig)
  val completed = statuses.count { it.done }
  val total = habitsConfig.size
  val ratio = if (total > 0) completed.toFloat() / total.toFloat() else 0f
  return ContributionDay(
    date = date,
    count = completed,
    totalHabits = total,
    completionRatio = ratio.coerceIn(0f, 1f),
    habitStatuses = statuses,
    dailyMemo = dailyMemo,
    inRange = true
  )
}
