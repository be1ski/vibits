package space.be1ski.vibits.shared.feature.habits.domain.usecase

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import space.be1ski.vibits.shared.feature.habits.domain.model.RangeBounds
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo

private const val HABITS_HASHTAG = "#habits"

/**
 * Counts daily posts within a date range.
 * Excludes memos with #habits hashtag (habit tracking memos).
 */
object CountDailyPostsUseCase {
  operator fun invoke(
    memos: List<Memo>,
    timeZone: TimeZone,
    bounds: RangeBounds,
  ): Map<LocalDate, Int> {
    val counts = mutableMapOf<LocalDate, Int>()
    memos.forEach { memo ->
      if (memo.content.contains(HABITS_HASHTAG)) {
        return@forEach
      }
      val date = parseMemoDate(memo, timeZone) ?: return@forEach
      if (date !in bounds.start..bounds.end) {
        return@forEach
      }
      counts[date] = (counts[date] ?: 0) + 1
    }
    return counts
  }
}
