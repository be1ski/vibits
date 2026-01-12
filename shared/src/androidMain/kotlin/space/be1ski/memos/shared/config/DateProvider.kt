package space.be1ski.memos.shared.config

import kotlinx.datetime.LocalDate

/**
 * Android implementation using java.time.LocalDate.
 */
actual fun currentLocalDate(): LocalDate {
  val today = java.time.LocalDate.now()
  return LocalDate(today.year, today.monthValue, today.dayOfMonth)
}
