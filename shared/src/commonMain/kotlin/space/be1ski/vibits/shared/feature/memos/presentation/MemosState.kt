package space.be1ski.vibits.shared.feature.memos.presentation

import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.feature.memos.domain.model.MemosContent
import space.be1ski.vibits.shared.feature.memos.domain.model.PostFilter

/**
 * State for the Memos feature containing both domain content and UI state.
 */
data class MemosState(
  val content: MemosContent = MemosContent(),
  val isLoading: Boolean = false,
  val errorMessage: String? = null,
  val credentialsMode: Boolean = false,
  // Create dialog
  val showCreateDialog: Boolean = false,
  val createDialogContent: String = "",
  // Edit dialog
  val showEditDialog: Boolean = false,
  val editDialogContent: String = "",
  val editDialogMemo: Memo? = null,
) {
  // Convenience accessors delegating to domain content
  val memos: List<Memo> get() = content.memos
  val baseUrl: String get() = content.baseUrl
  val token: String get() = content.token
  val isOfflineMode: Boolean get() = content.isOfflineMode
  val activePostFilter: PostFilter get() = content.activePostFilter
  val hasCredentials: Boolean get() = content.hasCredentials
  val needsCredentials: Boolean get() = content.needsCredentials
}
