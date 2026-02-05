package space.be1ski.vibits.feature.memos.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memos")
data class MemoEntity(
  @PrimaryKey val name: String,
  val content: String,
  val createTimeMillis: Long?,
  val updateTimeMillis: Long?,
)
