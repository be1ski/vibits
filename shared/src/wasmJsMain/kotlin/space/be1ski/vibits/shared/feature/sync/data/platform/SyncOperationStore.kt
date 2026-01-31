package space.be1ski.vibits.shared.feature.sync.data.platform

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import space.be1ski.vibits.shared.feature.sync.domain.model.SyncOperation
import space.be1ski.vibits.shared.feature.sync.domain.model.SyncOperationStatus

actual fun createSyncOperationStore(): SyncOperationStore = WasmSyncOperationStore()

@Suppress("TooManyFunctions")
private class WasmSyncOperationStore : SyncOperationStore {
  override suspend fun getPendingOperations(): List<SyncOperation> = emptyList()

  override suspend fun getAllOperations(): List<SyncOperation> = emptyList()

  override suspend fun upsertOperation(operation: SyncOperation) = Unit

  override suspend fun updateStatus(
    id: String,
    status: SyncOperationStatus,
  ) = Unit

  override suspend fun updateMemoName(
    id: String,
    memoName: String,
  ) = Unit

  override suspend fun removeOperation(id: String) = Unit

  override suspend fun clearSyncedOperations() = Unit

  override fun observePendingCount(): Flow<Int> = flowOf(0)

  override fun observeFailedCount(): Flow<Int> = flowOf(0)

  override suspend fun getPendingCount(): Int = 0

  override suspend fun getFailedCount(): Int = 0
}
