package space.be1ski.vibits.shared.feature.memos.presentation.state

import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.feature.memos.domain.model.PostFilter
import space.be1ski.vibits.shared.feature.sync.domain.model.SyncConflict
import space.be1ski.vibits.shared.feature.sync.domain.model.SyncStatus

/**
 * State for the Memos feature.
 */
data class MemosState(
  val memos: List<Memo> = emptyList(),
  val memosRevision: Int = 0,
  val isLoading: Boolean = false,
  val errorMessage: String? = null,
  val credentialsMode: Boolean = false,
  val baseUrl: String = "",
  val token: String = "",
  val isOfflineMode: Boolean = false,
  val activePostFilter: PostFilter = PostFilter.ALL,
  val initialDataLoaded: Boolean = false,
  // Create dialog
  val showCreateDialog: Boolean = false,
  val createDialogContent: String = "",
  // Edit dialog
  val showEditDialog: Boolean = false,
  val editDialogContent: String = "",
  val editDialogMemo: Memo? = null,
  // Sync state
  val syncStatus: SyncStatus = SyncStatus(),
  val isSyncing: Boolean = false,
  val syncConflicts: List<SyncConflict> = emptyList(),
  val showConflictDialog: Boolean = false,
  val showSyncLogDialog: Boolean = false,
) {
  val hasCredentials: Boolean get() = baseUrl.isNotBlank() && token.isNotBlank()
  val needsCredentials: Boolean get() = !isOfflineMode && !hasCredentials
  val hasPendingSync: Boolean get() = syncStatus.hasPendingOperations
  val hasSyncConflicts: Boolean get() = syncConflicts.isNotEmpty()
}
