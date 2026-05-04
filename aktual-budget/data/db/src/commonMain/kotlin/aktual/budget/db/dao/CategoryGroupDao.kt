package aktual.budget.db.dao

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.categoryGroups.GetAllActive
import aktual.budget.db.withResult
import aktual.budget.model.CategoryGroupId
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull

class CategoryGroupDao(database: BudgetDatabase) {
  private val queries = database.categoryGroupsQueries

  suspend fun name(id: CategoryGroupId): String = queries.withResult {
    getName(id).awaitAsOneOrNull()?.name ?: error("Required name for $id")
  }

  suspend fun names(ids: List<CategoryGroupId>): List<String> = queries.withResult {
    getNames(ids).awaitAsList().map { g -> g.name ?: error("Required name for $g") }
  }

  suspend fun getAllActive(): List<GetAllActive> = queries.withResult {
    getAllActive().awaitAsList()
  }
}
