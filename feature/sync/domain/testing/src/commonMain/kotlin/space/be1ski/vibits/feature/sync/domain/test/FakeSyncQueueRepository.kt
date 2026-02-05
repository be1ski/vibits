package space.be1ski.vibits.feature.sync.domain.test

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import space.be1ski.vibits.feature.sync.domain.model.SyncOperation
import space.be1ski.vibits.feature.sync.domain.model.SyncOperationStatus
import space.be1ski.vibits.feature.sync.domain.model.SyncStatus
import space.be1ski.vibits.feature.sync.domain.repository.SyncQueueRepository

@Suppress("TooManyFunctions")
class FakeSyncQueueRepository : SyncQueueRepository {
  val operations = mutableListOf<SyncOperation>()
  var syncStatus = SyncStatus(pendingCount = 0, failedCount = 0)

  override suspend fun addOperation(operation: SyncOperation) {
    operations.add(operation)
  }

  override suspend fun getPendingOperations(): List<SyncOperation> = operations.filter { it.status == SyncOperationStatus.PENDING }

  override suspend fun getAllOperations(): List<SyncOperation> = operations.toList()

  override suspend fun updateStatus(
    id: String,
    status: SyncOperationStatus,
  ) {
    val index = operations.indexOfFirst { it.id == id }
    if (index >= 0) {
      operations[index] = operations[index].copy(status = status)
    }
  }

  override suspend fun updateMemoName(
    id: String,
    memoName: String,
  ) {
    val index = operations.indexOfFirst { it.id == id }
    if (index >= 0) {
      operations[index] = operations[index].copy(memoName = memoName)
    }
  }

  override suspend fun updateContent(
    id: String,
    content: String,
  ): Boolean {
    val index = operations.indexOfFirst { it.id == id && it.status == SyncOperationStatus.PENDING }
    if (index >= 0) {
      operations[index] = operations[index].copy(content = content)
      return true
    }
    return false
  }

  override suspend fun removeOperation(id: String) {
    operations.removeAll { it.id == id }
  }

  override suspend fun clearOperations(syncedOnly: Boolean) {
    if (syncedOnly) {
      operations.removeAll { it.status == SyncOperationStatus.SYNCED }
    } else {
      operations.clear()
    }
  }

  override suspend fun resetInProgressToPending() {
    val updated =
      operations.map { op ->
        if (op.status == SyncOperationStatus.IN_PROGRESS) {
          op.copy(status = SyncOperationStatus.PENDING)
        } else {
          op
        }
      }
    operations.clear()
    operations.addAll(updated)
  }

  override fun observeSyncStatus(): Flow<SyncStatus> = flowOf(syncStatus)

  override suspend fun getSyncStatus(): SyncStatus = syncStatus
}
