package space.be1ski.vibits.shared.feature.habits.domain.usecase

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import space.be1ski.vibits.shared.core.logging.Log
import space.be1ski.vibits.shared.feature.habits.domain.model.DailyMemoInfo
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.feature.memos.domain.model.PostTags

private const val TAG = "ExtractDailyMemos"

/**
 * Extracts daily memos from a list of memos.
 * Daily memos are identified by PostTags.HABITS_DAILY or PostTags.DAILY tags.
 */
object ExtractDailyMemosUseCase {
  operator fun invoke(
    memos: List<Memo>,
    timeZone: TimeZone,
  ): Map<LocalDate, DailyMemoInfo> {
    Log.d(TAG, "Extracting daily memos from ${memos.size} memos")
    val dailyMemos =
      memos.filter { memo ->
        memo.content.contains(PostTags.HABITS_DAILY) || memo.content.contains(PostTags.DAILY)
      }
    Log.d(TAG, "Found ${dailyMemos.size} daily memos")
    val result =
      dailyMemos
        .mapNotNull { memo ->
          val date =
            parseDailyDateFromContent(memo.content)
              ?: parseMemoDate(memo, timeZone)
              ?: return@mapNotNull null
          date to
            DailyMemoInfo(
              name = memo.name,
              content = memo.content,
            )
        }.toMap()
    Log.d(TAG, "Returning ${result.size} daily memo entries")
    return result
  }

  /**
   * Finds daily memo for a specific date.
   */
  fun forDate(
    memos: List<Memo>,
    timeZone: TimeZone,
    date: LocalDate,
  ): DailyMemoInfo? = invoke(memos, timeZone)[date]
}
