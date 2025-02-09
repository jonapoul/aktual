package actual.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category_mapping")
data class CategoryMappingEntity(
  @PrimaryKey @ColumnInfo("id") val id: String,
  @ColumnInfo("transferId") val transferId: String?,
)
