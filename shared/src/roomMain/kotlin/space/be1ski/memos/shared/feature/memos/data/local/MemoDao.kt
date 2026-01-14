package space.be1ski.memos.shared.feature.memos.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Room DAO for cached memos.
 */
@Dao
interface MemoDao {
  /**
   * Returns all cached memos ordered by update time.
   */
  @Query("SELECT * FROM memos ORDER BY updateTimeMillis DESC")
  suspend fun loadAll(): List<MemoEntity>

  /**
   * Inserts or replaces memo entities.
   */
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertAll(entities: List<MemoEntity>)

  /**
   * Inserts or replaces a memo entity.
   */
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsert(entity: MemoEntity)

  /**
   * Deletes a memo by its name.
   */
  @Query("DELETE FROM memos WHERE name = :name")
  suspend fun deleteByName(name: String)

  /**
   * Clears all cached memos.
   */
  @Query("DELETE FROM memos")
  suspend fun clearAll()
}
