package space.be1ski.memos.shared.data.local

import space.be1ski.memos.shared.domain.model.memo.Memo

/**
 * Platform-specific memo cache for offline-first UI.
 */
expect class MemoCache() {
  /**
   * Loads cached memos from the local database.
   */
  suspend fun readMemos(): List<Memo>

  /**
   * Replaces the cache with the latest memos.
   */
  suspend fun replaceMemos(memos: List<Memo>)

  /**
   * Upserts a single memo into the cache.
   */
  suspend fun upsertMemo(memo: Memo)

  /**
   * Deletes a memo from the cache by name.
   */
  suspend fun deleteMemo(name: String)
}
