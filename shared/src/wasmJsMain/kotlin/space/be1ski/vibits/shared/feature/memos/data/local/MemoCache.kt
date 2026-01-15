package space.be1ski.vibits.shared.feature.memos.data.local

import space.be1ski.vibits.shared.feature.memos.domain.model.Memo

/**
 * No-op memo cache for web builds.
 */
actual open class MemoCache {
  actual open suspend fun readMemos(): List<Memo> = emptyList()

  actual open suspend fun replaceMemos(memos: List<Memo>) = Unit

  actual open suspend fun upsertMemo(memo: Memo) = Unit

  actual open suspend fun deleteMemo(name: String) = Unit

  actual open suspend fun clear() = Unit
}
