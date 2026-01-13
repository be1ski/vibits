package space.be1ski.memos.shared.data.local

import androidx.room.Room
import space.be1ski.memos.shared.domain.model.memo.Memo

/**
 * Android memo cache backed by Room.
 */
actual class MemoCache {
  private var database: MemoDatabase? = null

  private fun daoOrNull(): MemoDao? {
    if (database == null && AndroidContextHolder.isReady()) {
      database = Room.databaseBuilder(
        AndroidContextHolder.context,
        MemoDatabase::class.java,
        "memos.db"
      ).build()
    }
    return database?.memoDao()
  }

  actual suspend fun readMemos(): List<Memo> {
    val dao = daoOrNull() ?: return emptyList()
    return dao.loadAll().map(MemoEntityMapper::toDomain)
  }

  actual suspend fun replaceMemos(memos: List<Memo>) {
    val dao = daoOrNull() ?: return
    dao.clearAll()
    if (memos.isNotEmpty()) {
      dao.upsertAll(memos.map(MemoEntityMapper::toEntity))
    }
  }

  actual suspend fun upsertMemo(memo: Memo) {
    val dao = daoOrNull() ?: return
    dao.upsert(MemoEntityMapper.toEntity(memo))
  }

  actual suspend fun deleteMemo(name: String) {
    val dao = daoOrNull() ?: return
    dao.deleteByName(name)
  }
}
