package aktual.budget.db.dao

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.Categories
import aktual.budget.db.GetAllActiveGrouped
import aktual.budget.db.categories.GetAllActive
import aktual.budget.db.withResult
import aktual.budget.db.withoutResult
import aktual.budget.model.CategoryId
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import dev.zacsweers.metro.Inject

@Inject
class CategoryDao(database: BudgetDatabase) {
  private val queries = database.categoriesQueries
  private val mappings = database.categoryMappingQueries

  suspend fun insert(id: CategoryId, name: String) = queries.withoutResult {
    insert(
      Categories(
        id = id,
        name = name,
        is_income = false,
        cat_group = null,
        sort_order = null,
        tombstone = false,
        hidden = false,
        goal_def = null,
        template_settings = null,
        cleanup_def = null,
      ),
    )
    mappings.insert(id = id, transferId = id)
  }

  // getStartingBalancePayee(): the income category named "Starting Balances", or else any income
  // category
  suspend fun startingBalanceCategory(): CategoryId? = queries.withResult {
    getStartingBalancesCategory().awaitAsOneOrNull() ?: getFirstIncomeCategory().awaitAsOneOrNull()
  }

  suspend fun name(id: CategoryId): String? = queries.withResult {
    getName(id).awaitAsOneOrNull()?.name
  }

  suspend fun names(ids: List<CategoryId>): List<String> = queries.withResult {
    getNames(ids).awaitAsList().map { c -> c.name ?: error("Required name for $c") }
  }

  suspend fun getAllActive(): List<GetAllActive> = queries.withResult {
    getAllActive().awaitAsList()
  }

  suspend fun getAllActiveGrouped(): List<GetAllActiveGrouped> = queries.withResult {
    getAllActiveGrouped().awaitAsList()
  }
}
