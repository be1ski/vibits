package space.be1ski.vibits.feature.memos.domain.repository

import space.be1ski.vibits.feature.memos.domain.model.Memo

interface MemosRepository {
  suspend fun listMemos(): List<Memo>

  suspend fun cachedMemos(): List<Memo>

  suspend fun updateMemo(
    name: String,
    content: String,
  ): Memo

  suspend fun createMemo(content: String): Memo

  suspend fun deleteMemo(name: String)
}
