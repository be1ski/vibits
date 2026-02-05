package space.be1ski.vibits.feature.memos.data.internal

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import space.be1ski.vibits.core.platform.app.DesktopStoragePaths
import space.be1ski.vibits.feature.memos.data.room.MIGRATION_1_2
import space.be1ski.vibits.feature.memos.data.room.MemoDatabase
import space.be1ski.vibits.feature.memos.data.room.MemoDatabaseConstructor

object DesktopDatabaseHolder {
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
