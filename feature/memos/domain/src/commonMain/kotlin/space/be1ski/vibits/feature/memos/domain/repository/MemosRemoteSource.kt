package space.be1ski.vibits.feature.memos.domain.repository

import space.be1ski.vibits.feature.memos.domain.model.Memo

data class MemosPage(
  val memos: List<Memo>,
  val nextPageToken: String?,
)

interface MemosRemoteSource {
  suspend fun listMemos(
    pageSize: Int,
    pageToken: String?,
  ): MemosPage

  suspend fun createMemo(content: String): Memo

  suspend fun updateMemo(
    name: String,
    content: String,
  ): Memo

  suspend fun deleteMemo(name: String)
}
