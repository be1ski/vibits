package space.be1ski.vibits.feature.memos.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MemoDao {
  @Query("SELECT * FROM memos ORDER BY updateTimeMillis DESC")
  suspend fun loadAll(): List<MemoEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertAll(entities: List<MemoEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsert(entity: MemoEntity)

  @Query("DELETE FROM memos WHERE name = :name")
  suspend fun deleteByName(name: String)

  @Query("DELETE FROM memos")
  suspend fun clearAll()
}
