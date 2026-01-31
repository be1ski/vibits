package space.be1ski.vibits.shared.feature.sync.domain.repository

import kotlinx.coroutines.flow.Flow
import space.be1ski.vibits.shared.feature.sync.domain.model.SyncOperation
import space.be1ski.vibits.shared.feature.sync.domain.model.SyncOperationStatus
import space.be1ski.vibits.shared.feature.sync.domain.model.SyncStatus

/**
 * Provides sync status observation capabilities.
 */
interface SyncStatusProvider {
  /**
   * Observes the current sync status.
   */
  fun observeSyncStatus(): Flow<SyncStatus>

  /**
   * Returns the current sync status.
   */
  suspend fun getSyncStatus(): SyncStatus
}

/**
 * Repository for managing the sync operation queue.
 */
interface SyncQueueRepository : SyncStatusProvider {
  /**
   * Adds an operation to the sync queue.
   */
  suspend fun addOperation(operation: SyncOperation)

  /**
   * Returns all pending operations.
   */
  suspend fun getPendingOperations(): List<SyncOperation>

  /**
   * Returns all operations regardless of status.
   */
  suspend fun getAllOperations(): List<SyncOperation>

  /**
   * Updates the status of an operation.
   */
  suspend fun updateStatus(
    id: String,
    status: SyncOperationStatus,
  )

  /**
   * Updates the memo name after successful create (server assigns the name).
   */
  suspend fun updateMemoName(
    id: String,
    memoName: String,
  )

  /**
   * Removes a synced operation from the queue.
   */
  suspend fun removeOperation(id: String)

  /**
   * Clears operations from the queue.
   * @param syncedOnly If true, only clears synced operations. If false, clears all operations.
   */
  suspend fun clearOperations(syncedOnly: Boolean = true)

  /**
   * Resets IN_PROGRESS operations back to PENDING.
   * Called at startup to recover from crashes during sync.
   */
  suspend fun resetInProgressToPending()
}
