package space.be1ski.vibits.feature.habits.domain.usecase

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import space.be1ski.vibits.core.platform.date.DAYS_IN_WEEK
import space.be1ski.vibits.core.platform.date.FIRST_DAY_OF_MONTH
import space.be1ski.vibits.core.platform.date.FIRST_MONTH_OF_YEAR
import space.be1ski.vibits.core.platform.date.LAST_DAY_OF_DECEMBER
import space.be1ski.vibits.core.platform.date.MONTHS_IN_QUARTER
import space.be1ski.vibits.core.platform.date.MONTHS_IN_YEAR
import space.be1ski.vibits.feature.habits.domain.model.ActivityRange
import space.be1ski.vibits.feature.memos.domain.model.Memo

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

  private fun rangeBounds(range: ActivityRange): Pair<LocalDate, LocalDate> =
    when (range) {
      is ActivityRange.Week -> {
        range.startDate to range.startDate.plus(DatePeriod(days = DAYS_IN_WEEK - 1))
      }
      is ActivityRange.Month -> {
        val start = LocalDate(range.year, range.month, FIRST_DAY_OF_MONTH)
        val nextMonth = start.plus(DatePeriod(months = 1))
        val end = nextMonth.plus(DatePeriod(days = -1))
        start to end
      }
      is ActivityRange.Quarter -> {
        val startMonth = (range.index - 1) * MONTHS_IN_QUARTER + FIRST_MONTH_OF_YEAR
        val start = LocalDate(range.year, startMonth, FIRST_DAY_OF_MONTH)
        val end = start.plus(DatePeriod(months = MONTHS_IN_QUARTER)).plus(DatePeriod(days = -1))
        start to end
      }
      is ActivityRange.Year -> {
        val start = LocalDate(range.year, FIRST_MONTH_OF_YEAR, FIRST_DAY_OF_MONTH)
        val end = LocalDate(range.year, MONTHS_IN_YEAR, LAST_DAY_OF_DECEMBER)
        start to end
      }
    }
}
