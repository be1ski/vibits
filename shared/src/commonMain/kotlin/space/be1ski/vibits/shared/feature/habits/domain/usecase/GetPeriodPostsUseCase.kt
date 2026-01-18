package space.be1ski.vibits.shared.feature.habits.domain.usecase

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import space.be1ski.vibits.shared.core.ui.ActivityRange
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo

private const val DAYS_IN_WEEK = 7
private const val MONTHS_PER_QUARTER = 3

/**
 * Returns posts (non-habit memos) for a given activity range.
 */
class GetPeriodPostsUseCase {
  operator fun invoke(
    memos: List<Memo>,
    range: ActivityRange,
    timeZone: TimeZone,
  ): List<Memo> {
    val (start, end) = rangeBounds(range)
    return FilterPostsUseCase(memos)
      .filter { memo ->
        val instant = memo.createTime ?: memo.updateTime ?: return@filter false
        val date = instant.toLocalDateTime(timeZone).date
        date in start..end
      }.sortedByDescending { it.createTime ?: it.updateTime }
  }

  @Suppress("MagicNumber")
  private fun rangeBounds(range: ActivityRange): Pair<LocalDate, LocalDate> =
    when (range) {
      is ActivityRange.Week -> {
        range.startDate to range.startDate.plus(DatePeriod(days = DAYS_IN_WEEK - 1))
      }
      is ActivityRange.Month -> {
        val start = LocalDate(range.year, range.month, 1)
        val nextMonth = start.plus(DatePeriod(months = 1))
        val end = nextMonth.plus(DatePeriod(days = -1))
        start to end
      }
      is ActivityRange.Quarter -> {
        val startMonth = (range.index - 1) * MONTHS_PER_QUARTER + 1
        val start = LocalDate(range.year, startMonth, 1)
        val end = start.plus(DatePeriod(months = MONTHS_PER_QUARTER)).plus(DatePeriod(days = -1))
        start to end
      }
      is ActivityRange.Year -> {
        val start = LocalDate(range.year, 1, 1)
        val end = LocalDate(range.year, 12, 31)
        start to end
      }
    }
}
