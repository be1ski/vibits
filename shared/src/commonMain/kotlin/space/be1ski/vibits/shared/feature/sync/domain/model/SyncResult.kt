package space.be1ski.vibits.shared.feature.sync.domain.model

import space.be1ski.vibits.shared.feature.memos.domain.model.Memo

/**
 * Result of a sync attempt.
 */
sealed interface SyncResult {
  /** Sync completed successfully. */
  data class Success(
    val syncedMemos: List<Memo>,
  ) : SyncResult

  /** Sync detected conflicts that need user resolution. */
  data class Conflict(
    val conflicts: List<SyncConflict>,
  ) : SyncResult

  /** Sync failed due to an error. */
  data class Error(
    val message: String,
    val exception: Throwable? = null,
  ) : SyncResult

  /** No credentials configured. */
  data object NoCredentials : SyncResult
}
