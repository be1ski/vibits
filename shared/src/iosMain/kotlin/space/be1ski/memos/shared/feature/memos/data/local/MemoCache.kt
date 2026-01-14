package space.be1ski.memos.shared.feature.memos.data.local

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import space.be1ski.memos.shared.feature.memos.domain.model.Memo

/**
 * iOS memo cache backed by Room.
 */
actual open class MemoCache {
  private val database: MemoDatabase by lazy { createDatabase() }

  actual open suspend fun readMemos(): List<Memo> =
    database.memoDao().loadAll().map(MemoEntityMapper::toDomain)

  actual open suspend fun replaceMemos(memos: List<Memo>) {
    val dao = database.memoDao()
    dao.clearAll()
    if (memos.isNotEmpty()) {
      dao.upsertAll(memos.map(MemoEntityMapper::toEntity))
    }
  }

  actual open suspend fun upsertMemo(memo: Memo) {
    database.memoDao().upsert(MemoEntityMapper.toEntity(memo))
  }

  actual open suspend fun deleteMemo(name: String) {
    database.memoDao().deleteByName(name)
  }

  private fun createDatabase(): MemoDatabase =
    Room.databaseBuilder<MemoDatabase>(
      name = "memos.db",
      factory = MemoDatabaseConstructor::initialize
    )
      .setDriver(BundledSQLiteDriver())
      .build()
}
