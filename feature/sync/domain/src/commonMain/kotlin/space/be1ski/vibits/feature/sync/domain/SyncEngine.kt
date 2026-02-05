package space.be1ski.vibits.feature.sync.domain

import space.be1ski.vibits.feature.sync.domain.model.SyncResult

interface SyncEngine {
  val isSyncing: Boolean

  suspend fun performSync(): SyncResult

  suspend fun forceServerSync(): SyncResult

  suspend fun forceLocalSync(): SyncResult
}
