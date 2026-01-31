package space.be1ski.vibits.feature.habits.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month as CalendarMonth

/**
 * Range selection for activity charts.
 */
sealed class ActivityRange {
  /** Fixed calendar week starting on Monday. */
  data class Week(
    val startDate: LocalDate,
  ) : ActivityRange()

  /** Fixed calendar month. */
  data class Month(
    val year: Int,
    val month: CalendarMonth,
  ) : ActivityRange()

  /** Fixed calendar quarter. */
  data class Quarter(
    val year: Int,
    val index: Int,
  ) : ActivityRange()

  /** Fixed calendar year. */
  data class Year(
    val year: Int,
  ) : ActivityRange()
}
