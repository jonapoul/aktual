package aktual.budget.db.dao

import aktual.budget.db.model.CategoryMapping
import aktual.budget.model.CategoryId
import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert

@Dao
interface CategoryMappingDao {
  @Upsert suspend fun insert(categoryMapping: CategoryMapping)

  @Query("SELECT * FROM category_mapping WHERE transferId = :transferId")
  suspend fun selectByTransferId(transferId: CategoryId): List<CategoryMapping>

  @Query("UPDATE category_mapping SET transferId = :transferId WHERE id = :id")
  suspend fun update(id: CategoryId, transferId: CategoryId)
}
