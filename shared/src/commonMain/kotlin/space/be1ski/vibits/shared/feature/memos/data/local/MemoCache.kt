package space.be1ski.vibits.shared.feature.memos.data.local

import space.be1ski.vibits.shared.feature.memos.domain.model.Memo

expect open class MemoCache() {
  open suspend fun readMemos(): List<Memo>

  open suspend fun replaceMemos(memos: List<Memo>)

  open suspend fun upsertMemo(memo: Memo)

  open suspend fun deleteMemo(name: String)

  open suspend fun clear()
}
