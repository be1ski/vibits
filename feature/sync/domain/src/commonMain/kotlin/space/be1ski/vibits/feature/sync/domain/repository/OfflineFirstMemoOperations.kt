package space.be1ski.vibits.feature.sync.domain.repository

import space.be1ski.vibits.feature.memos.domain.model.Memo

/**
 * Interface for offline-first memo operations.
 * All write operations are persisted locally first, then queued for sync.
 */
interface OfflineFirstMemoOperations {
  /**
   * Creates a memo locally and queues it for sync.
   * Returns a memo with a temporary name that will be replaced after sync.
   */
  suspend fun createMemoLocally(content: String): Memo

  /**
   * Updates a memo locally and queues it for sync.
   */
  suspend fun updateMemoLocally(
    name: String,
    content: String,
  ): Memo

  /**
   * Deletes a memo locally and queues it for sync.
   */
  suspend fun deleteMemoLocally(name: String)

  /**
   * Returns all locally cached memos.
   */
  suspend fun getCachedMemos(): List<Memo>

  /**
   * Clears all online mode data (cache and pending operations).
   */
  suspend fun clearOnlineData()
}
