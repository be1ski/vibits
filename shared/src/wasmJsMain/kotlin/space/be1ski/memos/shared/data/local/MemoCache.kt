package space.be1ski.memos.shared.data.local

import space.be1ski.memos.shared.domain.model.memo.Memo

/**
 * No-op memo cache for web builds.
 */
actual open class MemoCache {
  actual open suspend fun readMemos(): List<Memo> = emptyList()

  actual open suspend fun replaceMemos(memos: List<Memo>) = Unit

  actual open suspend fun upsertMemo(memo: Memo) = Unit

  actual open suspend fun deleteMemo(name: String) = Unit
}
