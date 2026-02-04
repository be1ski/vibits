package space.be1ski.vibits.feature.habits.domain.model

import space.be1ski.vibits.feature.memos.domain.model.Memo

/**
 * Result of saving a daily habit memo.
 */
sealed interface SaveDailyMemoResult {
  data class Created(
    val memo: Memo,
  ) : SaveDailyMemoResult

  data class Updated(
    val memo: Memo,
  ) : SaveDailyMemoResult

  data class Deleted(
    val memoName: String,
  ) : SaveDailyMemoResult

  data class Error(
    val message: String,
    val exception: Throwable? = null,
  ) : SaveDailyMemoResult
}
