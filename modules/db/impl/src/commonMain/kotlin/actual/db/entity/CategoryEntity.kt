package actual.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
  @PrimaryKey @ColumnInfo("id") val id: String,
  @ColumnInfo("name") val name: String?,
  @ColumnInfo("is_income") val isIncome: Boolean? = false,
  @ColumnInfo("cat_group") val categoryGroup: String?,
  @ColumnInfo("sort_order") val sortOrder: Float?,
  @ColumnInfo("tombstone") val tombstone: Boolean? = false,
  @ColumnInfo("hidden") val isHidden: Boolean = false,
  @ColumnInfo("goal_def") val goalDef: String? = null,
)
