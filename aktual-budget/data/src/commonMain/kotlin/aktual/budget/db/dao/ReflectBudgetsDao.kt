package aktual.budget.db.dao

import aktual.budget.db.model.ReflectBudget
import androidx.room3.Dao
import androidx.room3.Query

@Dao
interface ReflectBudgetsDao {
  @Query(
    """
    SELECT b.*
    FROM reflect_budgets b
    LEFT JOIN categories c ON c.id = b.category
    WHERE c.tombstone = 0
    """
  )
  suspend fun getWithCategory(): List<ReflectBudget>
}
