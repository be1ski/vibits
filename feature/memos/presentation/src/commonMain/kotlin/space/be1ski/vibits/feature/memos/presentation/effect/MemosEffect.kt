package space.be1ski.vibits.feature.memos.presentation.effect

sealed interface MemosEffect {
  sealed interface Credentials : MemosEffect

  sealed interface Load : MemosEffect

  sealed interface Write : MemosEffect

  sealed interface Sync : MemosEffect

  data object LoadCredentials : Credentials

  data class SaveCredentials(
    val baseUrl: String,
    val token: String,
  ) : Credentials

  data object LoadCachedMemos : Load

  data object LoadRemoteMemos : Load

  /** Refresh memos from cache, always updating state. */
  data object RefreshMemos : Load

  data class CreateMemo(
    val content: String,
  ) : Write

  data class UpdateMemo(
    val name: String,
    val content: String,
  ) : Write

  data class DeleteMemo(
    val name: String,
  ) : Write

  data object PerformSync : Sync

  /** Force sync with local changes overwriting server. */
  data object ForceLocalSync : Sync

  /** Force sync with server data overwriting local. */
  data object ForceServerSync : Sync

  data object LoadSyncStatus : Sync

  data object ObserveSyncStatus : Sync
}
