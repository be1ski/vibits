package space.be1ski.vibits.shared.feature.habits.domain.usecase

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import space.be1ski.vibits.shared.core.platform.currentLocalDate
import space.be1ski.vibits.shared.core.ui.ActivityRange
import kotlinx.datetime.Month

private const val DAYS_IN_WEEK = 7
private const val WEEK_END_OFFSET = 6
private const val QUARTERS_IN_YEAR = 4
private const val MONTHS_IN_YEAR = 12
private const val MONTH_ABBREV_LENGTH = 3

/**
 * Handles navigation and comparison of activity ranges.
 */
class NavigateActivityRangeUseCase {

  /**
   * Shifts an activity range by [delta] units (positive = forward, negative = backward).
   */
  operator fun invoke(range: ActivityRange, delta: Int): ActivityRange {
    return when (range) {
      is ActivityRange.Week -> range.copy(
        startDate = range.startDate.plus(DatePeriod(days = delta * DAYS_IN_WEEK))
      )
      is ActivityRange.Month -> {
        val start = LocalDate(range.year, range.month, 1)
        val shifted = start.plus(DatePeriod(months = delta))
        ActivityRange.Month(shifted.year, shifted.month)
      }
      is ActivityRange.Quarter -> {
        val zeroBased = range.index - 1 + delta
        val yearShift = floorDiv(zeroBased, QUARTERS_IN_YEAR)
        val quarterIndex = floorMod(zeroBased, QUARTERS_IN_YEAR) + 1
        ActivityRange.Quarter(range.year + yearShift, quarterIndex)
      }
      is ActivityRange.Year -> ActivityRange.Year(range.year + delta)
    }
  }

  /**
   * Formats an activity range to a human-readable label.
   */
  fun formatLabel(range: ActivityRange): String {
    return when (range) {
      is ActivityRange.Week -> {
        val endDate = range.startDate.plus(DatePeriod(days = WEEK_END_OFFSET))
        val currentYear = currentLocalDate().year
        val showYear = range.startDate.year != currentYear
        "${formatMonthDay(range.startDate, showYear)} - ${formatMonthDay(endDate, showYear)}"
      }
      is ActivityRange.Month -> "${monthShort(range.month)} ${range.year}"
      is ActivityRange.Quarter -> "Q${range.index} ${range.year}"
      is ActivityRange.Year -> range.year.toString()
    }
  }

  /**
   * Checks if [range] is before [other].
   */
  fun isBefore(range: ActivityRange, other: ActivityRange): Boolean {
    return when (range) {
      is ActivityRange.Week -> other is ActivityRange.Week &&
        range.startDate < other.startDate
      is ActivityRange.Month -> other is ActivityRange.Month &&
        compareYearMonth(range.year, range.month, other.year, other.month) < 0
      is ActivityRange.Quarter -> other is ActivityRange.Quarter &&
        compareYearQuarter(range.year, range.index, other.year, other.index) < 0
      is ActivityRange.Year -> other is ActivityRange.Year && range.year < other.year
    }
  }

  /**
   * Calculates the number of periods between [from] and [to].
   * Returns positive if [to] is after [from], negative otherwise.
   */
  fun calculateDelta(from: ActivityRange, to: ActivityRange): Int = when (from) {
    is ActivityRange.Week -> if (to is ActivityRange.Week) {
      (to.startDate.toEpochDays() - from.startDate.toEpochDays()).toInt() / DAYS_IN_WEEK
    } else 0
    is ActivityRange.Month -> if (to is ActivityRange.Month) {
      (to.year - from.year) * MONTHS_IN_YEAR + (to.month.ordinal - from.month.ordinal)
    } else 0
    is ActivityRange.Quarter -> if (to is ActivityRange.Quarter) {
      (to.year - from.year) * QUARTERS_IN_YEAR + (to.index - from.index)
    } else 0
    is ActivityRange.Year -> if (to is ActivityRange.Year) {
      to.year - from.year
    } else 0
  }

  private fun formatMonthDay(date: LocalDate, showYear: Boolean = false): String {
    val base = "${monthShort(date.month)} ${date.day}"
    return if (showYear) "$base ${date.year}" else base
  }

  private fun monthShort(month: Month): String {
    return month.name.take(MONTH_ABBREV_LENGTH).lowercase().replaceFirstChar { it.uppercase() }
  }

  private fun compareYearMonth(
    year: Int,
    month: Month,
    otherYear: Int,
    otherMonth: Month
  ): Int {
    return if (year != otherYear) year - otherYear else month.ordinal - otherMonth.ordinal
  }

  private fun compareYearQuarter(
    year: Int,
    quarter: Int,
    otherYear: Int,
    otherQuarter: Int
  ): Int {
    return if (year != otherYear) year - otherYear else quarter - otherQuarter
  }

  @Suppress("SameParameterValue") // Standard math utility, kept generic for clarity
  private fun floorDiv(value: Int, divisor: Int): Int {
    var result = value / divisor
    if (value xor divisor < 0 && value % divisor != 0) {
      result -= 1
    }
    return result
  }

  @Suppress("SameParameterValue") // Standard math utility, kept generic for clarity
  private fun floorMod(value: Int, divisor: Int): Int {
    val mod = value % divisor
    return if (mod < 0) mod + divisor else mod
  }
}
