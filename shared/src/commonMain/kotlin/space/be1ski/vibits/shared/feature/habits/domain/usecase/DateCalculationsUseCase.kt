package space.be1ski.vibits.shared.feature.habits.domain.usecase

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo

private const val MONTHS_IN_QUARTER = 3
private const val FIRST_QUARTER_INDEX = 1

/**
 * Date calculation utilities for habits feature.
 */
class DateCalculationsUseCase {

  /**
   * Returns the Monday of the week containing [date].
   */
  fun startOfWeek(date: LocalDate): LocalDate {
    var start = date
    while (start.dayOfWeek != DayOfWeek.MONDAY) {
      start = start.minus(DatePeriod(days = 1))
    }
    return start
  }

  /**
   * Returns the quarter index (1-4) for a date.
   */
  fun quarterIndex(date: LocalDate): Int {
    return quarterIndex(date.month)
  }

  /**
   * Returns the quarter index (1-4) for a month.
   */
  fun quarterIndex(month: Month): Int {
    return month.ordinal / MONTHS_IN_QUARTER + FIRST_QUARTER_INDEX
  }

  /**
   * Returns the earliest memo date in the dataset.
   */
  fun earliestMemoDate(memos: List<Memo>, timeZone: TimeZone): LocalDate? {
    return memos.mapNotNull { memo ->
      ExtractDailyMemosUseCase.parseDailyDateFromContent(memo.content)
        ?: ExtractDailyMemosUseCase.parseMemoDate(memo, timeZone)
    }.minOrNull()
  }
}
