package space.be1ski.vibits.feature.sync.data

import kotlinx.coroutines.delay
import space.be1ski.vibits.core.utils.coroutines.runSuspendCatching
import space.be1ski.vibits.core.utils.logging.Log
import space.be1ski.vibits.feature.memos.domain.repository.MemosRemoteSource
import space.be1ski.vibits.feature.sync.domain.SyncLogTags
import space.be1ski.vibits.feature.sync.domain.model.SyncOperation
import space.be1ski.vibits.feature.sync.domain.model.SyncOperationStatus
import space.be1ski.vibits.feature.sync.domain.model.SyncOperationType
import space.be1ski.vibits.feature.sync.domain.model.TempMemoName
import space.be1ski.vibits.feature.sync.domain.repository.SyncQueueRepository
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

private const val TAG = SyncLogTags.SYNC_OPERATION_APPLIER

private const val DEFAULT_MAX_RETRIES = 3
private val DEFAULT_INITIAL_DELAY = 500.milliseconds
private val DEFAULT_MAX_DELAY = 10.seconds

/**
 * Configuration for retry behavior.
 */
data class RetryConfig(
  val maxRetries: Int = DEFAULT_MAX_RETRIES,
  val initialDelay: Duration = DEFAULT_INITIAL_DELAY,
  val maxDelay: Duration = DEFAULT_MAX_DELAY,
)

/**
 * Applies sync operations to the server with automatic retry and exponential backoff.
 */
internal class SyncOperationApplier(
  private val memosRemoteSource: MemosRemoteSource,
  private val syncQueue: SyncQueueRepository,
  private val offlineFirstRepository: OfflineFirstMemosRepository,
  private val retryConfig: RetryConfig = RetryConfig(),
) {
  suspend fun applyOperations(operations: List<SyncOperation>) {
    operations.forEach { operation -> applyOperation(operation) }
  }

  private suspend fun applyOperation(operation: SyncOperation) {
    syncQueue.updateStatus(operation.id, SyncOperationStatus.IN_PROGRESS)

    var lastException: Exception? = null
    var currentDelay = retryConfig.initialDelay

    for (attempt in 0..retryConfig.maxRetries) {
      val result =
        runSuspendCatching {
          when (operation.type) {
            SyncOperationType.CREATE -> applyCreateOperation(operation)
            SyncOperationType.UPDATE -> applyUpdateOperation(operation)
            SyncOperationType.DELETE -> applyDeleteOperation(operation)
          }
        }

      if (result.isSuccess) {
        val applied = result.getOrThrow()
        syncQueue.updateStatus(operation.id, SyncOperationStatus.SYNCED)
        if (applied) {
          Log.d(TAG, "Synced operation: ${operation.id}")
        } else {
          Log.d(TAG, "Skipped operation (marked as synced): ${operation.id}")
        }
        return
      }

      lastException = result.exceptionOrNull() as? Exception
      if (attempt < retryConfig.maxRetries) {
        Log.w(TAG, "Operation ${operation.id} failed, retrying in $currentDelay (attempt ${attempt + 1}/${retryConfig.maxRetries})")
        delay(currentDelay)
        currentDelay = (currentDelay * 2).coerceAtMost(retryConfig.maxDelay)
      }
    }

    syncQueue.updateStatus(operation.id, SyncOperationStatus.FAILED)
    Log.e(TAG, "Failed to sync operation after ${retryConfig.maxRetries} retries: ${operation.id}", lastException)
    throw lastException ?: Exception("Unknown error syncing operation ${operation.id}")
  }

  private suspend fun applyCreateOperation(operation: SyncOperation): Boolean {
    val content = operation.content ?: return false
    val serverMemo = memosRemoteSource.createMemo(content)

    operation.memoName?.let { tempName ->
      offlineFirstRepository.updateLocalMemo(tempName, serverMemo)
      syncQueue.updateMemoName(operation.id, serverMemo.name)
    }
    return true
  }

  private suspend fun applyUpdateOperation(operation: SyncOperation): Boolean {
    val name = operation.memoName
    val content = operation.content

    return when {
      name == null || content == null -> false
      TempMemoName.isTemporary(name) -> {
        Log.d(TAG, "Skipping update for temp memo: $name")
        false
      }
      else -> {
        memosRemoteSource.updateMemo(name, content)
        true
      }
    }
  }

  private suspend fun applyDeleteOperation(operation: SyncOperation): Boolean {
    val name = operation.memoName ?: return false
    memosRemoteSource.deleteMemo(name)
    return true
  }
}
