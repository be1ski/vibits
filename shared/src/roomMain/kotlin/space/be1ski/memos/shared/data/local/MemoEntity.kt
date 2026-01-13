package space.be1ski.memos.shared.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity that stores memo content for offline cache.
 */
@Entity(tableName = "memos")
data class MemoEntity(
  @PrimaryKey val name: String,
  val content: String,
  val createTimeMillis: Long?,
  val updateTimeMillis: Long?
)
