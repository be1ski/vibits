package space.be1ski.vibits.feature.memos.data.room.sync

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncOperationDao {
  @Query("SELECT * FROM sync_operations WHERE status = 'PENDING' ORDER BY createdAtMillis ASC")
  suspend fun getPending(): List<SyncOperationEntity>

  @Query("SELECT * FROM sync_operations ORDER BY createdAtMillis ASC")
  suspend fun getAll(): List<SyncOperationEntity>

  @Query("SELECT COUNT(*) FROM sync_operations WHERE status = 'PENDING'")
  fun observePendingCount(): Flow<Int>

  @Query("SELECT COUNT(*) FROM sync_operations WHERE status = 'FAILED'")
  fun observeFailedCount(): Flow<Int>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsert(entity: SyncOperationEntity)

  @Query("UPDATE sync_operations SET status = :status WHERE id = :id")
  suspend fun updateStatus(
    id: String,
    status: String,
  )

  @Query("UPDATE sync_operations SET memoName = :memoName WHERE id = :id")
  suspend fun updateMemoName(
    id: String,
    memoName: String,
  )

  // Returns number of rows affected (1 if found, 0 if not)
  @Query("UPDATE sync_operations SET content = :content WHERE id = :id AND status = 'PENDING'")
  suspend fun updateContent(
    id: String,
    content: String,
  ): Int

  @Query("DELETE FROM sync_operations WHERE id = :id")
  suspend fun deleteById(id: String)

  @Query("DELETE FROM sync_operations WHERE status = 'SYNCED'")
  suspend fun clearSynced()

  @Query("SELECT COUNT(*) FROM sync_operations WHERE status = 'PENDING'")
  suspend fun getPendingCount(): Int

  @Query("SELECT COUNT(*) FROM sync_operations WHERE status = 'FAILED'")
  suspend fun getFailedCount(): Int

  @Query("DELETE FROM sync_operations")
  suspend fun clearAll()

  @Query("UPDATE sync_operations SET status = 'PENDING' WHERE status = 'IN_PROGRESS'")
  suspend fun resetInProgressToPending()
}
