package space.be1ski.vibits.feature.sync.data.platform

import kotlinx.coroutines.flow.Flow
import space.be1ski.vibits.feature.memos.data.internal.DesktopDatabaseHolder
import space.be1ski.vibits.feature.sync.data.room.SyncOperationEntityMapper
import space.be1ski.vibits.feature.sync.domain.model.SyncOperation
import space.be1ski.vibits.feature.sync.domain.model.SyncOperationStatus

actual fun createSyncOperationStore(): SyncOperationStore = DesktopSyncOperationStore()

private class DesktopSyncOperationStore : SyncOperationStore {
  private val dao get() = DesktopDatabaseHolder.database.syncOperationDao()

  override suspend fun getPendingOperations(): List<SyncOperation> = dao.getPending().map(SyncOperationEntityMapper::toDomain)

  override suspend fun getAllOperations(): List<SyncOperation> = dao.getAll().map(SyncOperationEntityMapper::toDomain)

  override suspend fun upsertOperation(operation: SyncOperation) {
    dao.upsert(SyncOperationEntityMapper.toEntity(operation))
  }

  override suspend fun updateStatus(
    id: String,
    status: SyncOperationStatus,
  ) {
    dao.updateStatus(id, status.name)
  }

  override suspend fun updateMemoName(
    id: String,
    memoName: String,
  ) {
    dao.updateMemoName(id, memoName)
  }

  override suspend fun updateContent(
    id: String,
    content: String,
  ): Boolean = dao.updateContent(id, content) > 0

  override suspend fun removeOperation(id: String) {
    dao.deleteById(id)
  }

  override suspend fun clearOperations(syncedOnly: Boolean) {
    if (syncedOnly) {
      dao.clearSynced()
    } else {
      dao.clearAll()
    }
  }

  override suspend fun resetInProgressToPending() {
    dao.resetInProgressToPending()
  }

  override fun observePendingCount(): Flow<Int> = dao.observePendingCount()

  override fun observeFailedCount(): Flow<Int> = dao.observeFailedCount()

  override suspend fun getPendingCount(): Int = dao.getPendingCount()

  override suspend fun getFailedCount(): Int = dao.getFailedCount()
}
