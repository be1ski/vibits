package space.be1ski.vibits.feature.memos.data.test

import space.be1ski.vibits.feature.memos.domain.model.Memo
import space.be1ski.vibits.feature.memos.domain.repository.MemoCache

class FakeMemoCache(
  private var memos: List<Memo> = emptyList(),
) : MemoCache {
  var replaceCalls: Int = 0
    private set
  var upserted: Memo? = null
    private set
  var deletedName: String? = null
    private set
  var clearCalls: Int = 0
    private set

  override suspend fun readMemos(): List<Memo> = memos

  override suspend fun replaceMemos(memos: List<Memo>) {
    replaceCalls += 1
    this.memos = memos
  }

  override suspend fun upsertMemo(memo: Memo) {
    upserted = memo
    memos = memos.filterNot { it.name == memo.name } + memo
  }

  override suspend fun deleteMemo(name: String) {
    deletedName = name
    memos = memos.filterNot { it.name == name }
  }

  override suspend fun clear() {
    clearCalls += 1
    memos = emptyList()
  }
}
