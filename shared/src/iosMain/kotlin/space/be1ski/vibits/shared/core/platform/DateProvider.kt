package space.be1ski.vibits.shared.core.platform

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDate

actual fun currentLocalDate(): LocalDate {
  val calendar = NSCalendar.currentCalendar
  val components =
    calendar.components(
      NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay,
      fromDate = NSDate(),
    )
  val monthIndex = (components.month.toInt() - 1).coerceIn(0, MAX_MONTH_INDEX)
  val month = Month.entries[monthIndex]
  return LocalDate(
    year = components.year.toInt(),
    month = month,
    day = components.day.toInt(),
  )
}

private const val MAX_MONTH_INDEX = 11
