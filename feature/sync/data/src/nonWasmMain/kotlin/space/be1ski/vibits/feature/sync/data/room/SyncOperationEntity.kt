package space.be1ski.vibits.feature.sync.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for pending sync operations.
 */
@Entity(tableName = "sync_operations")
data class SyncOperationEntity(
  @PrimaryKey val id: String,
  val type: String,
  val memoName: String?,
  val content: String?,
  val createdAtMillis: Long,
  val status: String,
)
