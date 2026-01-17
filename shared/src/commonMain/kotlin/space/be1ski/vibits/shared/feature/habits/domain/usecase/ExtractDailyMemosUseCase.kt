package space.be1ski.vibits.shared.feature.habits.domain.usecase

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import space.be1ski.vibits.shared.feature.habits.domain.model.DailyMemoInfo
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import kotlin.time.Instant as KtInstant

/**
 * Extracts daily memos from a list of memos.
 * Daily memos are identified by #habits/daily or #daily tags.
 */
class ExtractDailyMemosUseCase {
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

  companion object {
    private val DATE_REGEX = Regex("\\b(\\d{4}-\\d{2}-\\d{2})\\b")

    fun parseDailyDateFromContent(content: String): LocalDate? {
      val match =
        if (content.contains("#habits/daily") || content.contains("#daily")) {
          DATE_REGEX.find(content)
        } else {
          null
        }
      val dateText = match?.groupValues?.getOrNull(1)
      return dateText?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
    }

    fun parseMemoDate(
      memo: Memo,
      timeZone: TimeZone,
    ): LocalDate? {
      val instant = parseMemoInstant(memo) ?: return null
      return instant.toLocalDateTime(timeZone).date
    }

    fun parseMemoInstant(memo: Memo): KtInstant? = memo.updateTime ?: memo.createTime
  }
}
