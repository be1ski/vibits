package space.be1ski.vibits.shared.feature.memos.data.platform

import space.be1ski.vibits.shared.feature.memos.data.room.MemoEntityMapper
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo

actual fun createMemoCache(): MemoCache = IosMemoCache()

private class IosMemoCache : MemoCache {
  override suspend fun readMemos(): List<Memo> =
    IosDatabaseHolder.database.memoDao().loadAll().map(MemoEntityMapper::toDomain)

  override suspend fun replaceMemos(memos: List<Memo>) {
    val dao = IosDatabaseHolder.database.memoDao()
    dao.clearAll()
    if (memos.isNotEmpty()) {
      dao.upsertAll(memos.map(MemoEntityMapper::toEntity))
    }
  }

  override suspend fun upsertMemo(memo: Memo) {
    IosDatabaseHolder.database.memoDao().upsert(MemoEntityMapper.toEntity(memo))
  }

  override suspend fun deleteMemo(name: String) {
    IosDatabaseHolder.database.memoDao().deleteByName(name)
  }

  override suspend fun clear() {
    IosDatabaseHolder.database.memoDao().clearAll()
  }
}
