package space.be1ski.memos.shared.presentation.time

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

/**
 * Android implementation using java.time.LocalDate.
 */
actual fun currentLocalDate(): LocalDate {
  val today = java.time.LocalDate.now()
  val month = Month.entries[today.monthValue - 1]
  return LocalDate(year = today.year, month = month, day = today.dayOfMonth)
}
