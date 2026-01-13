package space.be1ski.memos.shared.data.local

import space.be1ski.memos.shared.domain.model.memo.Memo

/**
 * No-op memo cache for web builds.
 */
actual class MemoCache {
  actual suspend fun readMemos(): List<Memo> = emptyList()

  actual suspend fun replaceMemos(memos: List<Memo>) = Unit

  actual suspend fun upsertMemo(memo: Memo) = Unit

  actual suspend fun deleteMemo(name: String) = Unit
}
