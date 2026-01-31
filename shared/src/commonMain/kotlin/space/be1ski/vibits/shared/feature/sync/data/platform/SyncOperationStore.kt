package space.be1ski.vibits.shared.feature.sync.data.platform

import kotlinx.coroutines.flow.Flow
import space.be1ski.vibits.shared.feature.sync.domain.model.SyncOperation
import space.be1ski.vibits.shared.feature.sync.domain.model.SyncOperationStatus

/**
 * Platform-specific storage for sync operations.
 */
@Suppress("TooManyFunctions")
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
  suspend fun updateStatus(id: String, status: SyncOperationStatus)

  /**
   * Updates the memo name of an operation.
   */
  suspend fun updateMemoName(id: String, memoName: String)

  /**
   * Removes an operation.
   */
  suspend fun removeOperation(id: String)

  /**
   * Clears all synced operations.
   */
  suspend fun clearSyncedOperations()

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
