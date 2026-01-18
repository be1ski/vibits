package space.be1ski.vibits.shared.feature.habits.domain.usecase

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import space.be1ski.vibits.shared.feature.habits.domain.model.DailyMemoInfo
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo

/**
 * Extracts daily memos from a list of memos.
 * Daily memos are identified by #habits/daily or #daily tags.
 */
object ExtractDailyMemosUseCase {
  operator fun invoke(
    memos: List<Memo>,
    timeZone: TimeZone,
  ): Map<LocalDate, DailyMemoInfo> {
    val dailyMemos =
      memos.filter { memo ->
        memo.content.contains("#habits/daily") || memo.content.contains("#daily")
      }
    return dailyMemos
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
