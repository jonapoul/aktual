package aktual.budget.db.dao

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.withResult
import aktual.budget.model.CategoryId

class CategoryDao(database: BudgetDatabase) {
  private val queries = database.categoriesQueries

  suspend fun name(id: CategoryId): String? = queries.withResult {
    getName(id).executeAsOneOrNull()?.name
  }

  suspend fun names(ids: List<CategoryId>): List<String> = queries.withResult {
    getNames(ids).executeAsList().map { c -> c.name ?: error("Required name for $c") }
  }
}
