package space.be1ski.vibits.feature.memos.presentation.effect

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import space.be1ski.vibits.core.elm.EffectHandler
import space.be1ski.vibits.core.elm.actions
import space.be1ski.vibits.core.utils.logging.Log
import space.be1ski.vibits.feature.memos.presentation.action.MemosAction
import space.be1ski.vibits.feature.settings.domain.usecase.LoadSyncDebounceDurationUseCase
import space.be1ski.vibits.feature.sync.domain.SyncEngine
import space.be1ski.vibits.feature.sync.domain.SyncLogTags
import space.be1ski.vibits.feature.sync.domain.model.SyncResult
import space.be1ski.vibits.feature.sync.domain.repository.SyncQueueRepository

private const val TAG = SyncLogTags.MEMOS_SYNC_EFFECT

class MemosSyncEffectHandler(
  private val syncEngine: SyncEngine,
  private val syncQueueRepository: SyncQueueRepository,
  private val loadSyncDebounceDuration: LoadSyncDebounceDurationUseCase,
) : EffectHandler<MemosEffect.Sync, MemosAction> {
  private val debounceMutex = Mutex()
  private var latestPerformSyncRequestId = 0L

  override fun invoke(effect: MemosEffect.Sync): Flow<MemosAction> =
    when (effect) {
      is MemosEffect.PerformSync -> handlePerformSync()
      is MemosEffect.ForceLocalSync -> handleForceLocalSync()
      is MemosEffect.ForceServerSync -> handleForceServerSync()
      is MemosEffect.LoadSyncStatus -> handleLoadSyncStatus()
      is MemosEffect.ObserveSyncStatus -> handleObserveSyncStatus()
    }

  private fun handlePerformSync(): Flow<MemosAction> =
    actions {
      val requestId = registerPerformSyncRequest()
      delay(loadSyncDebounceDuration())
      if (!isLatestPerformSyncRequest(requestId)) {
        Log.d(TAG, "Skipping stale sync request")
        return@actions
      }

      Log.d(TAG, "Performing sync")
      emit(MemosAction.Sync.SyncStarted)
      when (val result = syncEngine.performSync()) {
        is SyncResult.Success -> {
          Log.i(TAG, "Sync completed: ${result.syncedMemos.size} memos")
          emit(MemosAction.Sync.SyncCompleted(result.syncedMemos))
        }
        is SyncResult.Conflict -> {
          Log.w(TAG, "Sync conflicts: ${result.conflicts.size}")
          emit(MemosAction.Sync.SyncConflictDetected(result.conflicts))
        }
        is SyncResult.Error -> {
          Log.e(TAG, "Sync error: ${result.message}", result.exception)
          emit(MemosAction.Sync.SyncFailed(result.message))
        }
        is SyncResult.NoCredentials -> {
          Log.w(TAG, "No credentials for sync")
          emit(MemosAction.Sync.SyncFailed("No credentials configured"))
        }
      }
    }

  private fun handleForceLocalSync(): Flow<MemosAction> =
    actions {
      Log.d(TAG, "Forcing local sync")
      when (val result = syncEngine.forceLocalSync()) {
        is SyncResult.Success -> {
          Log.i(TAG, "Force local sync completed")
          emit(MemosAction.Sync.SyncCompleted(result.syncedMemos))
        }
        is SyncResult.Error -> {
          Log.e(TAG, "Force local sync error: ${result.message}")
          emit(MemosAction.Sync.SyncFailed(result.message))
        }
        is SyncResult.Conflict -> {
          // Shouldn't happen with force sync
          emit(MemosAction.Sync.SyncFailed("Unexpected conflict during force sync"))
        }
        is SyncResult.NoCredentials -> {
          emit(MemosAction.Sync.SyncFailed("No credentials configured"))
        }
      }
    }

  private fun handleForceServerSync(): Flow<MemosAction> =
    actions {
      Log.d(TAG, "Forcing server sync")
      when (val result = syncEngine.forceServerSync()) {
        is SyncResult.Success -> {
          Log.i(TAG, "Force server sync completed")
          emit(MemosAction.Sync.SyncCompleted(result.syncedMemos))
        }
        is SyncResult.Error -> {
          Log.e(TAG, "Force server sync error: ${result.message}")
          emit(MemosAction.Sync.SyncFailed(result.message))
        }
        is SyncResult.Conflict -> {
          // Shouldn't happen with force sync
          emit(MemosAction.Sync.SyncFailed("Unexpected conflict during force sync"))
        }
        is SyncResult.NoCredentials -> {
          emit(MemosAction.Sync.SyncFailed("No credentials configured"))
        }
      }
    }

  private fun handleLoadSyncStatus(): Flow<MemosAction> =
    actions {
      val status = syncQueueRepository.getSyncStatus()
      emit(MemosAction.Sync.SyncStatusUpdated(status))
    }

  private fun handleObserveSyncStatus(): Flow<MemosAction> =
    syncQueueRepository.observeSyncStatus().map { status ->
      MemosAction.Sync.SyncStatusUpdated(status)
    }

  private suspend fun registerPerformSyncRequest(): Long =
    debounceMutex.withLock {
      latestPerformSyncRequestId += 1
      latestPerformSyncRequestId
    }

  private suspend fun isLatestPerformSyncRequest(requestId: Long): Boolean =
    debounceMutex.withLock {
      requestId == latestPerformSyncRequestId
    }
}
