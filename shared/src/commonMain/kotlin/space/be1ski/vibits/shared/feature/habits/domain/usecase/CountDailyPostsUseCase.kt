package space.be1ski.vibits.shared.feature.habits.domain.usecase

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import space.be1ski.vibits.shared.feature.habits.domain.model.RangeBounds
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo

/**
 * Counts daily posts within a date range.
 * Excludes memos with #habits hashtag (habit tracking memos).
 */
class CountDailyPostsUseCase {
  operator fun invoke(
    memos: List<Memo>,
    timeZone: TimeZone,
    bounds: RangeBounds,
  ): Map<LocalDate, Int> {
    val counts = mutableMapOf<LocalDate, Int>()
    memos.forEach { memo ->
      // Skip habit tracking memos
      if (memo.content.contains(HABITS_HASHTAG)) {
        return@forEach
      }
      val date = ExtractDailyMemosUseCase.parseMemoDate(memo, timeZone) ?: return@forEach
      if (date !in bounds.start..bounds.end) {
        return@forEach
      }
      counts[date] = (counts[date] ?: 0) + 1
    }
    return counts
  }

  companion object {
    private const val HABITS_HASHTAG = "#habits"

    /**
     * Filters out habit tracking memos from a list.
     * Returns only regular posts (memos without #habits hashtag).
     */
    fun filterPosts(memos: List<Memo>): List<Memo> = memos.filter { !it.content.contains(HABITS_HASHTAG) }
  }
}
