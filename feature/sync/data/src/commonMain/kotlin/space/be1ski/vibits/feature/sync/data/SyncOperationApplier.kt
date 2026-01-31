package space.be1ski.vibits.feature.sync.data

import space.be1ski.vibits.core.logging.Log
import space.be1ski.vibits.feature.memos.data.mapper.MemoMapper
import space.be1ski.vibits.feature.memos.data.remote.MemosApi
import space.be1ski.vibits.feature.sync.domain.SyncLogTags
import space.be1ski.vibits.feature.sync.domain.model.SyncOperation
import space.be1ski.vibits.feature.sync.domain.model.SyncOperationStatus
import space.be1ski.vibits.feature.sync.domain.model.SyncOperationType
import space.be1ski.vibits.feature.sync.domain.model.TempMemoName
import space.be1ski.vibits.feature.sync.domain.repository.SyncQueueRepository

private val TAG = SyncLogTags.SYNC_OPERATION_APPLIER

/**
 * Applies sync operations to the server.
 */
internal class SyncOperationApplier(
  private val memosApi: MemosApi,
  private val memoMapper: MemoMapper,
  private val syncQueue: SyncQueueRepository,
  private val offlineFirstRepository: OfflineFirstMemosRepository,
) {
  suspend fun applyOperations(
    operations: List<SyncOperation>,
    baseUrl: String,
    token: String,
  ) {
    operations.forEach { operation -> applyOperation(operation, baseUrl, token) }
  }

  private suspend fun applyOperation(
    operation: SyncOperation,
    baseUrl: String,
    token: String,
  ) {
    syncQueue.updateStatus(operation.id, SyncOperationStatus.IN_PROGRESS)

    runCatching {
      when (operation.type) {
        SyncOperationType.CREATE -> applyCreateOperation(operation, baseUrl, token)
        SyncOperationType.UPDATE -> applyUpdateOperation(operation, baseUrl, token)
        SyncOperationType.DELETE -> applyDeleteOperation(operation, baseUrl, token)
      }
    }.onSuccess { applied ->
      // Always mark as SYNCED after processing (whether applied or skipped)
      // This prevents operations getting stuck in IN_PROGRESS state
      syncQueue.updateStatus(operation.id, SyncOperationStatus.SYNCED)
      if (applied) {
        Log.d(TAG, "Synced operation: ${operation.id}")
      } else {
        Log.d(TAG, "Skipped operation (marked as synced): ${operation.id}")
      }
    }.onFailure { e ->
      syncQueue.updateStatus(operation.id, SyncOperationStatus.FAILED)
      Log.e(TAG, "Failed to sync operation: ${operation.id}", e)
      throw e
    }
  }

  private suspend fun applyCreateOperation(
    operation: SyncOperation,
    baseUrl: String,
    token: String,
  ): Boolean {
    val content = operation.content ?: return false
    val serverMemo = memoMapper.toDomain(memosApi.createMemo(baseUrl, token, content))

    operation.memoName?.let { tempName ->
      offlineFirstRepository.updateLocalMemo(tempName, serverMemo)
      syncQueue.updateMemoName(operation.id, serverMemo.name)
    }
    return true
  }

  private suspend fun applyUpdateOperation(
    operation: SyncOperation,
    baseUrl: String,
    token: String,
  ): Boolean {
    val name = operation.memoName
    val content = operation.content

    return when {
      name == null || content == null -> false
      TempMemoName.isTemporary(name) -> {
        Log.d(TAG, "Skipping update for temp memo: $name")
        false
      }
      else -> {
        memosApi.updateMemo(baseUrl, token, name, content)
        true
      }
    }
  }

  private suspend fun applyDeleteOperation(
    operation: SyncOperation,
    baseUrl: String,
    token: String,
  ): Boolean {
    val name = operation.memoName ?: return false
    memosApi.deleteMemo(baseUrl, token, name)
    return true
  }
}
