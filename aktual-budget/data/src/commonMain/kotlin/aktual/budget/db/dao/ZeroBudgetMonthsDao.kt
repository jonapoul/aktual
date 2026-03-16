package aktual.budget.db.dao

import aktual.budget.db.model.ZeroBudgetMonth
import aktual.budget.model.ZeroBudgetMonthId
import androidx.room3.Dao
import androidx.room3.Query

@Dao
interface ZeroBudgetMonthsDao {
  @Query("SELECT * FROM zero_budget_months") suspend fun getAll(): List<ZeroBudgetMonth>

  @Query("UPDATE zero_budget_months SET buffered = :buffered WHERE id = :id")
  suspend fun update(id: ZeroBudgetMonthId, buffered: Boolean)

  @Query("SELECT EXISTS(SELECT 1 FROM zero_budget_months WHERE id = :id)")
  suspend fun idExists(id: ZeroBudgetMonthId): Boolean
}
