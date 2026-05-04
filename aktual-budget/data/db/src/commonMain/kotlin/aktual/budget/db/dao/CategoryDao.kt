package aktual.budget.db.dao

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.categories.GetAllActive
import aktual.budget.db.withResult
import aktual.budget.model.CategoryId
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull

class CategoryDao(database: BudgetDatabase) {
  private val queries = database.categoriesQueries

  suspend fun name(id: CategoryId): String? = queries.withResult {
    getName(id).awaitAsOneOrNull()?.name
  }

  suspend fun names(ids: List<CategoryId>): List<String> = queries.withResult {
    getNames(ids).awaitAsList().map { c -> c.name ?: error("Required name for $c") }
  }

  suspend fun getAllActive(): List<GetAllActive> = queries.withResult {
    getAllActive().awaitAsList()
  }
}
