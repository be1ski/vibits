package space.be1ski.vibits.shared.feature.habits.domain.usecase

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.feature.memos.domain.model.PostTags
import kotlin.time.Instant as KtInstant

private val DATE_REGEX = Regex("\\b(\\d{4}-\\d{2}-\\d{2})\\b")

/**
 * Parses date from daily memo content.
 * Looks for date pattern in memos with PostTags.HABITS_DAILY or PostTags.DAILY tags.
 */
fun parseDailyDateFromContent(content: String): LocalDate? {
  val match =
    if (content.contains(PostTags.HABITS_DAILY) || content.contains(PostTags.DAILY)) {
      DATE_REGEX.find(content)
    } else {
      null
    }
  val dateText = match?.groupValues?.getOrNull(1)
  return dateText?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
}

/**
 * Parses date from memo timestamp.
 */
fun parseMemoDate(
  memo: Memo,
  timeZone: TimeZone,
): LocalDate? {
  val instant = parseMemoInstant(memo) ?: return null
  return instant.toLocalDateTime(timeZone).date
}

/**
 * Gets the timestamp from memo, preferring updateTime over createTime.
 */
fun parseMemoInstant(memo: Memo): KtInstant? = memo.updateTime ?: memo.createTime
