package space.be1ski.vibits.feature.sync.data

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.ExperimentalMetroApi
import dev.zacsweers.metro.ExposeImplBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import space.be1ski.vibits.core.platform.di.AppScope
import space.be1ski.vibits.core.utils.logging.Log
import space.be1ski.vibits.feature.memos.domain.model.Memo
import space.be1ski.vibits.feature.memos.domain.repository.MemoCache
import space.be1ski.vibits.feature.sync.domain.SyncLogTags
import space.be1ski.vibits.feature.sync.domain.model.OperationId
import space.be1ski.vibits.feature.sync.domain.model.SyncOperation
import space.be1ski.vibits.feature.sync.domain.model.SyncOperationType
import space.be1ski.vibits.feature.sync.domain.model.TempMemoName
import space.be1ski.vibits.feature.sync.domain.repository.OfflineFirstMemoOperations
import space.be1ski.vibits.feature.sync.domain.repository.SyncQueueRepository
import kotlin.time.Clock

private const val TAG = SyncLogTags.OFFLINE_FIRST_MEMOS

/**
 * Thread-safe offline-first memo operations.
 * All write operations are persisted locally first, then queued for sync.
 * Uses a mutex to ensure thread-safe access to cache and sync queue.
 */
@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@OptIn(ExperimentalMetroApi::class)
@ExposeImplBinding
class OfflineFirstMemosRepository(
  private val memoCache: MemoCache,
  private val syncQueue: SyncQueueRepository,
) : OfflineFirstMemoOperations {
  /** Mutex to ensure thread-safe operations on cache and sync queue. */
  private val mutex = Mutex()

  /**
   * Creates a memo locally and queues it for sync.
   * Returns a memo with a temporary name that will be replaced after sync.
   * Thread-safe.
   */
  override suspend fun createMemoLocally(content: String): Memo =
    mutex.withLock {
      val now = Clock.System.now()
      val tempName = TempMemoName.generate()
      val memo =
        Memo(
          name = tempName,
          content = content,
          createTime = now,
          updateTime = now,
        )

      // Save locally first
      memoCache.upsertMemo(memo)
      Log.d(TAG, "Created memo locally: $tempName")

      // Queue for sync
      val operation =
        SyncOperation(
          id = OperationId.generate(),
          type = SyncOperationType.CREATE,
          memoName = tempName,
          content = content,
          createdAt = now,
        )
      syncQueue.addOperation(operation)
      Log.d(TAG, "Queued CREATE operation: ${operation.id}")

      memo
    }

  /**
   * Updates a memo locally and queues it for sync.
   * For temporary memos, coalesces the update into the pending CREATE operation
   * to ensure the latest content is synced when the CREATE is applied.
   * Thread-safe.
   */
  override suspend fun updateMemoLocally(
    name: String,
    content: String,
  ): Memo =
    mutex.withLock {
      val now = Clock.System.now()

      // Get existing memo to preserve create time
      val existingMemos = memoCache.readMemos()
      val existing = existingMemos.find { it.name == name }

      val memo =
        Memo(
          name = name,
          content = content,
          createTime = existing?.createTime ?: now,
          updateTime = now,
        )

      // Save locally first
      memoCache.upsertMemo(memo)
      Log.d(TAG, "Updated memo locally: $name")

      // For temp memos, try to update the pending CREATE operation instead of enqueueing UPDATE
      if (TempMemoName.isTemporary(name)) {
        val pendingCreate =
          syncQueue.getPendingOperations().find {
            it.type == SyncOperationType.CREATE && it.memoName == name
          }
        if (pendingCreate != null) {
          // Atomically update content only if still PENDING (avoids race with concurrent sync)
          val updated = syncQueue.updateContent(pendingCreate.id, content)
          if (updated) {
            Log.d(TAG, "Updated content in pending CREATE: ${pendingCreate.id}")
          } else {
            // CREATE is no longer pending (being synced), fall back to UPDATE
            Log.d(TAG, "CREATE no longer pending, enqueueing UPDATE: ${pendingCreate.id}")
            enqueueUpdateOperation(name, content, now)
          }
        } else {
          Log.w(TAG, "No pending CREATE found for temp memo: $name, enqueueing UPDATE")
          enqueueUpdateOperation(name, content, now)
        }
      } else {
        enqueueUpdateOperation(name, content, now)
      }

      memo
    }

  private suspend fun enqueueUpdateOperation(
    name: String,
    content: String,
    createdAt: kotlin.time.Instant,
  ) {
    val operation =
      SyncOperation(
        id = OperationId.generate(),
        type = SyncOperationType.UPDATE,
        memoName = name,
        content = content,
        createdAt = createdAt,
      )
    syncQueue.addOperation(operation)
    Log.d(TAG, "Queued UPDATE operation: ${operation.id}")
  }

  /**
   * Deletes a memo locally and queues it for sync.
   * For temporary memos, removes the pending CREATE operation instead of enqueueing DELETE.
   * Thread-safe.
   */
  override suspend fun deleteMemoLocally(name: String) =
    mutex.withLock {
      // Delete locally first
      memoCache.deleteMemo(name)
      Log.d(TAG, "Deleted memo locally: $name")

      // Queue for sync (only if it's not a temporary local-only memo)
      if (!TempMemoName.isTemporary(name)) {
        val operation =
          SyncOperation(
            id = OperationId.generate(),
            type = SyncOperationType.DELETE,
            memoName = name,
            content = null,
            createdAt = Clock.System.now(),
          )
        syncQueue.addOperation(operation)
        Log.d(TAG, "Queued DELETE operation: ${operation.id}")
      } else {
        // For temp memos that were never synced, remove any pending CREATE operation
        val pendingCreate =
          syncQueue.getPendingOperations().find {
            it.type == SyncOperationType.CREATE && it.memoName == name
          }
        if (pendingCreate != null) {
          syncQueue.removeOperation(pendingCreate.id)
          Log.d(TAG, "Removed pending CREATE for temp memo: ${pendingCreate.id}")
        } else {
          Log.d(TAG, "No pending CREATE found for temp memo: $name")
        }
      }
    }

  /**
   * Returns all locally cached memos.
   * Thread-safe.
   */
  override suspend fun getCachedMemos(): List<Memo> =
    mutex.withLock {
      memoCache.readMemos()
    }

  /**
   * Replaces all local memos with the given list.
   * Preserves any local temporary memos (names starting with local_) that were created
   * during sync but haven't been synced yet.
   * Used after successful full sync.
   * Thread-safe.
   */
  suspend fun replaceAllMemos(memos: List<Memo>) =
    mutex.withLock {
      // Preserve local temporary memos that might have been created during sync
      val currentMemos = memoCache.readMemos()
      val localTempMemos = currentMemos.filter { TempMemoName.isTemporary(it.name) }

      // Replace with server memos + preserved local temps
      val combined = memos + localTempMemos
      memoCache.replaceMemos(combined)

      if (localTempMemos.isNotEmpty()) {
        Log.d(TAG, "Replaced all memos: ${memos.size} server + ${localTempMemos.size} local temp")
      } else {
        Log.d(TAG, "Replaced all memos: ${memos.size}")
      }
    }

  /**
   * Updates a local memo with server-assigned data.
   * Used after successful sync to update temp names with real names.
   * Thread-safe.
   */
  suspend fun updateLocalMemo(
    oldName: String,
    newMemo: Memo,
  ) = mutex.withLock {
    if (oldName != newMemo.name) {
      // Name changed (temp -> real), delete old and insert new
      memoCache.deleteMemo(oldName)
    }
    memoCache.upsertMemo(newMemo)
    Log.d(TAG, "Updated local memo: $oldName -> ${newMemo.name}")
  }

  /**
   * Clears all online mode data (cache and pending operations).
   * Called when switching away from ONLINE mode to prevent data leakage.
   * Thread-safe.
   */
  override suspend fun clearOnlineData() =
    mutex.withLock {
      memoCache.clear()
      syncQueue.clearOperations(syncedOnly = false)
      Log.i(TAG, "Cleared online data (cache and pending operations)")
    }
}
