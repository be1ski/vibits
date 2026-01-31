package space.be1ski.vibits.shared.feature.memos.data.platform

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import space.be1ski.vibits.shared.feature.memos.data.room.MIGRATION_1_2
import space.be1ski.vibits.shared.feature.memos.data.room.MemoDatabase
import space.be1ski.vibits.shared.feature.memos.data.room.MemoDatabaseConstructor

/**
 * Singleton holder for the shared database instance on iOS.
 */
internal object IosDatabaseHolder {
  val database: MemoDatabase by lazy { createDatabase() }

  @OptIn(ExperimentalForeignApi::class)
  private fun createDatabase(): MemoDatabase {
    val dbPath = getDatabasePath()
    return Room
      .databaseBuilder<MemoDatabase>(
        name = dbPath,
        factory = MemoDatabaseConstructor::initialize,
      ).setDriver(BundledSQLiteDriver())
      .addMigrations(MIGRATION_1_2)
      .build()
  }

  @OptIn(ExperimentalForeignApi::class)
  private fun getDatabasePath(): String {
    val paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true)
    val documentsDir = paths.firstOrNull() as? String ?: ""
    return "$documentsDir/memos.db"
  }
}
