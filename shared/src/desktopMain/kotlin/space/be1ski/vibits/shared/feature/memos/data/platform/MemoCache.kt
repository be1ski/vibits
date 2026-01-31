package space.be1ski.vibits.shared.feature.memos.data.platform

import space.be1ski.vibits.shared.feature.memos.data.internal.DesktopDatabaseHolder
import space.be1ski.vibits.shared.feature.memos.data.room.MemoEntityMapper
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo

actual fun createMemoCache(): MemoCache = DesktopMemoCache()

private class DesktopMemoCache : MemoCache {
  override suspend fun readMemos(): List<Memo> =
    DesktopDatabaseHolder.database.memoDao().loadAll().map(MemoEntityMapper::toDomain)

  override suspend fun replaceMemos(memos: List<Memo>) {
    val dao = DesktopDatabaseHolder.database.memoDao()
    dao.clearAll()
    if (memos.isNotEmpty()) {
      dao.upsertAll(memos.map(MemoEntityMapper::toEntity))
    }
  }

  override suspend fun upsertMemo(memo: Memo) {
    DesktopDatabaseHolder.database.memoDao().upsert(MemoEntityMapper.toEntity(memo))
  }

  override suspend fun deleteMemo(name: String) {
    DesktopDatabaseHolder.database.memoDao().deleteByName(name)
  }

  override suspend fun clear() {
    DesktopDatabaseHolder.database.memoDao().clearAll()
  }
}
