package aktual.budget.db.model

import aktual.budget.model.CategoryGroupId
import aktual.budget.model.CategoryId
import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.json.JsonElement

@Entity(tableName = "categories")
data class Category(
  @PrimaryKey @ColumnInfo(name = "id") val id: CategoryId,
  @ColumnInfo(name = "name") val name: String?,
  @ColumnInfo(name = "is_income") val isIncome: Boolean? = false,
  @ColumnInfo(name = "cat_group") val catGroup: CategoryGroupId?,
  @ColumnInfo(name = "sort_order") val sortOrder: Double?,
  @ColumnInfo(name = "tombstone") val tombstone: Boolean? = false,
  @ColumnInfo(name = "hidden") val hidden: Boolean = false,
  @ColumnInfo(name = "goal_def") val goalDef: JsonElement? = null,
)
