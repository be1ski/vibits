package space.be1ski.vibits.shared.feature.sync.data.platform

import kotlinx.coroutines.flow.Flow
import space.be1ski.vibits.shared.feature.memos.data.internal.IosDatabaseHolder
import space.be1ski.vibits.shared.feature.sync.data.room.SyncOperationEntityMapper
import space.be1ski.vibits.shared.feature.sync.domain.model.SyncOperation
import space.be1ski.vibits.shared.feature.sync.domain.model.SyncOperationStatus

actual fun createSyncOperationStore(): SyncOperationStore = IosSyncOperationStore()

private class IosSyncOperationStore : SyncOperationStore {
  private val dao get() = IosDatabaseHolder.database.syncOperationDao()

  override suspend fun getPendingOperations(): List<SyncOperation> =
    dao.getPending().map(SyncOperationEntityMapper::toDomain)

  override suspend fun getAllOperations(): List<SyncOperation> =
    dao.getAll().map(SyncOperationEntityMapper::toDomain)

  override suspend fun upsertOperation(operation: SyncOperation) {
    dao.upsert(SyncOperationEntityMapper.toEntity(operation))
  }

  override suspend fun updateStatus(id: String, status: SyncOperationStatus) {
    dao.updateStatus(id, status.name)
  }

  override suspend fun updateMemoName(id: String, memoName: String) {
    dao.updateMemoName(id, memoName)
  }

  override suspend fun removeOperation(id: String) {
    dao.deleteById(id)
  }

  override suspend fun clearSyncedOperations() {
    dao.clearSynced()
  }

  override fun observePendingCount(): Flow<Int> = dao.observePendingCount()

  override fun observeFailedCount(): Flow<Int> = dao.observeFailedCount()

  override suspend fun getPendingCount(): Int = dao.getPendingCount()

  override suspend fun getFailedCount(): Int = dao.getFailedCount()
}
