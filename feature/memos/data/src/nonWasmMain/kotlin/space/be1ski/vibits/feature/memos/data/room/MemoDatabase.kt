package space.be1ski.vibits.feature.memos.data.room

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import space.be1ski.vibits.feature.memos.data.room.sync.SyncOperationDao
import space.be1ski.vibits.feature.memos.data.room.sync.SyncOperationEntity

/**
 * Room database that stores cached memos and sync operations.
 */
@Database(
  entities = [MemoEntity::class, SyncOperationEntity::class],
  version = 2,
  exportSchema = false,
)
@ConstructedBy(MemoDatabaseConstructor::class)
abstract class MemoDatabase : RoomDatabase() {
  /**
   * Returns DAO for memo cache.
   */
  abstract fun memoDao(): MemoDao

  /**
   * Returns DAO for sync operations.
   */
  abstract fun syncOperationDao(): SyncOperationDao
}
