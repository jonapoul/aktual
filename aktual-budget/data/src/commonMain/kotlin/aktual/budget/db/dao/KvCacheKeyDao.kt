package aktual.budget.db.dao

import aktual.budget.db.model.KvCacheKey
import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert

@Dao
interface KvCacheKeyDao {
  @Query("DELETE FROM kvcache_key") suspend fun clear()

  @Upsert suspend fun set(kvCacheKey: KvCacheKey)

  @Query("SELECT key FROM kvcache_key WHERE id = 1") suspend fun get(): Double?
}
