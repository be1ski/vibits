package space.be1ski.vibits.shared.core.platform

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

/**
 * Returns the current local date for web builds.
 */
actual fun currentLocalDate(): LocalDate {
  return Clock.System.now()
    .toLocalDateTime(TimeZone.currentSystemDefault())
    .date
}
