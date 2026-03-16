package aktual.budget.db.model

import aktual.budget.model.CategoryGroupId
import aktual.budget.model.CategoryId
import androidx.room3.ColumnInfo
import androidx.room3.DatabaseView

@DatabaseView(
  viewName = "v_categories",
  value =
    """
    SELECT
      id,
      name,
      is_income,
      hidden,
      cat_group AS `group`,
      sort_order,
      tombstone
    FROM categories
  """,
)
data class VCategories(
  @ColumnInfo(name = "id") val id: CategoryId,
  @ColumnInfo(name = "name") val name: String?,
  @ColumnInfo(name = "is_income") val isIncome: Boolean?,
  @ColumnInfo(name = "hidden") val hidden: Boolean,
  @ColumnInfo(name = "group") val group: CategoryGroupId?,
  @ColumnInfo(name = "sort_order") val sortOrder: Double?,
  @ColumnInfo(name = "tombstone") val tombstone: Boolean?,
)
