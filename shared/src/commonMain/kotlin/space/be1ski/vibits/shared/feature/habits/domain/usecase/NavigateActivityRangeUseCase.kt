package space.be1ski.vibits.shared.feature.habits.domain.usecase

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import space.be1ski.vibits.shared.app.domain.model.ActivityRange

private const val DAYS_IN_WEEK = 7
private const val QUARTERS_IN_YEAR = 4

/**
 * Shifts an activity range by delta units (positive = forward, negative = backward).
 */
object NavigateActivityRangeUseCase {
  operator fun invoke(
    range: ActivityRange,
    delta: Int,
  ): ActivityRange =
    when (range) {
      is ActivityRange.Week ->
        range.copy(
          startDate = range.startDate.plus(DatePeriod(days = delta * DAYS_IN_WEEK)),
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

  private fun floorDiv(
    value: Int,
    divisor: Int,
  ): Int {
    var result = value / divisor
    if (value xor divisor < 0 && value % divisor != 0) {
      result -= 1
    }
    return result
  }

  private fun floorMod(
    value: Int,
    divisor: Int,
  ): Int {
    val mod = value % divisor
    return if (mod < 0) mod + divisor else mod
  }
}
