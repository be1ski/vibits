package space.be1ski.vibits.core.ui.date

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import org.jetbrains.compose.resources.stringResource
import space.be1ski.vibits.core.strings.generated.Res
import space.be1ski.vibits.core.strings.generated.day_fri
import space.be1ski.vibits.core.strings.generated.day_mon
import space.be1ski.vibits.core.strings.generated.day_sat
import space.be1ski.vibits.core.strings.generated.day_sun
import space.be1ski.vibits.core.strings.generated.day_thu
import space.be1ski.vibits.core.strings.generated.day_tue
import space.be1ski.vibits.core.strings.generated.day_wed
import space.be1ski.vibits.core.strings.generated.month_apr
import space.be1ski.vibits.core.strings.generated.month_aug
import space.be1ski.vibits.core.strings.generated.month_dec
import space.be1ski.vibits.core.strings.generated.month_feb
import space.be1ski.vibits.core.strings.generated.month_jan
import space.be1ski.vibits.core.strings.generated.month_jul
import space.be1ski.vibits.core.strings.generated.month_jun
import space.be1ski.vibits.core.strings.generated.month_mar
import space.be1ski.vibits.core.strings.generated.month_may
import space.be1ski.vibits.core.strings.generated.month_nov
import space.be1ski.vibits.core.strings.generated.month_oct
import space.be1ski.vibits.core.strings.generated.month_sep

private const val MONTH_FALLBACK_LENGTH = 3
private const val DAY_FALLBACK_LENGTH = 2

class DateFormatter(
  private val months: Map<Month, String>,
  private val days: Map<DayOfWeek, String>,
) {
  fun monthShort(month: Month): String = months[month] ?: month.name.take(MONTH_FALLBACK_LENGTH)

  fun dayOfWeekShort(day: DayOfWeek): String = days[day] ?: day.name.take(DAY_FALLBACK_LENGTH)

  fun monthInitial(month: Month): String = monthShort(month).take(1)

  fun monthDay(date: LocalDate): String = "${monthShort(date.month)} ${date.day}"

  fun monthDayYear(date: LocalDate): String = "${monthShort(date.month)} ${date.day}, ${date.year}"

  fun dateTime(dateTime: LocalDateTime): String {
    val hour = dateTime.hour.toString().padStart(2, '0')
    val minute = dateTime.minute.toString().padStart(2, '0')
    return "${dateTime.date} $hour:$minute"
  }

  fun compactDateTime(dateTime: LocalDateTime): String {
    val minute = dateTime.minute.toString().padStart(2, '0')
    return "${dateTime.date.day}/${dateTime.date.month.ordinal + 1} ${dateTime.hour}:$minute"
  }

  fun weekRange(
    start: LocalDate,
    end: LocalDate,
    currentYear: Int,
  ): String {
    val showYear = start.year != currentYear
    return if (!showYear) {
      "${monthDay(start)} - ${monthDay(end)}"
    } else if (start.year == end.year) {
      "${monthDay(start)} - ${monthDay(end)} (${end.year})"
    } else {
      "${monthDay(start)}, ${start.year} – ${monthDay(end)}, ${end.year}"
    }
  }
}

@Composable
fun rememberDateFormatter(): DateFormatter {
  val jan = stringResource(Res.string.month_jan)
  val feb = stringResource(Res.string.month_feb)
  val mar = stringResource(Res.string.month_mar)
  val apr = stringResource(Res.string.month_apr)
  val may = stringResource(Res.string.month_may)
  val jun = stringResource(Res.string.month_jun)
  val jul = stringResource(Res.string.month_jul)
  val aug = stringResource(Res.string.month_aug)
  val sep = stringResource(Res.string.month_sep)
  val oct = stringResource(Res.string.month_oct)
  val nov = stringResource(Res.string.month_nov)
  val dec = stringResource(Res.string.month_dec)

  val mon = stringResource(Res.string.day_mon)
  val tue = stringResource(Res.string.day_tue)
  val wed = stringResource(Res.string.day_wed)
  val thu = stringResource(Res.string.day_thu)
  val fri = stringResource(Res.string.day_fri)
  val sat = stringResource(Res.string.day_sat)
  val sun = stringResource(Res.string.day_sun)

  return remember(jan, feb, mar, apr, may, jun, jul, aug, sep, oct, nov, dec, mon, tue, wed, thu, fri, sat, sun) {
    DateFormatter(
      months =
        mapOf(
          Month.JANUARY to jan,
          Month.FEBRUARY to feb,
          Month.MARCH to mar,
          Month.APRIL to apr,
          Month.MAY to may,
          Month.JUNE to jun,
          Month.JULY to jul,
          Month.AUGUST to aug,
          Month.SEPTEMBER to sep,
          Month.OCTOBER to oct,
          Month.NOVEMBER to nov,
          Month.DECEMBER to dec,
        ),
      days =
        mapOf(
          DayOfWeek.MONDAY to mon,
          DayOfWeek.TUESDAY to tue,
          DayOfWeek.WEDNESDAY to wed,
          DayOfWeek.THURSDAY to thu,
          DayOfWeek.FRIDAY to fri,
          DayOfWeek.SATURDAY to sat,
          DayOfWeek.SUNDAY to sun,
        ),
    )
  }
}
