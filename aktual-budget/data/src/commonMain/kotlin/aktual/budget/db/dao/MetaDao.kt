package aktual.budget.db.dao

import aktual.budget.db.model.Meta
import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert

@Dao
interface MetaDao {
  @Upsert suspend fun insert(meta: Meta)

  @Query("SELECT value FROM __meta__ WHERE key = :key") suspend fun getValue(key: String): String?
}
