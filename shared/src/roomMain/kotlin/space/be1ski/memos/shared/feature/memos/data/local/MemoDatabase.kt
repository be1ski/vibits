package space.be1ski.memos.shared.feature.memos.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Room database that stores cached memos.
 */
@Database(entities = [MemoEntity::class], version = 1, exportSchema = false)
@ConstructedBy(MemoDatabaseConstructor::class)
abstract class MemoDatabase : RoomDatabase() {
  /**
   * Returns DAO for memo cache.
   */
  abstract fun memoDao(): MemoDao
}
