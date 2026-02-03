package space.be1ski.vibits.feature.memos.data.room.sync

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for sync operations.
 */
@Dao
interface SyncOperationDao {
  /**
   * Returns all pending operations ordered by creation time.
   * Only returns operations with PENDING status (not FAILED).
   */
  @Query("SELECT * FROM sync_operations WHERE status = 'PENDING' ORDER BY createdAtMillis ASC")
  suspend fun getPending(): List<SyncOperationEntity>

  /**
   * Returns all operations.
   */
  @Query("SELECT * FROM sync_operations ORDER BY createdAtMillis ASC")
  suspend fun getAll(): List<SyncOperationEntity>

  /**
   * Observes the count of pending operations.
   */
  @Query("SELECT COUNT(*) FROM sync_operations WHERE status = 'PENDING'")
  fun observePendingCount(): Flow<Int>

  /**
   * Observes the count of failed operations.
   */
  @Query("SELECT COUNT(*) FROM sync_operations WHERE status = 'FAILED'")
  fun observeFailedCount(): Flow<Int>

  /**
   * Inserts or replaces an operation.
   */
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsert(entity: SyncOperationEntity)

  /**
   * Updates the status of an operation.
   */
  @Query("UPDATE sync_operations SET status = :status WHERE id = :id")
  suspend fun updateStatus(
    id: String,
    status: String,
  )

  /**
   * Updates the memo name of an operation.
   */
  @Query("UPDATE sync_operations SET memoName = :memoName WHERE id = :id")
  suspend fun updateMemoName(
    id: String,
    memoName: String,
  )

  /**
   * Updates the content of a pending operation.
   * @return number of rows affected (1 if found, 0 if not)
   */
  @Query("UPDATE sync_operations SET content = :content WHERE id = :id AND status = 'PENDING'")
  suspend fun updateContent(
    id: String,
    content: String,
  ): Int

  /**
   * Deletes an operation by ID.
   */
  @Query("DELETE FROM sync_operations WHERE id = :id")
  suspend fun deleteById(id: String)

  /**
   * Clears all synced operations.
   */
  @Query("DELETE FROM sync_operations WHERE status = 'SYNCED'")
  suspend fun clearSynced()

  /**
   * Gets pending count.
   */
  @Query("SELECT COUNT(*) FROM sync_operations WHERE status = 'PENDING'")
  suspend fun getPendingCount(): Int

  /**
   * Gets failed count.
   */
  @Query("SELECT COUNT(*) FROM sync_operations WHERE status = 'FAILED'")
  suspend fun getFailedCount(): Int

  /**
   * Clears ALL operations regardless of status.
   */
  @Query("DELETE FROM sync_operations")
  suspend fun clearAll()

  /**
   * Resets IN_PROGRESS operations back to PENDING.
   */
  @Query("UPDATE sync_operations SET status = 'PENDING' WHERE status = 'IN_PROGRESS'")
  suspend fun resetInProgressToPending()
}
