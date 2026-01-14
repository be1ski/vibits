package space.be1ski.memos.shared.presentation.memos

import space.be1ski.memos.shared.feature.memos.domain.model.Memo

/**
 * Actions for the Memos feature.
 */
sealed interface MemosAction {
  // Credentials
  data class UpdateBaseUrl(val value: String) : MemosAction
  data class UpdateToken(val value: String) : MemosAction
  data object EditCredentials : MemosAction

  // Loading
  data object LoadMemos : MemosAction
  data object LoadCachedMemos : MemosAction

  // CRUD
  data class CreateMemo(val content: String) : MemosAction
  data class UpdateMemo(val name: String, val content: String) : MemosAction
  data class DeleteMemo(val name: String) : MemosAction

  // Internal responses
  data class MemosLoaded(val memos: List<Memo>) : MemosAction
  data class CachedMemosLoaded(val memos: List<Memo>) : MemosAction
  data class MemoCreated(val memo: Memo) : MemosAction
  data class MemoUpdated(val memo: Memo) : MemosAction
  data class MemoDeleted(val name: String) : MemosAction
  data class OperationFailed(val error: String) : MemosAction
  data class CredentialsLoaded(val baseUrl: String, val token: String) : MemosAction
}

/**
 * State for the Memos feature.
 */
data class MemosState(
  val memos: List<Memo> = emptyList(),
  val isLoading: Boolean = false,
  val errorMessage: String? = null,
  val credentialsMode: Boolean = false,
  val baseUrl: String = "",
  val token: String = ""
) {
  val hasCredentials: Boolean get() = baseUrl.isNotBlank() && token.isNotBlank()
}

/**
 * Side effects for the Memos feature.
 */
sealed interface MemosEffect {
  data object LoadCachedMemos : MemosEffect
  data object LoadRemoteMemos : MemosEffect
  data class SaveCredentials(val baseUrl: String, val token: String) : MemosEffect
  data object LoadCredentials : MemosEffect
  data class CreateMemo(val content: String) : MemosEffect
  data class UpdateMemo(val name: String, val content: String) : MemosEffect
  data class DeleteMemo(val name: String) : MemosEffect
}
