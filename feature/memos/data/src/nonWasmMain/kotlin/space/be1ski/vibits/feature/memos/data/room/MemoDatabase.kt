package space.be1ski.vibits.feature.memos.data.room

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import space.be1ski.vibits.feature.memos.data.room.sync.SyncOperationDao
import space.be1ski.vibits.feature.memos.data.room.sync.SyncOperationEntity

@Database(
  entities = [MemoEntity::class, SyncOperationEntity::class],
  version = 2,
  exportSchema = false,
)
@ConstructedBy(MemoDatabaseConstructor::class)
abstract class MemoDatabase : RoomDatabase() {
  abstract fun memoDao(): MemoDao

  abstract fun syncOperationDao(): SyncOperationDao
}
