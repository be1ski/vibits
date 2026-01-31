package space.be1ski.vibits.shared.feature.memos.data.platform

import androidx.room.Room
import space.be1ski.vibits.shared.app.data.AndroidContextHolder
import space.be1ski.vibits.shared.feature.memos.data.room.MIGRATION_1_2
import space.be1ski.vibits.shared.feature.memos.data.room.MemoDatabase

/**
 * Singleton holder for the shared database instance on Android.
 */
internal object AndroidDatabaseHolder {
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
