package space.be1ski.vibits.feature.sync.data

import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import space.be1ski.vibits.core.platform.di.AppScope
import space.be1ski.vibits.core.utils.logging.Log
import space.be1ski.vibits.feature.auth.domain.model.isFilled
import space.be1ski.vibits.feature.auth.domain.repository.CredentialsRepository
import space.be1ski.vibits.feature.memos.domain.config.MemosDefaults
import space.be1ski.vibits.feature.memos.domain.model.Memo
import space.be1ski.vibits.feature.memos.domain.repository.MemosRemoteSource
import space.be1ski.vibits.feature.sync.domain.SyncEngine
import space.be1ski.vibits.feature.sync.domain.SyncLogTags
import space.be1ski.vibits.feature.sync.domain.model.SyncResult
import space.be1ski.vibits.feature.sync.domain.repository.SyncQueueRepository
import space.be1ski.vibits.feature.sync.domain.usecase.DetectSyncConflictsUseCase

private val TAG = SyncLogTags.SYNC_ENGINE
private const val LOG_CONTENT_PREVIEW_LENGTH = 50

@Inject
@SingleIn(AppScope::class)
class SyncEngineImpl(
  private val memosRemoteSource: MemosRemoteSource,
  private val credentialsRepository: CredentialsRepository,
  private val syncQueue: SyncQueueRepository,
  private val offlineFirstRepository: OfflineFirstMemosRepository,
) : SyncEngine {
  private val syncMutex = Mutex()
  private val _isSyncing = atomic(false)
  override val isSyncing: Boolean get() = _isSyncing.value

  private val operationApplier by lazy {
    SyncOperationApplier(memosRemoteSource, syncQueue, offlineFirstRepository)
  }

  override suspend fun performSync(): SyncResult = withSyncLock { performSyncInternal() }

  override suspend fun forceServerSync(): SyncResult = withSyncLock { forceServerSyncInternal() }

  override suspend fun forceLocalSync(): SyncResult = withSyncLock { forceLocalSyncInternal() }

  private suspend inline fun withSyncLock(crossinline block: suspend () -> SyncResult): SyncResult =
    syncMutex.withLock {
      _isSyncing.value = true
      try {
        block()
      } finally {
        _isSyncing.value = false
      }
    }

  private suspend fun performSyncInternal(): SyncResult {
    val credentials = credentialsRepository.load()
    if (!credentials.isFilled) {
      Log.w(TAG, "No credentials configured")
      return SyncResult.NoCredentials
    }

    // Reset any IN_PROGRESS operations from previous crash/kill
    syncQueue.resetInProgressToPending()

    return runCatching {
      Log.i(TAG, "Starting sync...")
      executeSyncFlow()
    }.getOrElse { e ->
      Log.e(TAG, "Sync failed", e)
      SyncResult.Error(e.message ?: "Sync failed", e)
    }
  }

  private suspend fun executeSyncFlow(): SyncResult {
    val serverMemos = fetchServerMemos()
    Log.d(TAG, "Fetched ${serverMemos.size} memos from server")

    val pendingOperations = syncQueue.getPendingOperations()
    if (pendingOperations.isNotEmpty()) {
      Log.d(TAG, "Found ${pendingOperations.size} pending operations:")
      pendingOperations.forEach { op ->
        val content = op.content?.take(LOG_CONTENT_PREVIEW_LENGTH)?.replace("\n", " ") ?: "null"
        Log.d(TAG, "  - ${op.type.name} '${op.memoName}': '$content...'")
      }
    } else {
      Log.d(TAG, "No pending operations")
    }

    if (pendingOperations.isEmpty()) {
      offlineFirstRepository.replaceAllMemos(serverMemos)
      return SyncResult.Success(serverMemos)
    }

    val localMemos = offlineFirstRepository.getCachedMemos()
    val conflicts = DetectSyncConflictsUseCase(pendingOperations, localMemos, serverMemos)

    return if (conflicts.isNotEmpty()) {
      Log.w(TAG, "Detected ${conflicts.size} conflicts:")
      conflicts.forEach { conflict ->
        val memoName = conflict.operation.memoName ?: "unknown"
        val opType = conflict.operation.type.name
        val conflictType = conflict.conflictType.name
        val localContent =
          conflict.localMemo
            ?.content
            ?.take(LOG_CONTENT_PREVIEW_LENGTH)
            ?.replace("\n", " ") ?: "null"
        val serverContent =
          conflict.serverMemo
            ?.content
            ?.take(LOG_CONTENT_PREVIEW_LENGTH)
            ?.replace("\n", " ") ?: "null"
        Log.w(
          TAG,
          "  - [$conflictType] $opType '$memoName': local='$localContent...', server='$serverContent...'",
        )
      }
      SyncResult.Conflict(conflicts)
    } else {
      operationApplier.applyOperations(pendingOperations)
      val updatedMemos = fetchServerMemos()
      offlineFirstRepository.replaceAllMemos(updatedMemos)
      syncQueue.clearOperations(syncedOnly = true)
      Log.i(TAG, "Sync completed successfully")
      SyncResult.Success(updatedMemos)
    }
  }

  private suspend fun forceServerSyncInternal(): SyncResult {
    val credentials = credentialsRepository.load()
    if (!credentials.isFilled) {
      return SyncResult.NoCredentials
    }

    return runCatching {
      Log.i(TAG, "Forcing server sync...")
      val serverMemos = fetchServerMemos()

      val pendingOperations = syncQueue.getPendingOperations()
      pendingOperations.forEach { syncQueue.removeOperation(it.id) }
      Log.d(TAG, "Discarded ${pendingOperations.size} pending operations")

      offlineFirstRepository.replaceAllMemos(serverMemos)
      Log.i(TAG, "Force server sync completed")
      SyncResult.Success(serverMemos)
    }.getOrElse { e ->
      Log.e(TAG, "Force server sync failed", e)
      SyncResult.Error(e.message ?: "Force sync failed", e)
    }
  }

  private suspend fun forceLocalSyncInternal(): SyncResult {
    val credentials = credentialsRepository.load()
    if (!credentials.isFilled) {
      return SyncResult.NoCredentials
    }

    return runCatching {
      Log.i(TAG, "Forcing local sync...")
      val pendingOperations = syncQueue.getPendingOperations()

      operationApplier.applyOperations(pendingOperations)
      val updatedMemos = fetchServerMemos()
      offlineFirstRepository.replaceAllMemos(updatedMemos)
      syncQueue.clearOperations(syncedOnly = true)

      Log.i(TAG, "Force local sync completed")
      SyncResult.Success(updatedMemos)
    }.getOrElse { e ->
      Log.e(TAG, "Force local sync failed", e)
      SyncResult.Error(e.message ?: "Force local sync failed", e)
    }
  }

  private suspend fun fetchServerMemos(): List<Memo> {
    val allMemos = mutableListOf<Memo>()
    var nextPageToken: String? = null

    do {
      val page =
        memosRemoteSource.listMemos(
          pageSize = MemosDefaults.DEFAULT_PAGE_SIZE,
          pageToken = nextPageToken,
        )
      allMemos += page.memos
      nextPageToken = page.nextPageToken
    } while (nextPageToken != null)

    return allMemos
  }
}
