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
import space.be1ski.vibits.shared.feature.sync.domain.model.ConflictType
import space.be1ski.vibits.shared.feature.sync.domain.model.SyncConflict
import space.be1ski.vibits.shared.feature.sync.domain.model.SyncOperation
import space.be1ski.vibits.shared.feature.sync.domain.model.SyncOperationStatus
import space.be1ski.vibits.shared.feature.sync.domain.model.SyncOperationType
import space.be1ski.vibits.shared.feature.sync.domain.repository.SyncQueueRepository

private const val TAG = "SyncEngine"

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
    val conflicts: List<SyncConflict>,
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
  /** Mutex to prevent concurrent sync operations. */
  private val syncMutex = Mutex()

  /** Atomic flag indicating if sync is in progress. */
  private val _isSyncing = atomic(false)
  override val isSyncing: Boolean get() = _isSyncing.value

  /**
   * Performs a full sync:
   * 1. Fetches current server state
   * 2. Compares with pending operations
   * 3. Detects conflicts
   * 4. If no conflicts, applies pending operations to server
   * 5. Updates local state with server response
   *
   * Thread-safe: Only one sync can run at a time.
   */
  override suspend fun performSync(): SyncResult = syncMutex.withLock {
    _isSyncing.value = true
    try {
      performSyncInternal()
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

    val baseUrl = credentials.baseUrl.trim()
    val token = credentials.token.trim()

    return try {
      Log.i(TAG, "Starting sync...")

      // 1. Fetch server memos
      val serverMemos = fetchServerMemos(baseUrl, token)
      Log.d(TAG, "Fetched ${serverMemos.size} memos from server")

      // 2. Get pending operations
      val pendingOperations = syncQueue.getPendingOperations()
      Log.d(TAG, "Found ${pendingOperations.size} pending operations")

      if (pendingOperations.isEmpty()) {
        // No pending changes, just update local cache
        offlineFirstRepository.replaceAllMemos(serverMemos)
        return SyncResult.Success(serverMemos)
      }

      // 3. Check for conflicts
      val conflicts = detectConflicts(pendingOperations, serverMemos)
      if (conflicts.isNotEmpty()) {
        Log.w(TAG, "Detected ${conflicts.size} conflicts")
        return SyncResult.Conflict(conflicts)
      }

      // 4. Apply pending operations to server
      applyPendingOperations(pendingOperations, baseUrl, token)

      // 5. Fetch updated server state
      val updatedMemos = fetchServerMemos(baseUrl, token)
      offlineFirstRepository.replaceAllMemos(updatedMemos)

      // 6. Clear synced operations
      syncQueue.clearSyncedOperations()

      Log.i(TAG, "Sync completed successfully")
      SyncResult.Success(updatedMemos)
    } catch (e: Exception) {
      Log.e(TAG, "Sync failed", e)
      SyncResult.Error(e.message ?: "Sync failed", e)
    }
  }

  /**
   * Forces server data to overwrite local data.
   * Used when user chooses to resolve conflicts by keeping server data.
   *
   * Thread-safe: Only one sync can run at a time.
   */
  override suspend fun forceServerSync(): SyncResult = syncMutex.withLock {
    _isSyncing.value = true
    try {
      forceServerSyncInternal()
    } finally {
      _isSyncing.value = false
    }
  }

  private suspend fun forceServerSyncInternal(): SyncResult {
    val credentials = credentialsRepository.load()
    if (credentials.baseUrl.isBlank() || credentials.token.isBlank()) {
      return SyncResult.NoCredentials
    }

    return try {
      Log.i(TAG, "Forcing server sync...")

      // Fetch server memos
      val serverMemos = fetchServerMemos(credentials.baseUrl.trim(), credentials.token.trim())

      // Discard all pending operations
      val pendingOperations = syncQueue.getPendingOperations()
      pendingOperations.forEach { syncQueue.removeOperation(it.id) }
      Log.d(TAG, "Discarded ${pendingOperations.size} pending operations")

      // Replace local cache with server data
      offlineFirstRepository.replaceAllMemos(serverMemos)

      Log.i(TAG, "Force server sync completed")
      SyncResult.Success(serverMemos)
    } catch (e: Exception) {
      Log.e(TAG, "Force server sync failed", e)
      SyncResult.Error(e.message ?: "Force sync failed", e)
    }
  }

  /**
   * Forces local data to overwrite server data.
   * Used when user chooses to resolve conflicts by keeping local data.
   *
   * Thread-safe: Only one sync can run at a time.
   */
  override suspend fun forceLocalSync(): SyncResult = syncMutex.withLock {
    _isSyncing.value = true
    try {
      forceLocalSyncInternal()
    } finally {
      _isSyncing.value = false
    }
  }

  private suspend fun forceLocalSyncInternal(): SyncResult {
    val credentials = credentialsRepository.load()
    if (credentials.baseUrl.isBlank() || credentials.token.isBlank()) {
      return SyncResult.NoCredentials
    }

    val baseUrl = credentials.baseUrl.trim()
    val token = credentials.token.trim()

    return try {
      Log.i(TAG, "Forcing local sync...")

      // Get all pending operations
      val pendingOperations = syncQueue.getPendingOperations()

      // Apply all operations to server (ignore conflicts)
      applyPendingOperations(pendingOperations, baseUrl, token)

      // Fetch updated server state
      val updatedMemos = fetchServerMemos(baseUrl, token)
      offlineFirstRepository.replaceAllMemos(updatedMemos)

      // Clear synced operations
      syncQueue.clearSyncedOperations()

      Log.i(TAG, "Force local sync completed")
      SyncResult.Success(updatedMemos)
    } catch (e: Exception) {
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

  private suspend fun detectConflicts(
    pendingOperations: List<SyncOperation>,
    serverMemos: List<Memo>,
  ): List<SyncConflict> {
    val localMemos = offlineFirstRepository.getCachedMemos()
    val serverMemosByName = serverMemos.associateBy { it.name }
    val localMemosByName = localMemos.associateBy { it.name }
    val conflicts = mutableListOf<SyncConflict>()

    for (operation in pendingOperations) {
      val memoName = operation.memoName ?: continue

      when (operation.type) {
        SyncOperationType.CREATE -> {
          // Check if a memo with this name already exists on server
          // (shouldn't happen with temp names, but check anyway)
          if (!GenerateTempMemoNameUseCase.isTemporaryName(memoName)) {
            val serverMemo = serverMemosByName[memoName]
            if (serverMemo != null) {
              conflicts +=
                SyncConflict(
                  operation = operation,
                  localMemo = localMemosByName[memoName],
                  serverMemo = serverMemo,
                  conflictType = ConflictType.BOTH_MODIFIED,
                )
            }
          }
        }

        SyncOperationType.UPDATE -> {
          val serverMemo = serverMemosByName[memoName]
          val localMemo = localMemosByName[memoName]

          if (serverMemo == null) {
            // Server doesn't have this memo anymore
            conflicts +=
              SyncConflict(
                operation = operation,
                localMemo = localMemo,
                serverMemo = null,
                conflictType = ConflictType.DELETED_ON_SERVER,
              )
          } else if (localMemo != null) {
            // Check if server version is newer
            val serverUpdateTime = serverMemo.updateTime
            val localUpdateTime = localMemo.updateTime
            if (serverUpdateTime != null &&
              localUpdateTime != null &&
              serverUpdateTime > operation.createdAt
            ) {
              // Server was modified after our local change
              conflicts +=
                SyncConflict(
                  operation = operation,
                  localMemo = localMemo,
                  serverMemo = serverMemo,
                  conflictType = ConflictType.SERVER_NEWER,
                )
            }
          }
        }

        SyncOperationType.DELETE -> {
          val serverMemo = serverMemosByName[memoName]
          if (serverMemo != null) {
            // Check if server version was modified after our delete request
            val serverUpdateTime = serverMemo.updateTime
            if (serverUpdateTime != null && serverUpdateTime > operation.createdAt) {
              conflicts +=
                SyncConflict(
                  operation = operation,
                  localMemo = null,
                  serverMemo = serverMemo,
                  conflictType = ConflictType.SERVER_NEWER,
                )
            }
          }
          // If server doesn't have the memo, that's fine - nothing to delete
        }
      }
    }

    return conflicts
  }

  private suspend fun applyPendingOperations(
    operations: List<SyncOperation>,
    baseUrl: String,
    token: String,
  ) {
    for (operation in operations) {
      try {
        syncQueue.updateStatus(operation.id, SyncOperationStatus.IN_PROGRESS)

        when (operation.type) {
          SyncOperationType.CREATE -> {
            val content = operation.content ?: continue
            val serverMemo = memoMapper.toDomain(memosApi.createMemo(baseUrl, token, content))

            // Update local memo with server-assigned name
            operation.memoName?.let { tempName ->
              offlineFirstRepository.updateLocalMemo(tempName, serverMemo)
              syncQueue.updateMemoName(operation.id, serverMemo.name)
            }
          }

          SyncOperationType.UPDATE -> {
            val name = operation.memoName ?: continue
            val content = operation.content ?: continue

            // Skip temp memos that haven't been synced yet
            if (GenerateTempMemoNameUseCase.isTemporaryName(name)) {
              Log.d(TAG, "Skipping update for temp memo: $name")
              continue
            }

            memosApi.updateMemo(baseUrl, token, name, content)
          }

          SyncOperationType.DELETE -> {
            val name = operation.memoName ?: continue
            memosApi.deleteMemo(baseUrl, token, name)
          }
        }

        syncQueue.updateStatus(operation.id, SyncOperationStatus.SYNCED)
        Log.d(TAG, "Synced operation: ${operation.id}")
      } catch (e: Exception) {
        syncQueue.updateStatus(operation.id, SyncOperationStatus.FAILED)
        Log.e(TAG, "Failed to sync operation: ${operation.id}", e)
        throw e // Re-throw to fail the entire sync
      }
    }
  }
}
