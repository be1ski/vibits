package space.be1ski.memos.shared.feature.memos.domain.repository

import space.be1ski.memos.shared.feature.memos.domain.model.Memo

/**
 * Domain repository for loading memos.
 */
interface MemosRepository {
  /**
   * Returns all memos from the server using paginated requests.
   */
  suspend fun listMemos(): List<Memo>

  /**
   * Returns cached memos stored locally.
   */
  suspend fun cachedMemos(): List<Memo>

  /**
   * Updates memo content and returns the updated memo.
   */
  suspend fun updateMemo(name: String, content: String): Memo

  /**
   * Creates a new memo and returns it.
   */
  suspend fun createMemo(content: String): Memo

  /**
   * Deletes a memo by name.
   */
  suspend fun deleteMemo(name: String)
}
