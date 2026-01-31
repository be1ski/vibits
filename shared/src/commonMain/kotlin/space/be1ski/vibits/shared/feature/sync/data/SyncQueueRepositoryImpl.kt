package space.be1ski.vibits.shared.feature.sync.data

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import space.be1ski.vibits.shared.app.di.AppScope
import space.be1ski.vibits.shared.feature.sync.data.platform.SyncOperationStore
import space.be1ski.vibits.shared.feature.sync.domain.model.SyncOperation
import space.be1ski.vibits.shared.feature.sync.domain.model.SyncOperationStatus
import space.be1ski.vibits.shared.feature.sync.domain.model.SyncStatus
import space.be1ski.vibits.shared.feature.sync.domain.repository.SyncQueueRepository

/**
 * Thread-safe implementation of SyncQueueRepository.
 * Uses a mutex to ensure thread-safe access to the underlying store.
 */
@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class SyncQueueRepositoryImpl(
  private val store: SyncOperationStore,
) : SyncQueueRepository {
  /** Mutex to ensure thread-safe operations on the sync queue. */
  private val mutex = Mutex()

  override suspend fun addOperation(operation: SyncOperation) =
    mutex.withLock {
      store.upsertOperation(operation)
    }

  override suspend fun getPendingOperations(): List<SyncOperation> =
    mutex.withLock {
      store.getPendingOperations()
    }

  override suspend fun getAllOperations(): List<SyncOperation> =
    mutex.withLock {
      store.getAllOperations()
    }

  override suspend fun updateStatus(
    id: String,
    status: SyncOperationStatus,
  ) = mutex.withLock {
    store.updateStatus(id, status)
  }

  override suspend fun updateMemoName(
    id: String,
    memoName: String,
  ) = mutex.withLock {
    store.updateMemoName(id, memoName)
  }

  override suspend fun removeOperation(id: String) =
    mutex.withLock {
      store.removeOperation(id)
    }

  override suspend fun clearOperations(syncedOnly: Boolean) =
    mutex.withLock {
      store.clearOperations(syncedOnly)
    }

  override suspend fun resetInProgressToPending() =
    mutex.withLock {
      store.resetInProgressToPending()
    }

  override fun observeSyncStatus(): Flow<SyncStatus> =
    combine(
      store.observePendingCount(),
      store.observeFailedCount(),
    ) { pending, failed ->
      SyncStatus(
        pendingCount = pending,
        failedCount = failed,
      )
    }

  override suspend fun getSyncStatus(): SyncStatus =
    mutex.withLock {
      SyncStatus(
        pendingCount = store.getPendingCount(),
        failedCount = store.getFailedCount(),
      )
    }
}
