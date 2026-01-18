package space.be1ski.vibits.shared.feature.memos.data.platform

import androidx.room.Room
import space.be1ski.vibits.shared.app.data.AndroidContextHolder
import space.be1ski.vibits.shared.feature.memos.data.local.MemoDao
import space.be1ski.vibits.shared.feature.memos.data.local.MemoDatabase
import space.be1ski.vibits.shared.feature.memos.data.local.MemoEntityMapper
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo

actual fun createMemoCache(): MemoCache = AndroidMemoCache()

private class AndroidMemoCache : MemoCache {
  private var database: MemoDatabase? = null

  private fun daoOrNull(): MemoDao? {
    if (database == null && AndroidContextHolder.isReady()) {
      database =
        Room
          .databaseBuilder(
            AndroidContextHolder.context,
            MemoDatabase::class.java,
            "memos.db",
          ).build()
    }
    return database?.memoDao()
  }

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
