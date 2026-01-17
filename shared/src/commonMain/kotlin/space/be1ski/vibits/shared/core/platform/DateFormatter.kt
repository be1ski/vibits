package space.be1ski.vibits.shared.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import org.jetbrains.compose.resources.stringResource
import space.be1ski.vibits.shared.Res
import space.be1ski.vibits.shared.day_fri
import space.be1ski.vibits.shared.day_mon
import space.be1ski.vibits.shared.day_sat
import space.be1ski.vibits.shared.day_sun
import space.be1ski.vibits.shared.day_thu
import space.be1ski.vibits.shared.day_tue
import space.be1ski.vibits.shared.day_wed
import space.be1ski.vibits.shared.month_apr
import space.be1ski.vibits.shared.month_aug
import space.be1ski.vibits.shared.month_dec
import space.be1ski.vibits.shared.month_feb
import space.be1ski.vibits.shared.month_jan
import space.be1ski.vibits.shared.month_jul
import space.be1ski.vibits.shared.month_jun
import space.be1ski.vibits.shared.month_mar
import space.be1ski.vibits.shared.month_may
import space.be1ski.vibits.shared.month_nov
import space.be1ski.vibits.shared.month_oct
import space.be1ski.vibits.shared.month_sep

/**
 * CompositionLocal for accessing DateFormatter with localized strings.
 */
val LocalDateFormatter = compositionLocalOf { DateFormatter() }

/**
 * Provides DateFormatter with localized strings to the composition.
 */
@Composable
fun ProvideDateFormatter(content: @Composable () -> Unit) {
  val formatter = rememberDateFormatter()
  CompositionLocalProvider(LocalDateFormatter provides formatter) {
    content()
  }
}

@Composable
private fun rememberDateFormatter(): DateFormatter {
  val monthsShort = listOf(
    stringResource(Res.string.month_jan),
    stringResource(Res.string.month_feb),
    stringResource(Res.string.month_mar),
    stringResource(Res.string.month_apr),
    stringResource(Res.string.month_may),
    stringResource(Res.string.month_jun),
    stringResource(Res.string.month_jul),
    stringResource(Res.string.month_aug),
    stringResource(Res.string.month_sep),
    stringResource(Res.string.month_oct),
    stringResource(Res.string.month_nov),
    stringResource(Res.string.month_dec)
  )
  val daysOfWeek = listOf(
    stringResource(Res.string.day_mon),
    stringResource(Res.string.day_tue),
    stringResource(Res.string.day_wed),
    stringResource(Res.string.day_thu),
    stringResource(Res.string.day_fri),
    stringResource(Res.string.day_sat),
    stringResource(Res.string.day_sun)
  )
  return remember(monthsShort, daysOfWeek) {
    DateFormatter(monthsShort, daysOfWeek)
  }
}

/**
 * Centralized date formatting with localized strings.
 */
class DateFormatter(
  private val monthsShort: List<String> = emptyList(),
  private val daysOfWeek: List<String> = DEFAULT_DAYS_OF_WEEK
) {

  /**
   * Short month name: "Jan", "Feb", "Mar"
   */
  fun monthShort(month: Month): String {
    return monthsShort.getOrNull(month.ordinal) ?: fallbackMonthShort(month)
  }

  /**
   * Single letter month initial: "J", "F", "M"
   */
  fun monthInitial(month: Month): String {
    return monthShort(month).take(1)
  }

  /**
   * Short day of week: "Mo", "Tu", "We"
   */
  fun dayOfWeekShort(day: DayOfWeek): String {
    return daysOfWeek.getOrNull(day.ordinal) ?: day.name.take(2)
  }

  /**
   * Month and day: "Jan 15"
   */
  fun monthDay(date: LocalDate): String {
    return "${monthShort(date.month)} ${date.day}"
  }

  /**
   * Full date with time: "2026-01-17 14:30"
   */
  fun dateTime(dateTime: LocalDateTime): String {
    val hour = dateTime.hour.toString().padStart(2, '0')
    val minute = dateTime.minute.toString().padStart(2, '0')
    return "${dateTime.date} $hour:$minute"
  }

  /**
   * Compact date with time: "17/1 14:30"
   */
  fun compactDateTime(dateTime: LocalDateTime): String {
    val minute = dateTime.minute.toString().padStart(2, '0')
    return "${dateTime.date.day}/${dateTime.date.month.ordinal + 1} ${dateTime.hour}:$minute"
  }

  /**
   * Week range label with smart year display:
   * - Current year: "Jan 8 - Jan 14"
   * - Past/future year (same): "Jan 8 - Jan 14 (2024)"
   * - Cross-year: "Dec 30, 2024 – Jan 5, 2025"
   */
  fun weekRange(start: LocalDate, end: LocalDate, currentYear: Int): String {
    val showYear = start.year != currentYear
    return if (!showYear) {
      "${monthDay(start)} - ${monthDay(end)}"
    } else if (start.year == end.year) {
      "${monthDay(start)} - ${monthDay(end)} (${end.year})"
    } else {
      "${monthDay(start)}, ${start.year} – ${monthDay(end)}, ${end.year}"
    }
  }

  companion object {
    private val DEFAULT_DAYS_OF_WEEK = listOf(
      "Mo", "Tu", "We", "Th", "Fr", "Sa", "Su"
    )

    private val defaultInstance = DateFormatter()

    // Static methods for non-Composable contexts (uses English defaults)
    fun monthShort(month: Month): String = defaultInstance.monthShort(month)
    fun monthInitial(month: Month): String = defaultInstance.monthInitial(month)
    fun dayOfWeekShort(day: DayOfWeek): String = defaultInstance.dayOfWeekShort(day)
    fun monthDay(date: LocalDate): String = defaultInstance.monthDay(date)
    fun dateTime(dateTime: LocalDateTime): String = defaultInstance.dateTime(dateTime)
    fun compactDateTime(dateTime: LocalDateTime): String = defaultInstance.compactDateTime(dateTime)
    fun weekRange(start: LocalDate, end: LocalDate, currentYear: Int): String =
      defaultInstance.weekRange(start, end, currentYear)

    private fun fallbackMonthShort(month: Month): String {
      return month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
    }
  }
}
