package space.be1ski.vibits.shared.feature.memos.data.platform

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import space.be1ski.vibits.shared.app.data.DesktopStoragePaths
import space.be1ski.vibits.shared.feature.memos.data.room.MemoDatabase
import space.be1ski.vibits.shared.feature.memos.data.room.MemoDatabaseConstructor
import space.be1ski.vibits.shared.feature.memos.data.room.MemoEntityMapper
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo

actual fun createMemoCache(): MemoCache = DesktopMemoCache()

private class DesktopMemoCache : MemoCache {
  private val database: MemoDatabase by lazy { createDatabase() }

  override suspend fun readMemos(): List<Memo> = database.memoDao().loadAll().map(MemoEntityMapper::toDomain)

  override suspend fun replaceMemos(memos: List<Memo>) {
    val dao = database.memoDao()
    dao.clearAll()
    if (memos.isNotEmpty()) {
      dao.upsertAll(memos.map(MemoEntityMapper::toEntity))
    }
  }

  override suspend fun upsertMemo(memo: Memo) {
    database.memoDao().upsert(MemoEntityMapper.toEntity(memo))
  }

  override suspend fun deleteMemo(name: String) {
    database.memoDao().deleteByName(name)
  }

  override suspend fun clear() {
    database.memoDao().clearAll()
  }

  private fun createDatabase(): MemoDatabase =
    Room
      .databaseBuilder<MemoDatabase>(
        name = DesktopStoragePaths.databasePath(),
        factory = MemoDatabaseConstructor::initialize,
      ).setDriver(BundledSQLiteDriver())
      .build()
}
