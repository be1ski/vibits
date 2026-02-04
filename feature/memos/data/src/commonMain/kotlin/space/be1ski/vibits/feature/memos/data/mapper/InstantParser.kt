package space.be1ski.vibits.feature.memos.data.mapper

import kotlin.time.Instant

private const val EPOCH_SECONDS_LENGTH = 10
private const val MILLIS_IN_SECOND = 1000L

/**
 * Parses a timestamp string into an [Instant].
 *
 * Supports the following formats:
 * - ISO 8601 format (e.g., "2023-01-15T10:30:00Z")
 * - ISO 8601 without timezone (adds "Z" suffix)
 * - Numeric timestamps (seconds or milliseconds since epoch)
 *
 * @param value The timestamp string to parse
 * @return The parsed [Instant], or null if parsing fails
 */
fun parseInstant(value: String?): Instant? {
  if (value.isNullOrBlank()) {
    return null
  }
  val trimmed = value.trim()
  return runCatching { Instant.parse(trimmed) }.getOrNull()
    ?: runCatching { Instant.parse("${trimmed}Z") }.getOrNull()
    ?: runCatching {
      val number = trimmed.toLong()
      val millis = if (trimmed.length > EPOCH_SECONDS_LENGTH) number else number * MILLIS_IN_SECOND
      Instant.fromEpochMilliseconds(millis)
    }.getOrNull()
}
