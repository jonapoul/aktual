package actual.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "kvcache_key")
data class KvCacheKeyEntity(
  @PrimaryKey @ColumnInfo("id") val id: String,
  @ColumnInfo("key") val key: Float?,
)
