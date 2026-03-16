package aktual.budget.db.model

import aktual.budget.model.CategoryGroupId
import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "category_groups")
data class CategoryGroup(
  @PrimaryKey @ColumnInfo(name = "id") val id: CategoryGroupId,
  @ColumnInfo(name = "name") val name: String?,
  @ColumnInfo(name = "is_income") val isIncome: Boolean? = false,
  @ColumnInfo(name = "sort_order") val sortOrder: Double?,
  @ColumnInfo(name = "tombstone") val tombstone: Boolean? = false,
  @ColumnInfo(name = "hidden") val hidden: Boolean = false,
)
