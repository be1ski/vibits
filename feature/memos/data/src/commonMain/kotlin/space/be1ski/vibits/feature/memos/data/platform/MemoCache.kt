package space.be1ski.vibits.feature.memos.data.platform

import space.be1ski.vibits.feature.memos.domain.model.Memo

/**
 * Platform-specific memo cache.
 * Uses Room database (Android, Desktop, iOS) or no-op (WASM).
 */
interface MemoCache {
  suspend fun readMemos(): List<Memo>

  suspend fun replaceMemos(memos: List<Memo>)

  suspend fun upsertMemo(memo: Memo)

  suspend fun deleteMemo(name: String)

  suspend fun clear()
}

expect fun createMemoCache(): MemoCache
