package space.be1ski.memos.shared.domain.habits

import space.be1ski.memos.shared.presentation.components.HabitConfig
import space.be1ski.memos.shared.presentation.components.HabitStatus

/**
 * Parses a single line from a habits config memo.
 * Supports formats:
 * - "Label | #habits/tag"
 * - "#habits/tag" (label derived from tag)
 * - "Label" (tag derived from label)
 */
fun parseHabitConfigLine(line: String): HabitConfig? {
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

/**
 * Normalizes a raw habit tag to the canonical #habits/name format.
 */
fun normalizeHabitTag(raw: String): String {
  val trimmed = raw.trim()
  val withoutPrefix = trimmed.removePrefix("#habits/").removePrefix("#habit/")
  val sanitized = withoutPrefix.replace("\\s+".toRegex(), "_")
  return "#habits/$sanitized"
}

/**
 * Extracts a human-readable label from a habit tag.
 */
fun labelFromTag(tag: String): String {
  return tag.removePrefix("#habits/").removePrefix("#habit/").replace('_', ' ')
}

/**
 * Builds habit statuses for a day given the memo content and habit configurations.
 */
fun buildHabitStatuses(content: String?, habits: List<HabitConfig>): List<HabitStatus> {
  val done = if (content.isNullOrBlank()) {
    emptySet()
  } else {
    extractCompletedHabits(content, habits.map { it.tag }.toSet())
  }
  return if (habits.isEmpty()) {
    emptyList()
  } else {
    habits.map { habit ->
      HabitStatus(tag = habit.tag, label = habit.label, done = done.contains(habit.tag))
    }
  }
}

/**
 * Extracts completed habits from memo content.
 * Supports both checkbox format and plain tag format.
 */
fun extractCompletedHabits(content: String, habits: Set<String>): Set<String> {
  val done = mutableSetOf<String>()
  val lines = content.lineSequence()
  val checkboxRegex = Regex("^\\s*[-*]\\s*\\[(x|X)\\]\\s+(.+)$")
  var sawCheckbox = false
  lines.forEach { line ->
    val match = checkboxRegex.find(line)
    if (match != null) {
      sawCheckbox = true
      val trailing = match.groupValues[2]
      val habitTag = habits.firstOrNull { tag -> trailing.contains(tag) }
      if (habitTag != null) {
        done.add(habitTag)
      }
      return@forEach
    }
  }
  if (!sawCheckbox) {
    val tags = extractHabitTagsFromContent(content)
    done.addAll(tags.intersect(habits))
  }
  return done
}

/**
 * Extracts all habit tags from content (excluding #habits/daily marker).
 */
fun extractHabitTagsFromContent(content: String?): Set<String> {
  if (content.isNullOrBlank()) {
    return emptySet()
  }
  return Regex("#habits/[^\\s]+")
    .findAll(content)
    .map { it.value }
    .filterNot { it.equals("#habits/daily", ignoreCase = true) || it.startsWith("#habits/daily") }
    .toSet()
}
