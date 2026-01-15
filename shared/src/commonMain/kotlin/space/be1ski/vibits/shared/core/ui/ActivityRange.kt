package space.be1ski.vibits.shared.core.ui

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month as CalendarMonth

/**
 * Activity visualization modes.
 */
enum class ActivityMode {
  /** Habit completion based on #habits/daily + #habits/config. */
  Habits,
  /** Raw post count per day. */
  Posts
}

/**
 * Range selection for activity charts.
 */
sealed class ActivityRange {
  /** Fixed calendar week starting on Monday. */
  data class Week(val startDate: LocalDate) : ActivityRange()
  /** Fixed calendar month. */
  data class Month(val year: Int, val month: CalendarMonth) : ActivityRange()
  /** Fixed calendar quarter. */
  data class Quarter(val year: Int, val index: Int) : ActivityRange()
  /** Fixed calendar year. */
  data class Year(val year: Int) : ActivityRange()
}
