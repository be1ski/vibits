package space.be1ski.vibits.shared.feature.sync.domain.repository

import kotlinx.coroutines.flow.Flow
import space.be1ski.vibits.shared.feature.sync.domain.model.SyncOperation
import space.be1ski.vibits.shared.feature.sync.domain.model.SyncOperationStatus
import space.be1ski.vibits.shared.feature.sync.domain.model.SyncStatus

/**
 * Repository for managing the sync operation queue.
 */
interface SyncQueueRepository {
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
  suspend fun updateStatus(id: String, status: SyncOperationStatus)

  /**
   * Updates the memo name after successful create (server assigns the name).
   */
  suspend fun updateMemoName(id: String, memoName: String)

  /**
   * Removes a synced operation from the queue.
   */
  suspend fun removeOperation(id: String)

  /**
   * Clears all synced operations.
   */
  suspend fun clearSyncedOperations()

  /**
   * Observes the current sync status.
   */
  fun observeSyncStatus(): Flow<SyncStatus>

  /**
   * Returns the current sync status.
   */
  suspend fun getSyncStatus(): SyncStatus
}
