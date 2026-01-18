package space.be1ski.vibits.shared.feature.memos.data.platform

import space.be1ski.vibits.shared.feature.memos.domain.model.Memo

actual fun createMemoCache(): MemoCache = WasmMemoCache()

private class WasmMemoCache : MemoCache {
  override suspend fun readMemos(): List<Memo> = emptyList()

  override suspend fun replaceMemos(memos: List<Memo>) = Unit

  override suspend fun upsertMemo(memo: Memo) = Unit

  override suspend fun deleteMemo(name: String) = Unit

  override suspend fun clear() = Unit
}
