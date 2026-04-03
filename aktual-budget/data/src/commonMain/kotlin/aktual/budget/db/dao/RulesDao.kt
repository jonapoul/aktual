package aktual.budget.db.dao

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.Rules
import aktual.budget.db.withResult
import aktual.budget.model.RuleId

class RulesDao(database: BudgetDatabase) {
  private val queries = database.rulesQueries

  suspend fun getAll(): List<Rules> = queries.withResult { getAll().executeAsList() }

  suspend fun delete(id: RuleId) = queries.withResult { delete(id) }
}
