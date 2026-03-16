package aktual.budget.db.dao

import aktual.budget.db.model.CategoryGroup
import aktual.budget.model.CategoryGroupId
import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert

@Dao
interface CategoryGroupsDao {
  @Upsert suspend fun insert(categoryGroup: CategoryGroup)

  @Query(
    "SELECT * FROM category_groups WHERE id IN (:ids) AND tombstone = 0 ORDER BY is_income, sort_order, id"
  )
  suspend fun getWithIds(ids: List<CategoryGroupId>): List<CategoryGroup>
}
