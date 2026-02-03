package space.be1ski.vibits.feature.sync.data.platform

import kotlinx.coroutines.flow.Flow
import space.be1ski.vibits.feature.sync.domain.model.SyncOperation
import space.be1ski.vibits.feature.sync.domain.model.SyncOperationStatus

/**
 * Platform-specific storage for sync operations.
 */
interface SyncOperationStore {
  /**
   * Returns all pending or failed operations.
   */
  suspend fun getPendingOperations(): List<SyncOperation>

  /**
   * Returns all operations.
   */
  suspend fun getAllOperations(): List<SyncOperation>

  /**
   * Adds or updates an operation.
   */
  suspend fun upsertOperation(operation: SyncOperation)

  /**
   * Updates the status of an operation.
   */
  suspend fun updateStatus(
    id: String,
    status: SyncOperationStatus,
  )

  /**
   * Updates the memo name of an operation.
   */
  suspend fun updateMemoName(
    id: String,
    memoName: String,
  )

  /**
   * Updates the content of a pending operation.
   * @return true if the operation was found and updated, false otherwise
   */
  suspend fun updateContent(
    id: String,
    content: String,
  ): Boolean

  /**
   * Removes an operation.
   */
  suspend fun removeOperation(id: String)

  /**
   * Clears operations from the store.
   * @param syncedOnly If true, only clears synced operations. If false, clears all operations.
   */
  suspend fun clearOperations(syncedOnly: Boolean = true)

  /**
   * Resets IN_PROGRESS operations back to PENDING.
   * Called at startup to recover from crashes during sync.
   */
  suspend fun resetInProgressToPending()

  /**
   * Observes the count of pending operations.
   */
  fun observePendingCount(): Flow<Int>

  /**
   * Observes the count of failed operations.
   */
  fun observeFailedCount(): Flow<Int>

  /**
   * Returns the count of pending operations.
   */
  suspend fun getPendingCount(): Int

  /**
   * Returns the count of failed operations.
   */
  suspend fun getFailedCount(): Int
}

/**
 * Creates a platform-specific sync operation store.
 */
expect fun createSyncOperationStore(): SyncOperationStore
