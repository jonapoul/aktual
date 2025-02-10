package actual.db.dao

import actual.budget.model.BudgetType
import androidx.room.Dao
import androidx.room.Query

@Dao
interface PreferencesDao {
  @Query("SELECT value FROM preferences WHERE id = '$KEY_BUDGET_TYPE'")
  suspend fun getBudgetTypeString(): String?

  private companion object {
    const val KEY_BUDGET_TYPE = "budgetType"
  }
}

suspend fun PreferencesDao.getBudgetType(): BudgetType? =
  getBudgetTypeString()?.let(BudgetType::fromString)
