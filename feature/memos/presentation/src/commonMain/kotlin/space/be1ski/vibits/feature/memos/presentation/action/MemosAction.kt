package space.be1ski.vibits.feature.memos.presentation.action

import space.be1ski.vibits.core.elm.Action
import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.feature.memos.domain.model.Memo
import space.be1ski.vibits.feature.memos.domain.model.PostFilter
import space.be1ski.vibits.feature.sync.domain.model.SyncConflict
import space.be1ski.vibits.feature.sync.domain.model.SyncStatus

sealed interface MemosAction : Action {
  sealed interface Credentials : MemosAction {
    data class UpdateBaseUrl(
      val value: String,
    ) : Credentials

    data class UpdateToken(
      val value: String,
    ) : Credentials

    data object EditCredentials : Credentials

    data class CredentialsLoaded(
      val baseUrl: String,
      val token: String,
    ) : Credentials
  }

  sealed interface Loading : MemosAction {
    data object LoadMemos : Loading

    data object LoadCachedMemos : Loading

    /** Refresh memos from cache, always updating state (used after local changes). */
    data object RefreshMemos : Loading

    data class ResetForModeChange(
      val newMode: AppMode,
    ) : Loading

    data class ChangePostFilter(
      val filter: PostFilter,
    ) : Loading

    data class CachedMemosLoaded(
      val memos: List<Memo>,
    ) : Loading

    data class MemosLoaded(
      val memos: List<Memo>,
    ) : Loading
  }

  sealed interface Crud : MemosAction {
    data class CreateMemo(
      val content: String,
    ) : Crud

    data class UpdateMemo(
      val name: String,
      val content: String,
    ) : Crud

    data class DeleteMemo(
      val name: String,
    ) : Crud

    data class MemoCreated(
      val memo: Memo,
    ) : Crud

    data class MemoUpdated(
      val memo: Memo,
    ) : Crud

    data class MemoDeleted(
      val name: String,
    ) : Crud

    data class OperationFailed(
      val error: String,
    ) : Crud
  }

  sealed interface CreateDialog : MemosAction {
    data object ShowCreateDialog : CreateDialog

    data class UpdateCreateContent(
      val content: String,
    ) : CreateDialog

    data object DismissCreateDialog : CreateDialog

    data object ConfirmCreateDialog : CreateDialog
  }

  sealed interface EditDialog : MemosAction {
    data class ShowEditDialog(
      val memo: Memo,
    ) : EditDialog

    data class UpdateEditContent(
      val content: String,
    ) : EditDialog

    data object DismissEditDialog : EditDialog

    data object ConfirmEditDialog : EditDialog
  }

  sealed interface Sync : MemosAction {
    data object StartSync : Sync

    data class SyncCompleted(
      val memos: List<Memo>,
    ) : Sync

    data class SyncConflictDetected(
      val conflicts: List<SyncConflict>,
    ) : Sync

    data class SyncFailed(
      val error: String,
    ) : Sync

    data class SyncStatusUpdated(
      val status: SyncStatus,
    ) : Sync

    /** User chose to keep local changes. */
    data object ResolveKeepLocal : Sync

    /** User chose to keep server data. */
    data object ResolveKeepServer : Sync

    data object DismissConflictDialog : Sync

    data object ShowSyncLogDialog : Sync

    data object DismissSyncLogDialog : Sync
  }
}
