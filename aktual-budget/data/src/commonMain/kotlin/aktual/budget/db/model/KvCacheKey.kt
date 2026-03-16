package aktual.budget.db.model

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "kvcache_key")
data class KvCacheKey(
  @PrimaryKey @ColumnInfo(name = "id") val id: Int,
  @ColumnInfo(name = "key") val key: Double?,
)
