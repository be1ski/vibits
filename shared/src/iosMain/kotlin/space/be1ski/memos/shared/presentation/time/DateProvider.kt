package space.be1ski.memos.shared.presentation.time

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDate

actual fun currentLocalDate(): LocalDate {
  val calendar = NSCalendar.currentCalendar
  val components = calendar.components(
    NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay,
    fromDate = NSDate()
  )
  val monthIndex = (components.month.toInt() - 1).coerceIn(0, 11)
  val month = Month.values()[monthIndex]
  return LocalDate(
    year = components.year.toInt(),
    month = month,
    day = components.day.toInt()
  )
}
