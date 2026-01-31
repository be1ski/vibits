package space.be1ski.vibits.shared.feature.sync.data.platform

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import space.be1ski.vibits.shared.feature.memos.data.internal.AndroidDatabaseHolder
import space.be1ski.vibits.shared.feature.sync.data.room.SyncOperationDao
import space.be1ski.vibits.shared.feature.sync.data.room.SyncOperationEntityMapper
import space.be1ski.vibits.shared.feature.sync.domain.model.SyncOperation
import space.be1ski.vibits.shared.feature.sync.domain.model.SyncOperationStatus

private const val DB_POLL_INTERVAL_MS = 100L

actual fun createSyncOperationStore(): SyncOperationStore = AndroidSyncOperationStore()

private class AndroidSyncOperationStore : SyncOperationStore {
  private fun daoOrNull(): SyncOperationDao? = AndroidDatabaseHolder.getDatabase()?.syncOperationDao()

  override suspend fun getPendingOperations(): List<SyncOperation> =
    daoOrNull()?.getPending()?.map(SyncOperationEntityMapper::toDomain) ?: emptyList()

  override suspend fun getAllOperations(): List<SyncOperation> =
    daoOrNull()?.getAll()?.map(SyncOperationEntityMapper::toDomain) ?: emptyList()

  override suspend fun upsertOperation(operation: SyncOperation) {
    daoOrNull()?.upsert(SyncOperationEntityMapper.toEntity(operation))
  }

  override suspend fun updateStatus(
    id: String,
    status: SyncOperationStatus,
  ) {
    daoOrNull()?.updateStatus(id, status.name)
  }

  override suspend fun updateMemoName(
    id: String,
    memoName: String,
  ) {
    daoOrNull()?.updateMemoName(id, memoName)
  }

  override suspend fun updateContent(
    id: String,
    content: String,
  ): Boolean = daoOrNull()?.updateContent(id, content)?.let { it > 0 } ?: false

  override suspend fun removeOperation(id: String) {
    daoOrNull()?.deleteById(id)
  }

  override suspend fun clearOperations(syncedOnly: Boolean) {
    if (syncedOnly) {
      daoOrNull()?.clearSynced()
    } else {
      daoOrNull()?.clearAll()
    }
  }

  override suspend fun resetInProgressToPending() {
    daoOrNull()?.resetInProgressToPending()
  }

  override fun observePendingCount(): Flow<Int> =
    flow {
      val dao = awaitDao()
      emitAll(dao.observePendingCount())
    }

  override fun observeFailedCount(): Flow<Int> =
    flow {
      val dao = awaitDao()
      emitAll(dao.observeFailedCount())
    }

  private suspend fun awaitDao(): SyncOperationDao {
    while (true) {
      daoOrNull()?.let { return it }
      delay(DB_POLL_INTERVAL_MS)
    }
  }

  override suspend fun getPendingCount(): Int = daoOrNull()?.getPendingCount() ?: 0

  override suspend fun getFailedCount(): Int = daoOrNull()?.getFailedCount() ?: 0
}
