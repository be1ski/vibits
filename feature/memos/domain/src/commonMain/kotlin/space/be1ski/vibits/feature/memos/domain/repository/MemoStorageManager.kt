package space.be1ski.vibits.feature.memos.domain.repository

/**
 * Manages offline memo storage operations.
 */
fun interface MemoStorageManager {
  /**
   * Clears all memos from offline storage.
   */
  fun clearAll()
}
