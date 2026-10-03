package aktual.budget.db.dao

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.withResult
import aktual.budget.model.AccountId
import aktual.budget.model.CategoryGroupId
import aktual.budget.model.CategoryId
import aktual.budget.model.PayeeId
import aktual.budget.model.RuleId
import aktual.budget.model.ScheduleId
import app.cash.sqldelight.async.coroutines.awaitAsList
import dev.zacsweers.metro.Inject

/** What running rules against transactions needs to know about the rest of the budget. */
@Inject
class RuleContextDao(database: BudgetDatabase) {
  private val queries = database.ruleContextQueries

  // Every payee, including deleted ones, in table order
  suspend fun payees(): List<PayeeRow> = queries.withResult {
    rulePayees { id, name, tombstone -> PayeeRow(id, name, tombstone == true) }.awaitAsList()
  }

  // Whether each non-deleted account is off budget
  suspend fun accountsOffBudget(): Map<AccountId, Boolean> = queries.withResult {
    ruleAccounts { id, offBudget -> id to (offBudget == true) }.awaitAsList().toMap()
  }

  // Every category's group, including deleted categories
  suspend fun categoryGroups(): Map<CategoryId, CategoryGroupId?> = queries.withResult {
    ruleCategoryGroups { id, group -> id to group }.awaitAsList().toMap()
  }

  // Merged payees and categories: each id mapped to the id it was merged into
  suspend fun idMappings(): Map<String, String> = queries.withResult {
    val payees = rulePayeeMappings { id, target ->
      id.value to target?.value
    }
      .awaitAsList()
      .mapNotNull { (id, target) -> target?.let { id to it } }
    val categories = ruleCategoryMappings { id, target ->
      id.value to target?.value
    }
      .awaitAsList()
      .mapNotNull { (id, target) -> target?.let { id to it } }
    (categories + payees).toMap()
  }

  // The rule behind each schedule, including deleted ones
  suspend fun scheduleRules(): Map<ScheduleId, RuleId> = queries.withResult {
    ruleScheduleRules { id, rule -> id to rule }.awaitAsList().toMap()
  }

  data class PayeeRow(val id: PayeeId, val name: String?, val tombstone: Boolean)
}
