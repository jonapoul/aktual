package aktual.budget.db.dao

import aktual.budget.db.model.KvCache
import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert

@Dao
interface KvCacheDao {
  @Upsert suspend fun insert(kvCache: KvCache)

  @Query("SELECT * FROM kvcache") suspend fun getAll(): List<KvCache>

  @Query("SELECT COUNT(*) FROM kvcache") suspend fun getCount(): Long
}
