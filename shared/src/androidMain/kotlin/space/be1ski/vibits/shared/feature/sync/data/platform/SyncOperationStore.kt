package space.be1ski.vibits.shared.feature.sync.data.platform

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import space.be1ski.vibits.shared.feature.memos.data.internal.AndroidDatabaseHolder
import space.be1ski.vibits.shared.feature.sync.data.room.SyncOperationDao
import space.be1ski.vibits.shared.feature.sync.data.room.SyncOperationEntityMapper
import space.be1ski.vibits.shared.feature.sync.domain.model.SyncOperation
import space.be1ski.vibits.shared.feature.sync.domain.model.SyncOperationStatus

actual fun createSyncOperationStore(): SyncOperationStore = AndroidSyncOperationStore()

@Suppress("TooManyFunctions")
private class AndroidSyncOperationStore : SyncOperationStore {
  private fun daoOrNull(): SyncOperationDao? = AndroidDatabaseHolder.getDatabase()?.syncOperationDao()

  override suspend fun getPendingOperations(): List<SyncOperation> =
    daoOrNull()?.getPending()?.map(SyncOperationEntityMapper::toDomain) ?: emptyList()

  override suspend fun getAllOperations(): List<SyncOperation> =
    daoOrNull()?.getAll()?.map(SyncOperationEntityMapper::toDomain) ?: emptyList()

  override suspend fun upsertOperation(operation: SyncOperation) {
    daoOrNull()?.upsert(SyncOperationEntityMapper.toEntity(operation))
  }

  override suspend fun updateStatus(id: String, status: SyncOperationStatus) {
    daoOrNull()?.updateStatus(id, status.name)
  }

  override suspend fun updateMemoName(id: String, memoName: String) {
    daoOrNull()?.updateMemoName(id, memoName)
  }

  override suspend fun removeOperation(id: String) {
    daoOrNull()?.deleteById(id)
  }

  override suspend fun clearSyncedOperations() {
    daoOrNull()?.clearSynced()
  }

  override fun observePendingCount(): Flow<Int> = daoOrNull()?.observePendingCount() ?: emptyFlow()

  override fun observeFailedCount(): Flow<Int> = daoOrNull()?.observeFailedCount() ?: emptyFlow()

  override suspend fun getPendingCount(): Int = daoOrNull()?.getPendingCount() ?: 0

  override suspend fun getFailedCount(): Int = daoOrNull()?.getFailedCount() ?: 0
}
