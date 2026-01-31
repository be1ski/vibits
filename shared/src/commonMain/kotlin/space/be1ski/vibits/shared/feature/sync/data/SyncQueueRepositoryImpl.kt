package space.be1ski.vibits.shared.feature.sync.data

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import space.be1ski.vibits.shared.app.di.AppScope
import space.be1ski.vibits.shared.feature.sync.data.platform.SyncOperationStore
import space.be1ski.vibits.shared.feature.sync.domain.model.SyncOperation
import space.be1ski.vibits.shared.feature.sync.domain.model.SyncOperationStatus
import space.be1ski.vibits.shared.feature.sync.domain.model.SyncStatus
import space.be1ski.vibits.shared.feature.sync.domain.repository.SyncQueueRepository

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class SyncQueueRepositoryImpl(
  private val store: SyncOperationStore,
) : SyncQueueRepository {
  override suspend fun addOperation(operation: SyncOperation) {
    store.upsertOperation(operation)
  }

  override suspend fun getPendingOperations(): List<SyncOperation> = store.getPendingOperations()

  override suspend fun getAllOperations(): List<SyncOperation> = store.getAllOperations()

  override suspend fun updateStatus(id: String, status: SyncOperationStatus) {
    store.updateStatus(id, status)
  }

  override suspend fun updateMemoName(id: String, memoName: String) {
    store.updateMemoName(id, memoName)
  }

  override suspend fun removeOperation(id: String) {
    store.removeOperation(id)
  }

  override suspend fun clearSyncedOperations() {
    store.clearSyncedOperations()
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
    SyncStatus(
      pendingCount = store.getPendingCount(),
      failedCount = store.getFailedCount(),
    )
}
