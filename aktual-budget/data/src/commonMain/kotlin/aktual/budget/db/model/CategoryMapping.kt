package aktual.budget.db.model

import aktual.budget.model.CategoryId
import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "category_mapping")
data class CategoryMapping(
  @PrimaryKey @ColumnInfo(name = "id") val id: CategoryId,
  @ColumnInfo(name = "transferId") val transferId: CategoryId?,
)
