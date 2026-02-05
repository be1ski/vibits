package space.be1ski.vibits.feature.sync.domain

import space.be1ski.vibits.feature.sync.domain.model.SyncResult

interface SyncEngine {
  val isSyncing: Boolean

  suspend fun performSync(): SyncResult

  /** Forces server data to overwrite local data. */
  suspend fun forceServerSync(): SyncResult

  /** Forces local data to overwrite server data. */
  suspend fun forceLocalSync(): SyncResult
}
