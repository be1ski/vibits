package space.be1ski.vibits.shared.core.platform

import kotlinx.datetime.LocalDate

/**
 * Returns the current local date for the active platform.
 */
expect fun currentLocalDate(): LocalDate
