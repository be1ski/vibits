package space.be1ski.memos.shared.presentation.components

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant as KtInstant
import space.be1ski.memos.shared.feature.habits.domain.model.DailyMemoInfo
import space.be1ski.memos.shared.feature.memos.domain.model.Memo

internal fun extractDailyMemos(
  memos: List<Memo>,
  timeZone: TimeZone
): Map<LocalDate, DailyMemoInfo> {
  val dailyMemos = memos.filter { memo ->
    memo.content.contains("#habits/daily") || memo.content.contains("#daily")
  }
  return dailyMemos.mapNotNull { memo ->
    val date = parseDailyDateFromContent(memo.content) ?: parseMemoDate(memo, timeZone) ?: return@mapNotNull null
    date to DailyMemoInfo(
      name = memo.name,
      content = memo.content
    )
  }.toMap()
}

internal fun findDailyMemoForDate(
  memos: List<Memo>,
  timeZone: TimeZone,
  date: LocalDate
): DailyMemoInfo? {
  val dailyMemos = extractDailyMemos(memos, timeZone)
  return dailyMemos[date]
}

internal fun parseDailyDateFromContent(content: String): LocalDate? {
  val match = if (content.contains("#habits/daily") || content.contains("#daily")) {
    Regex("\\b(\\d{4}-\\d{2}-\\d{2})\\b").find(content)
  } else {
    null
  }
  val dateText = match?.groupValues?.getOrNull(1)
  return dateText?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
}

internal fun extractDailyPostCounts(
  memos: List<Memo>,
  timeZone: TimeZone,
  bounds: RangeBounds
): Map<LocalDate, Int> {
  val counts = mutableMapOf<LocalDate, Int>()
  memos.forEach { memo ->
    val date = parseMemoDate(memo, timeZone) ?: return@forEach
    if (date < bounds.start || date > bounds.end) {
      return@forEach
    }
    counts[date] = (counts[date] ?: 0) + 1
  }
  return counts
}

internal fun parseMemoDate(memo: Memo, timeZone: TimeZone): LocalDate? {
  val instant = parseMemoInstant(memo) ?: return null
  return instant.toLocalDateTime(timeZone).date
}

internal fun parseMemoInstant(memo: Memo): KtInstant? {
  return memo.updateTime ?: memo.createTime
}
