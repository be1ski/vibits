package space.be1ski.vibits.shared.feature.memos.data.platform

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import space.be1ski.vibits.shared.app.data.DesktopStoragePaths
import space.be1ski.vibits.shared.feature.memos.data.room.MIGRATION_1_2
import space.be1ski.vibits.shared.feature.memos.data.room.MemoDatabase
import space.be1ski.vibits.shared.feature.memos.data.room.MemoDatabaseConstructor

/**
 * Singleton holder for the shared database instance on Desktop.
 */
internal object DesktopDatabaseHolder {
  val database: MemoDatabase by lazy {
    Room
      .databaseBuilder<MemoDatabase>(
        name = DesktopStoragePaths.databasePath(),
        factory = MemoDatabaseConstructor::initialize,
      ).setDriver(BundledSQLiteDriver())
      .addMigrations(MIGRATION_1_2)
      .build()
  }
}
