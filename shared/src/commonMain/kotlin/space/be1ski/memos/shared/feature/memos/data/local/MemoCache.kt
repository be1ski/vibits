package space.be1ski.memos.shared.feature.memos.data.local

import space.be1ski.memos.shared.feature.memos.domain.model.Memo

/**
 * Platform-specific memo cache for offline-first UI.
 */
expect open class MemoCache() {
  /**
   * Loads cached memos from the local database.
   */
  open suspend fun readMemos(): List<Memo>

  /**
   * Replaces the cache with the latest memos.
   */
  open suspend fun replaceMemos(memos: List<Memo>)

  /**
   * Upserts a single memo into the cache.
   */
  open suspend fun upsertMemo(memo: Memo)

  /**
   * Deletes a memo from the cache by name.
   */
  open suspend fun deleteMemo(name: String)
}
