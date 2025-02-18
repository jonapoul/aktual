package actual.db.api

import actual.budget.model.BudgetType

interface PreferencesDao {
  suspend fun getBudgetType(): BudgetType?
}
