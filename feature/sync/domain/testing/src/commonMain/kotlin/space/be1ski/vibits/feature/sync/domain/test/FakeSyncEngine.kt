package space.be1ski.vibits.feature.sync.domain.test

import space.be1ski.vibits.feature.sync.domain.SyncEngine
import space.be1ski.vibits.feature.sync.domain.model.SyncResult

class FakeSyncEngine : SyncEngine {
  var performSyncResult: SyncResult = SyncResult.Success(emptyList())
  var forceLocalSyncResult: SyncResult = SyncResult.Success(emptyList())
  var forceServerSyncResult: SyncResult = SyncResult.Success(emptyList())
  var performSyncCallCount: Int = 0
  var forceLocalSyncCallCount: Int = 0
  var forceServerSyncCallCount: Int = 0
  override var isSyncing: Boolean = false

  override suspend fun performSync(): SyncResult {
    performSyncCallCount += 1
    return performSyncResult
  }

  override suspend fun forceLocalSync(): SyncResult {
    forceLocalSyncCallCount += 1
    return forceLocalSyncResult
  }

  override suspend fun forceServerSync(): SyncResult {
    forceServerSyncCallCount += 1
    return forceServerSyncResult
  }
}
