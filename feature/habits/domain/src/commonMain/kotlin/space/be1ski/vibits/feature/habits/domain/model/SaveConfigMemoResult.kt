package space.be1ski.vibits.feature.habits.domain.model

import space.be1ski.vibits.feature.memos.domain.model.Memo

sealed interface SaveConfigMemoResult {
  data class Created(
    val memo: Memo,
  ) : SaveConfigMemoResult

  data class Updated(
    val memo: Memo,
  ) : SaveConfigMemoResult

  data class Error(
    val message: String,
    val exception: Throwable? = null,
  ) : SaveConfigMemoResult
}
