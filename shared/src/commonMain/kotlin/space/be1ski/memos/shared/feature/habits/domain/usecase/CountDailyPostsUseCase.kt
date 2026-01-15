package space.be1ski.memos.shared.feature.habits.domain.usecase

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import space.be1ski.memos.shared.feature.habits.domain.model.RangeBounds
import space.be1ski.memos.shared.feature.memos.domain.model.Memo

/**
 * Counts daily posts within a date range.
 */
class CountDailyPostsUseCase {

  operator fun invoke(
    memos: List<Memo>,
    timeZone: TimeZone,
    bounds: RangeBounds
  ): Map<LocalDate, Int> {
    val counts = mutableMapOf<LocalDate, Int>()
    memos.forEach { memo ->
      val date = ExtractDailyMemosUseCase.parseMemoDate(memo, timeZone) ?: return@forEach
      if (date !in bounds.start..bounds.end) {
        return@forEach
      }
      counts[date] = (counts[date] ?: 0) + 1
    }
    return counts
  }
}
