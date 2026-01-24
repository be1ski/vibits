package space.be1ski.vibits.shared.feature.memos.data.platform

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import space.be1ski.vibits.shared.feature.memos.data.room.MemoDatabase
import space.be1ski.vibits.shared.feature.memos.data.room.MemoDatabaseConstructor
import space.be1ski.vibits.shared.feature.memos.data.room.MemoEntityMapper
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo

actual fun createMemoCache(): MemoCache = IosMemoCache()

private class IosMemoCache : MemoCache {
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

  @OptIn(ExperimentalForeignApi::class)
  private fun createDatabase(): MemoDatabase {
    val dbPath = getDatabasePath()
    return Room
      .databaseBuilder<MemoDatabase>(
        name = dbPath,
        factory = MemoDatabaseConstructor::initialize,
      ).setDriver(BundledSQLiteDriver())
      .build()
  }

  @OptIn(ExperimentalForeignApi::class)
  private fun getDatabasePath(): String {
    val paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true)
    val documentsDir = paths.firstOrNull() as? String ?: ""
    return "$documentsDir/memos.db"
  }
}
