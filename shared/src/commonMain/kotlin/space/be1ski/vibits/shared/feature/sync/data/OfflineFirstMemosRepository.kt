package space.be1ski.vibits.shared.feature.sync.data

import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import space.be1ski.vibits.shared.app.di.AppScope
import space.be1ski.vibits.shared.core.logging.Log
import space.be1ski.vibits.shared.feature.memos.data.platform.MemoCache
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.feature.sync.domain.SyncLogTags
import space.be1ski.vibits.shared.feature.sync.domain.model.SyncOperation
import space.be1ski.vibits.shared.feature.sync.domain.model.SyncOperationType
import space.be1ski.vibits.shared.feature.sync.domain.repository.SyncQueueRepository
import space.be1ski.vibits.shared.feature.sync.domain.usecase.GenerateOperationIdUseCase
import space.be1ski.vibits.shared.feature.sync.domain.usecase.GenerateTempMemoNameUseCase
import kotlin.time.Clock

private val TAG = SyncLogTags.OFFLINE_FIRST_MEMOS

/**
 * Thread-safe offline-first memo operations.
 * All write operations are persisted locally first, then queued for sync.
 * Uses a mutex to ensure thread-safe access to cache and sync queue.
 */
@Inject
@SingleIn(AppScope::class)
class OfflineFirstMemosRepository(
  private val memoCache: MemoCache,
  private val syncQueue: SyncQueueRepository,
) {
  /** Mutex to ensure thread-safe operations on cache and sync queue. */
  private val mutex = Mutex()

  /**
   * Creates a memo locally and queues it for sync.
   * Returns a memo with a temporary name that will be replaced after sync.
   * Thread-safe.
   */
  suspend fun createMemoLocally(content: String): Memo =
    mutex.withLock {
      val now = Clock.System.now()
      val tempName = GenerateTempMemoNameUseCase()
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
          id = GenerateOperationIdUseCase(),
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
   * Thread-safe.
   */
  suspend fun updateMemoLocally(
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

      // Queue for sync
      val operation =
        SyncOperation(
          id = GenerateOperationIdUseCase(),
          type = SyncOperationType.UPDATE,
          memoName = name,
          content = content,
          createdAt = now,
        )
      syncQueue.addOperation(operation)
      Log.d(TAG, "Queued UPDATE operation: ${operation.id}")

      memo
    }

  /**
   * Deletes a memo locally and queues it for sync.
   * Thread-safe.
   */
  suspend fun deleteMemoLocally(name: String) =
    mutex.withLock {
      // Delete locally first
      memoCache.deleteMemo(name)
      Log.d(TAG, "Deleted memo locally: $name")

      // Queue for sync (only if it's not a temporary local-only memo)
      if (!GenerateTempMemoNameUseCase.isTemporaryName(name)) {
        val operation =
          SyncOperation(
            id = GenerateOperationIdUseCase(),
            type = SyncOperationType.DELETE,
            memoName = name,
            content = null,
            createdAt = Clock.System.now(),
          )
        syncQueue.addOperation(operation)
        Log.d(TAG, "Queued DELETE operation: ${operation.id}")
      } else {
        // For temp memos that were never synced, just remove any pending CREATE operation
        Log.d(TAG, "Skipped sync for temporary memo: $name")
      }
    }

  /**
   * Returns all locally cached memos.
   * Thread-safe.
   */
  suspend fun getCachedMemos(): List<Memo> =
    mutex.withLock {
      memoCache.readMemos()
    }

  /**
   * Replaces all local memos with the given list.
   * Used after successful full sync.
   * Thread-safe.
   */
  suspend fun replaceAllMemos(memos: List<Memo>) =
    mutex.withLock {
      memoCache.replaceMemos(memos)
      Log.d(TAG, "Replaced all memos: ${memos.size}")
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
}
