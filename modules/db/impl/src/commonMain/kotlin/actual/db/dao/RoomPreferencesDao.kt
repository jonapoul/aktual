package actual.db.dao

import actual.budget.model.BudgetType
import actual.db.api.PreferencesDao
import androidx.room.Dao
import androidx.room.Query

@Dao
interface RoomPreferencesDao : PreferencesDao {
  @Query("SELECT value FROM preferences WHERE id = '$KEY_BUDGET_TYPE'")
  suspend fun getBudgetTypeString(): String?

  override suspend fun getBudgetType(): BudgetType? =
    getBudgetTypeString()?.let(BudgetType::fromString)

  private companion object {
    const val KEY_BUDGET_TYPE = "budgetType"
  }
}
