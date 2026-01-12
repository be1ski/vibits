package space.be1ski.memos.shared.domain.repository

import space.be1ski.memos.shared.domain.model.Memo

/**
 * Domain repository for loading memos.
 */
interface MemosRepository {
  /**
   * Returns all memos from the server using paginated requests.
   */
  suspend fun listMemos(baseUrl: String, token: String, pageSize: Int): List<Memo>
}
