package space.be1ski.memos.shared.presentation.time

import kotlinx.datetime.LocalDate
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDate

/**
 * iOS implementation using NSCalendar.
 */
actual fun currentLocalDate(): LocalDate {
  val calendar = NSCalendar.currentCalendar
  val components = calendar.components(
    NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay,
    fromDate = NSDate()
  )
  return LocalDate(
    year = components.year.toInt(),
    monthNumber = components.month.toInt(),
    dayOfMonth = components.day.toInt()
  )
}
