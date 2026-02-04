package space.be1ski.vibits.feature.habits.domain.usecase

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import space.be1ski.vibits.feature.habits.domain.model.RangeBounds
import space.be1ski.vibits.feature.memos.domain.model.Memo
import space.be1ski.vibits.feature.memos.domain.model.PostTags

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
      if (memo.content.contains(PostTags.HABITS_HASHTAG)) {
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
