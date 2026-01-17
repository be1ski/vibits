package space.be1ski.vibits.shared.feature.memos.data.local

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo

/**
 * iOS memo cache backed by Room.
 */
actual open class MemoCache {
  private val database: MemoDatabase by lazy { createDatabase() }

  actual open suspend fun readMemos(): List<Memo> = database.memoDao().loadAll().map(MemoEntityMapper::toDomain)

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

  actual open suspend fun clear() {
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
