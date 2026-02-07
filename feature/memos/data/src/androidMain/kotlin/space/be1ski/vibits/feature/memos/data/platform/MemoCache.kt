package space.be1ski.vibits.feature.memos.data.platform

import space.be1ski.vibits.feature.memos.data.internal.AndroidDatabaseHolder
import space.be1ski.vibits.feature.memos.data.room.MemoDao
import space.be1ski.vibits.feature.memos.data.room.MemoEntityMapper
import space.be1ski.vibits.feature.memos.domain.model.Memo
import space.be1ski.vibits.feature.memos.domain.repository.MemoCache

actual fun createMemoCache(): MemoCache = AndroidMemoCache()

private class AndroidMemoCache : MemoCache {
  private fun daoOrNull(): MemoDao? = AndroidDatabaseHolder.getDatabase()?.memoDao()

  override suspend fun readMemos(): List<Memo> {
    val dao = daoOrNull() ?: return emptyList()
    return dao.loadAll().map(MemoEntityMapper::toDomain)
  }

  override suspend fun replaceMemos(memos: List<Memo>) {
    val dao = daoOrNull() ?: return
    dao.clearAll()
    if (memos.isNotEmpty()) {
      dao.upsertAll(memos.map(MemoEntityMapper::toEntity))
    }
  }

  override suspend fun upsertMemo(memo: Memo) {
    val dao = daoOrNull() ?: return
    dao.upsert(MemoEntityMapper.toEntity(memo))
  }

  override suspend fun deleteMemo(name: String) {
    val dao = daoOrNull() ?: return
    dao.deleteByName(name)
  }

  override suspend fun clear() {
    val dao = daoOrNull() ?: return
    dao.clearAll()
  }
}
