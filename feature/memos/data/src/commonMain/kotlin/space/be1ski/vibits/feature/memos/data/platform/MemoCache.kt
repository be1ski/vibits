package space.be1ski.vibits.feature.memos.data.platform

import space.be1ski.vibits.feature.memos.domain.model.Memo

interface MemoCache {
  suspend fun readMemos(): List<Memo>

  suspend fun replaceMemos(memos: List<Memo>)

  suspend fun upsertMemo(memo: Memo)

  suspend fun deleteMemo(name: String)

  suspend fun clear()
}

expect fun createMemoCache(): MemoCache
