package space.be1ski.vibits.feature.memos.data.internal

import androidx.room.Room
import space.be1ski.vibits.core.platform.app.AndroidContextHolder
import space.be1ski.vibits.feature.memos.data.room.MIGRATION_1_2
import space.be1ski.vibits.feature.memos.data.room.MemoDatabase

/**
 * Singleton holder for the shared database instance on Android.
 */
object AndroidDatabaseHolder {
  private var database: MemoDatabase? = null

  fun getDatabase(): MemoDatabase? {
    if (database == null && AndroidContextHolder.isReady()) {
      database =
        Room
          .databaseBuilder(
            AndroidContextHolder.context,
            MemoDatabase::class.java,
            "memos.db",
          ).addMigrations(MIGRATION_1_2)
          .build()
    }
    return database
  }
}
