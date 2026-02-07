package space.be1ski.vibits.feature.habits.domain

import space.be1ski.vibits.feature.habits.domain.model.DEFAULT_HABIT_COLOR
import space.be1ski.vibits.feature.habits.domain.model.HabitConfig
import space.be1ski.vibits.feature.habits.domain.model.HabitStatus
import space.be1ski.vibits.feature.memos.domain.model.PostTags
import space.be1ski.vibits.feature.memos.domain.model.isConfigMemo
import space.be1ski.vibits.feature.memos.domain.model.isConfigTag
import space.be1ski.vibits.feature.memos.domain.model.stripHabitPrefixes

/** Regex for matching whitespace sequences (for tag normalization). */
private val WHITESPACE_REGEX = Regex("\\s+")

/** Regex for matching checkbox format: "- [x] habit" or "* [X] habit". */
private val CHECKBOX_REGEX = Regex("^\\s*[-*]\\s*\\[(x|X)\\]\\s+(.+)$")

/** Regex for extracting habit tags from content. */
private val HABIT_TAG_REGEX = Regex("${PostTags.HABITS_PREFIX}[^\\s]+")

// Hex color format constants
private const val HEX_RGB_LENGTH = 6
private const val HEX_ARGB_LENGTH = 8
private const val FULL_ALPHA_MASK = 0xFF000000L
private const val RGB_MASK = 0xFFFFFFL
private const val HEX_RADIX = 16

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
fun parseHexColor(hex: String): Long? {
  val clean = hex.trim().removePrefix("#")
  return when (clean.length) {
    HEX_RGB_LENGTH -> clean.toLongOrNull(HEX_RADIX)?.let { FULL_ALPHA_MASK or it }
    HEX_ARGB_LENGTH -> clean.toLongOrNull(HEX_RADIX)
    else -> null
  }
}

/**
 * Formats an ARGB Long color to hex string (#RRGGBB).
 */
fun formatHexColor(color: Long): String {
  val rgb = color and RGB_MASK
  return "#${rgb.toString(HEX_RADIX).uppercase().padStart(HEX_RGB_LENGTH, '0')}"
}

/**
 * Normalizes a raw habit tag to the canonical PostTags.HABITS_PREFIX format.
 */
fun normalizeHabitTag(raw: String): String {
  val trimmed = raw.trim()
  val withoutPrefix = trimmed.stripHabitPrefixes()
  val sanitized = withoutPrefix.replace(WHITESPACE_REGEX, "_")
  return "${PostTags.HABITS_PREFIX}$sanitized"
}

/**
 * Extracts a human-readable label from a habit tag.
 */
fun labelFromTag(tag: String): String = tag.stripHabitPrefixes().replace('_', ' ')

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
  var sawCheckbox = false
  lines.forEach { line ->
    val match = CHECKBOX_REGEX.find(line)
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
  return HABIT_TAG_REGEX
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
  if (!content.isConfigMemo()) {
    return emptyList()
  }
  return content
    .lineSequence()
    .map { it.trim() }
    .filter { it.isNotBlank() }
    .filterNot { it.isConfigTag() }
    .mapNotNull { line -> parseHabitConfigLine(line) }
    .distinctBy { it.tag }
    .toList()
}
