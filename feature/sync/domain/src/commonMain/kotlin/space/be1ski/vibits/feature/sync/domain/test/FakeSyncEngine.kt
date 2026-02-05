package space.be1ski.vibits.feature.sync.domain.test

import space.be1ski.vibits.feature.sync.domain.SyncEngine
import space.be1ski.vibits.feature.sync.domain.model.SyncResult

class FakeSyncEngine : SyncEngine {
  var performSyncResult: SyncResult = SyncResult.Success(emptyList())
  var forceLocalSyncResult: SyncResult = SyncResult.Success(emptyList())
  var forceServerSyncResult: SyncResult = SyncResult.Success(emptyList())
  override var isSyncing: Boolean = false

  override suspend fun performSync(): SyncResult = performSyncResult

  override suspend fun forceLocalSync(): SyncResult = forceLocalSyncResult

  override suspend fun forceServerSync(): SyncResult = forceServerSyncResult
}
