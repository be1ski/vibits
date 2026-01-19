package space.be1ski.vibits.shared.feature.memos.presentation

import space.be1ski.vibits.shared.feature.memos.domain.model.Memo

/**
 * Actions for the Memos feature.
 */
sealed interface MemosAction {
  // Credentials
  data class UpdateBaseUrl(
    val value: String,
  ) : MemosAction

  data class UpdateToken(
    val value: String,
  ) : MemosAction

  data object EditCredentials : MemosAction

  // Loading
  data object LoadMemos : MemosAction

  data object LoadCachedMemos : MemosAction

  // CRUD
  data class CreateMemo(
    val content: String,
  ) : MemosAction

  data class UpdateMemo(
    val name: String,
    val content: String,
  ) : MemosAction

  data class DeleteMemo(
    val name: String,
  ) : MemosAction

  // Internal responses
  data class MemosLoaded(
    val memos: List<Memo>,
  ) : MemosAction

  data class CachedMemosLoaded(
    val memos: List<Memo>,
  ) : MemosAction

  data class MemoCreated(
    val memo: Memo,
  ) : MemosAction

  data class MemoUpdated(
    val memo: Memo,
  ) : MemosAction

  data class MemoDeleted(
    val name: String,
  ) : MemosAction

  data class OperationFailed(
    val error: String,
  ) : MemosAction

  data class CredentialsLoaded(
    val baseUrl: String,
    val token: String,
  ) : MemosAction
}
