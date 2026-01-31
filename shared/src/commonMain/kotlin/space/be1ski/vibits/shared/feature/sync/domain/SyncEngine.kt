package space.be1ski.vibits.shared.feature.sync.domain

import space.be1ski.vibits.shared.feature.sync.domain.model.SyncResult

/**
 * Sync engine interface for processing pending operations and syncing with the server.
 */
interface SyncEngine {
  /** Whether a sync is currently in progress. */
  val isSyncing: Boolean

  /** Performs a full sync. */
  suspend fun performSync(): SyncResult

  /** Forces server data to overwrite local data. */
  suspend fun forceServerSync(): SyncResult

  /** Forces local data to overwrite server data. */
  suspend fun forceLocalSync(): SyncResult
}
