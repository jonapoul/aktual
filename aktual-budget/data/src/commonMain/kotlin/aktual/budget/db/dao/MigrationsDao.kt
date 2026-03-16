package aktual.budget.db.dao

import aktual.budget.db.model.Migration
import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query

@Dao
interface MigrationsDao {
  @Query("DELETE FROM __migrations__ WHERE id = :id") suspend fun delete(id: Int)

  @Insert suspend fun insert(migration: Migration)

  @Query("SELECT * FROM __migrations__ ORDER BY id ASC") suspend fun getAll(): List<Migration>
}
