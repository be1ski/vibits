package space.be1ski.vibits.feature.memos.presentation.reducer

import space.be1ski.vibits.feature.habits.domain.usecase.parseDailyDateFromContent
import space.be1ski.vibits.feature.memos.domain.model.Memo

private const val MILLIS_PER_DAY = 86400000L

internal fun sortedMemos(memos: List<Memo>): List<Memo> {
  return memos.sortedByDescending { memo ->
    // For habit tracking posts, use the tracked date instead of creation date
    val trackingDate = parseDailyDateFromContent(memo.content)
    if (trackingDate != null) {
      // Convert LocalDate to epoch days, then to milliseconds
      // toEpochDays returns days since 1970-01-01
      trackingDate.toEpochDays() * MILLIS_PER_DAY
    } else {
      // For all other posts, use createTime or updateTime
      memo.createTime?.toEpochMilliseconds()
        ?: memo.updateTime?.toEpochMilliseconds()
        ?: Long.MIN_VALUE
    }
  }
}
