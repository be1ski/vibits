package space.be1ski.vibits.feature.habits.domain.usecase

import space.be1ski.vibits.feature.habits.domain.model.ActivityRange

private const val DAYS_IN_WEEK = 7
private const val QUARTERS_IN_YEAR = 4
private const val MONTHS_IN_YEAR = 12

/**
 * Calculates the number of periods between two activity ranges.
 * Returns positive if [to] is after [from], negative otherwise.
 */
object CalculateActivityRangeDeltaUseCase {
  operator fun invoke(
    from: ActivityRange,
    to: ActivityRange,
  ): Int =
    when (from) {
      is ActivityRange.Week ->
        if (to is ActivityRange.Week) {
          (to.startDate.toEpochDays() - from.startDate.toEpochDays()).toInt() / DAYS_IN_WEEK
        } else {
          0
        }
      is ActivityRange.Month ->
        if (to is ActivityRange.Month) {
          (to.year - from.year) * MONTHS_IN_YEAR + (to.month.ordinal - from.month.ordinal)
        } else {
          0
        }
      is ActivityRange.Quarter ->
        if (to is ActivityRange.Quarter) {
          (to.year - from.year) * QUARTERS_IN_YEAR + (to.index - from.index)
        } else {
          0
        }
      is ActivityRange.Year ->
        if (to is ActivityRange.Year) {
          to.year - from.year
        } else {
          0
        }
    }
}
