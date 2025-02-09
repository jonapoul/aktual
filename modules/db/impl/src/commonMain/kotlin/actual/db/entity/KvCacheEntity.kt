package actual.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "kvcache")
data class KvCacheEntity(
  @PrimaryKey @ColumnInfo("key") val key: String,
  @ColumnInfo("value") val value: String?,
)
