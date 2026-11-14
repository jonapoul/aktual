package aktual.budget.db.dao

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.categoryGroups.GetAllActive
import aktual.budget.db.withResult
import aktual.budget.model.CategoryGroupId
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.coroutines.asFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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

  // Emits whenever the table behind getAllActive() is written to
  fun observeChanges(): Flow<Unit> = queries.getAllActive().asFlow().map {}

  // Hidden groups included
  suspend fun nameMap(): Map<CategoryGroupId, String?> = queries.withResult {
    getAllNames().awaitAsList().associate { it.id to it.name }
  }
}
