package actual.db.entity

import actual.budget.model.CategoryId
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category_mapping")
data class CategoryMappingEntity(
  @PrimaryKey @ColumnInfo("id") val id: CategoryId,
  @ColumnInfo("transferId") val transferId: CategoryId?,
)
