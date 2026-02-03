package space.be1ski.vibits.feature.memos.data.room

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * Migration from version 1 to 2: adds sync_operations table.
 */
val MIGRATION_1_2 =
  object : Migration(1, 2) {
    override fun migrate(connection: SQLiteConnection) {
      connection.execSQL(
        """
        CREATE TABLE IF NOT EXISTS sync_operations (
          id TEXT NOT NULL PRIMARY KEY,
          type TEXT NOT NULL,
          memoName TEXT,
          content TEXT,
          createdAtMillis INTEGER NOT NULL,
          status TEXT NOT NULL
        )
        """.trimIndent(),
      )
    }
  }
