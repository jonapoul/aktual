package aktual.budget.db.dao

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.withResult
import aktual.budget.model.CategoryGroupId

class CategoryGroupDao(database: BudgetDatabase) {
  private val queries = database.categoryGroupsQueries

  suspend fun name(id: CategoryGroupId): String = queries.withResult {
    getName(id).executeAsOneOrNull()?.name ?: error("Required name for $id")
  }

  suspend fun names(ids: List<CategoryGroupId>): List<String> = queries.withResult {
    getNames(ids).executeAsList().map { g -> g.name ?: error("Required name for $g") }
  }
}
