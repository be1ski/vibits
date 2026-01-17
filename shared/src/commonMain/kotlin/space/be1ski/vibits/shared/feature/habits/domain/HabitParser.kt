package space.be1ski.vibits.shared.feature.habits.domain

import space.be1ski.vibits.shared.feature.habits.domain.model.DEFAULT_HABIT_COLOR
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitConfig
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitStatus

/**
 * Parses a single line from a habits config memo.
 * Supports formats:
 * - "Label | #habits/tag | #hexcolor"
 * - "Label | #habits/tag"
 * - "#habits/tag" (label derived from tag)
 * - "Label" (tag derived from label)
 */
fun parseHabitConfigLine(line: String): HabitConfig? {
  val parts = line.split("|", limit = 3).map { it.trim() }.filter { it.isNotBlank() }
  if (parts.isEmpty()) {
    return null
  }
  val (label, tagRaw, color) =
    when (parts.size) {
      1 -> {
        val raw = parts.first()
        val tag = normalizeHabitTag(raw)
        val lbl = if (raw.startsWith("#habits/") || raw.startsWith("#habit/")) labelFromTag(tag) else raw
        Triple(lbl, tag, DEFAULT_HABIT_COLOR)
      }
      2 -> {
        val lbl = parts[0]
        val tag = normalizeHabitTag(parts[1])
        Triple(lbl, tag, DEFAULT_HABIT_COLOR)
      }
      else -> {
        val lbl = parts[0]
        val tag = normalizeHabitTag(parts[1])
        val clr = parseHexColor(parts[2]) ?: DEFAULT_HABIT_COLOR
        Triple(lbl, tag, clr)
      }
    }
  return HabitConfig(tag = tagRaw, label = label, color = color)
}

/**
 * Parses a hex color string to ARGB Long.
 * Supports formats: #RRGGBB or #AARRGGBB
 */
@Suppress("MagicNumber")
fun parseHexColor(hex: String): Long? {
  val clean = hex.trim().removePrefix("#")
  return when (clean.length) {
    6 -> clean.toLongOrNull(16)?.let { 0xFF000000L or it }
    8 -> clean.toLongOrNull(16)
    else -> null
  }
}

/**
 * Formats an ARGB Long color to hex string (#RRGGBB).
 */
@Suppress("MagicNumber")
fun formatHexColor(color: Long): String {
  val rgb = color and 0xFFFFFFL
  return "#${rgb.toString(16).uppercase().padStart(6, '0')}"
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
fun labelFromTag(tag: String): String = tag.removePrefix("#habits/").removePrefix("#habit/").replace('_', ' ')

/**
 * Builds habit statuses for a day given the memo content and habit configurations.
 */
fun buildHabitStatuses(
  content: String?,
  habits: List<HabitConfig>,
): List<HabitStatus> {
  val done =
    if (content.isNullOrBlank()) {
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
fun extractCompletedHabits(
  content: String,
  habits: Set<String>,
): Set<String> {
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
