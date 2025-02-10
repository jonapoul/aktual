package actual.db.entity

import actual.budget.model.CategoryGroupId
import actual.budget.model.CategoryId
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "categories",
  foreignKeys = [
    ForeignKey(CategoryGroupEntity::class, parentColumns = ["id"], childColumns = ["cat_group"]),
  ],
  indices = [
    Index(value = ["cat_group"]),
  ],
)
data class CategoryEntity(
  @PrimaryKey @ColumnInfo("id") val id: CategoryId,
  @ColumnInfo("name") val name: String?,
  @ColumnInfo("is_income") val isIncome: Boolean = false,
  @ColumnInfo("cat_group") val categoryGroup: CategoryGroupId?,
  @ColumnInfo("sort_order") val sortOrder: Float?,
  @ColumnInfo("tombstone") val tombstone: Boolean = false,
  @ColumnInfo("hidden") val isHidden: Boolean = false,
  @ColumnInfo("goal_def") val goalDef: String? = null,
)
