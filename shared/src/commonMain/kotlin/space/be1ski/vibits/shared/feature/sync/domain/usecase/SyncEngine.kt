package space.be1ski.vibits.shared.feature.sync.domain.usecase

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import space.be1ski.vibits.shared.app.di.AppScope
import space.be1ski.vibits.shared.core.logging.Log
import space.be1ski.vibits.shared.feature.auth.domain.repository.CredentialsRepository
import space.be1ski.vibits.shared.feature.memos.data.mapper.MemoMapper
import space.be1ski.vibits.shared.feature.memos.data.remote.MemosApi
import space.be1ski.vibits.shared.feature.memos.domain.config.MemosDefaults
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.feature.sync.data.OfflineFirstMemosRepository
import space.be1ski.vibits.shared.feature.sync.domain.SyncLogTags
import space.be1ski.vibits.shared.feature.sync.domain.repository.SyncQueueRepository

private val TAG = SyncLogTags.SYNC_ENGINE
private const val LOG_CONTENT_PREVIEW_LENGTH = 50

/**
 * Result of a sync attempt.
 */
sealed interface SyncResult {
  /** Sync completed successfully. */
  data class Success(
    val syncedMemos: List<Memo>,
  ) : SyncResult

  /** Sync detected conflicts that need user resolution. */
  data class Conflict(
    val conflicts: List<space.be1ski.vibits.shared.feature.sync.domain.model.SyncConflict>,
  ) : SyncResult

  /** Sync failed due to an error. */
  data class Error(
    val message: String,
    val exception: Throwable? = null,
  ) : SyncResult

  /** No credentials configured. */
  data object NoCredentials : SyncResult
}

/**
 * Sync engine interface for processing pending operations and syncing with the server.
 */
interface SyncEngine {
  /** Whether a sync is currently in progress. */
  val isSyncing: Boolean

  /** Performs a full sync. */
  suspend fun performSync(): SyncResult

  /** Forces server data to overwrite local data. */
  suspend fun forceServerSync(): SyncResult

  /** Forces local data to overwrite server data. */
  suspend fun forceLocalSync(): SyncResult
}

/**
 * Thread-safe sync engine that processes pending operations and syncs with the server.
 * Only one sync operation can run at a time.
 */
@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class SyncEngineImpl(
  private val memosApi: MemosApi,
  private val memoMapper: MemoMapper,
  private val credentialsRepository: CredentialsRepository,
  private val syncQueue: SyncQueueRepository,
  private val offlineFirstRepository: OfflineFirstMemosRepository,
) : SyncEngine {
  private val syncMutex = Mutex()
  private val _isSyncing = atomic(false)
  override val isSyncing: Boolean get() = _isSyncing.value

  private val operationApplier by lazy {
    SyncOperationApplier(memosApi, memoMapper, syncQueue, offlineFirstRepository)
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
    if (credentials.baseUrl.isBlank() || credentials.token.isBlank()) {
      Log.w(TAG, "No credentials configured")
      return SyncResult.NoCredentials
    }

    return runCatching {
      Log.i(TAG, "Starting sync...")
      executeSyncFlow(credentials.baseUrl.trim(), credentials.token.trim())
    }.getOrElse { e ->
      Log.e(TAG, "Sync failed", e)
      SyncResult.Error(e.message ?: "Sync failed", e)
    }
  }

  private suspend fun executeSyncFlow(
    baseUrl: String,
    token: String,
  ): SyncResult {
    val serverMemos = fetchServerMemos(baseUrl, token)
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
    val conflicts = SyncConflictDetector.detectConflicts(pendingOperations, localMemos, serverMemos)

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
      operationApplier.applyOperations(pendingOperations, baseUrl, token)
      val updatedMemos = fetchServerMemos(baseUrl, token)
      offlineFirstRepository.replaceAllMemos(updatedMemos)
      syncQueue.clearSyncedOperations()
      Log.i(TAG, "Sync completed successfully")
      SyncResult.Success(updatedMemos)
    }
  }

  private suspend fun forceServerSyncInternal(): SyncResult {
    val credentials = credentialsRepository.load()
    if (credentials.baseUrl.isBlank() || credentials.token.isBlank()) {
      return SyncResult.NoCredentials
    }

    return runCatching {
      Log.i(TAG, "Forcing server sync...")
      val serverMemos = fetchServerMemos(credentials.baseUrl.trim(), credentials.token.trim())

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
    if (credentials.baseUrl.isBlank() || credentials.token.isBlank()) {
      return SyncResult.NoCredentials
    }

    val baseUrl = credentials.baseUrl.trim()
    val token = credentials.token.trim()

    return runCatching {
      Log.i(TAG, "Forcing local sync...")
      val pendingOperations = syncQueue.getPendingOperations()

      operationApplier.applyOperations(pendingOperations, baseUrl, token)
      val updatedMemos = fetchServerMemos(baseUrl, token)
      offlineFirstRepository.replaceAllMemos(updatedMemos)
      syncQueue.clearSyncedOperations()

      Log.i(TAG, "Force local sync completed")
      SyncResult.Success(updatedMemos)
    }.getOrElse { e ->
      Log.e(TAG, "Force local sync failed", e)
      SyncResult.Error(e.message ?: "Force local sync failed", e)
    }
  }

  private suspend fun fetchServerMemos(
    baseUrl: String,
    token: String,
  ): List<Memo> {
    val allMemos = mutableListOf<Memo>()
    var nextPageToken: String? = null

    do {
      val response =
        memosApi.listMemos(
          baseUrl = baseUrl,
          token = token,
          pageSize = MemosDefaults.DEFAULT_PAGE_SIZE,
          pageToken = nextPageToken,
        )
      allMemos += memoMapper.toDomainList(response.memos)
      nextPageToken = response.nextPageToken?.takeIf { it.isNotBlank() }
    } while (nextPageToken != null)

    return allMemos
  }
}
