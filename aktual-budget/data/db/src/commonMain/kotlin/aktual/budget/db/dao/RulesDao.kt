package aktual.budget.db.dao

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.Rules
import aktual.budget.db.withResult
import aktual.budget.model.RuleId
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull

class RulesDao(database: BudgetDatabase) {
  private val queries = database.rulesQueries

  suspend fun getAll(): List<Rules> = queries.withResult { getAll().awaitAsList() }

  suspend operator fun get(id: RuleId): Rules? = queries.withResult { get(id).awaitAsOneOrNull() }

  suspend fun tombstone(ids: Set<RuleId>): Long = queries.withResult { tombstone(ids) }

  suspend fun insert(rule: Rules): Long = queries.withResult {
    with(rule) {
      insert(
        id = id,
        stage = stage,
        conditions = conditions,
        actions = actions,
        tombstone = tombstone,
        conditions_op = conditions_op,
      )
    }
  }
}
