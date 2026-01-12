package space.be1ski.memos.shared.presentation.time

import kotlinx.datetime.LocalDate

/**
 * Returns the current local date for the active platform.
 */
expect fun currentLocalDate(): LocalDate
