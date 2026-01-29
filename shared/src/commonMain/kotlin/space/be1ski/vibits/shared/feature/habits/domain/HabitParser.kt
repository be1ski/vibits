package space.be1ski.vibits.shared.feature.habits.domain

import space.be1ski.vibits.shared.core.ui.theme.DefaultHabitColor
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitConfig
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitStatus
import space.be1ski.vibits.shared.feature.memos.domain.model.PostTags

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
        val lbl =
          if (raw.startsWith(PostTags.HABITS_PREFIX) || raw.startsWith(PostTags.HABIT_PREFIX)) {
            labelFromTag(tag)
          } else {
            raw
          }
        Triple(lbl, tag, DefaultHabitColor)
      }
      2 -> {
        val lbl = parts[0]
        val tag = normalizeHabitTag(parts[1])
        Triple(lbl, tag, DefaultHabitColor)
      }
      else -> {
        val lbl = parts[0]
        val tag = normalizeHabitTag(parts[1])
        val clr = parseHexColor(parts[2]) ?: DefaultHabitColor
        Triple(lbl, tag, clr)
      }
    }
  return HabitConfig(tag = tagRaw, label = label, color = color)
}

/**
 * Parses a hex color string to ARGB Long.
 * Supports formats: #RRGGBB or #AARRGGBB
 */
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
fun formatHexColor(color: Long): String {
  val rgb = color and 0xFFFFFFL
  return "#${rgb.toString(16).uppercase().padStart(6, '0')}"
}

/**
 * Normalizes a raw habit tag to the canonical PostTags.HABITS_PREFIX format.
 */
fun normalizeHabitTag(raw: String): String {
  val trimmed = raw.trim()
  val withoutPrefix = trimmed.removePrefix(PostTags.HABITS_PREFIX).removePrefix(PostTags.HABIT_PREFIX)
  val sanitized = withoutPrefix.replace("\\s+".toRegex(), "_")
  return "${PostTags.HABITS_PREFIX}$sanitized"
}

/**
 * Extracts a human-readable label from a habit tag.
 */
fun labelFromTag(tag: String): String = tag.removePrefix(PostTags.HABITS_PREFIX).removePrefix(PostTags.HABIT_PREFIX).replace('_', ' ')

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
 * Extracts all habit tags from content (excluding PostTags.HABITS_DAILY marker).
 */
fun extractHabitTagsFromContent(content: String?): Set<String> {
  if (content.isNullOrBlank()) {
    return emptySet()
  }
  val habitTagPattern = "${PostTags.HABITS_PREFIX}[^\\s]+"
  return Regex(habitTagPattern)
    .findAll(content)
    .map { it.value }
    .filterNot { it.equals(PostTags.HABITS_DAILY, ignoreCase = true) || it.startsWith(PostTags.HABITS_DAILY) }
    .toSet()
}

/**
 * Parses habit configurations from a memo's content.
 * Returns the list of habits defined in the memo, or empty list if not a config memo.
 */
fun parseConfigFromContent(content: String): List<HabitConfig> {
  if (!content.contains(PostTags.HABITS_CONFIG) && !content.contains(PostTags.HABITS_CONFIG_ALT)) {
    return emptyList()
  }
  return content
    .lineSequence()
    .map { it.trim() }
    .filter { it.isNotBlank() }
    .filterNot { it.startsWith(PostTags.HABITS_CONFIG) || it.startsWith(PostTags.HABITS_CONFIG_ALT) }
    .mapNotNull { line -> parseHabitConfigLine(line) }
    .distinctBy { it.tag }
    .toList()
}
