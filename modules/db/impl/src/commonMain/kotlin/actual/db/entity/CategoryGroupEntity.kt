package actual.db.entity

import actual.budget.model.CategoryGroupId
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category_groups")
data class CategoryGroupEntity(
  @PrimaryKey @ColumnInfo("id") val id: CategoryGroupId,
  @ColumnInfo("name") val name: String?,
  @ColumnInfo("is_income") val isIncome: Boolean = false,
  @ColumnInfo("sort_order") val sortOrder: Float?,
  @ColumnInfo("tombstone") val tombstone: Boolean = false,
  @ColumnInfo("hidden") val isHidden: Boolean = false,
)
