package space.be1ski.memos.shared.feature.habits.presentation.components

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

internal const val QUARTER_START_DAY_LIMIT = 7
internal const val MONTHS_IN_QUARTER = 3
internal const val FIRST_QUARTER_INDEX = 1

/**
 * Returns the quarter index (1-4) for a given date.
 */
internal fun quarterIndex(date: LocalDate): Int {
  return date.month.ordinal / MONTHS_IN_QUARTER + FIRST_QUARTER_INDEX
}

/**
 * Returns the quarter index (1-4) for a given month.
 */
internal fun quarterIndex(month: Month): Int {
  return month.ordinal / MONTHS_IN_QUARTER + FIRST_QUARTER_INDEX
}
