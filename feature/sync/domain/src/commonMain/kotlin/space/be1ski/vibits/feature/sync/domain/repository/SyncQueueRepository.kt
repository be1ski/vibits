package space.be1ski.vibits.feature.sync.domain.repository

import kotlinx.coroutines.flow.Flow
import space.be1ski.vibits.feature.sync.domain.model.SyncOperation
import space.be1ski.vibits.feature.sync.domain.model.SyncOperationStatus
import space.be1ski.vibits.feature.sync.domain.model.SyncStatus

interface SyncStatusProvider {
  fun observeSyncStatus(): Flow<SyncStatus>

  suspend fun getSyncStatus(): SyncStatus
}

interface SyncQueueRepository : SyncStatusProvider {
  suspend fun addOperation(operation: SyncOperation)

  suspend fun getPendingOperations(): List<SyncOperation>

  suspend fun getAllOperations(): List<SyncOperation>

  suspend fun updateStatus(
    id: String,
    status: SyncOperationStatus,
  )

  suspend fun updateMemoName(
    id: String,
    memoName: String,
  )

  /**
   * Coalesces updates to temporary memos into the pending CREATE operation.
   * @return true if the operation was found and updated
   */
  suspend fun updateContent(
    id: String,
    content: String,
  ): Boolean

  suspend fun removeOperation(id: String)

  /**
   * @param syncedOnly If true, only clears synced operations. If false, clears all.
   */
  suspend fun clearOperations(syncedOnly: Boolean)

  /** Resets IN_PROGRESS operations back to PENDING to recover from crashes during sync. */
  suspend fun resetInProgressToPending()
}
