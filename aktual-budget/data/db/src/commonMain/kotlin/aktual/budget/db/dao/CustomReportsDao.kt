package aktual.budget.db.dao

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.CustomReports
import aktual.budget.db.withResult
import aktual.budget.model.CustomReportId
import alakazam.kotlin.CoroutineContexts
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Inject
class CustomReportsDao(database: BudgetDatabase, private val contexts: CoroutineContexts) {
  private val queries = database.customReportsQueries

  suspend fun insert(reports: CustomReports): Long = queries.withResult { insert(reports) }

  suspend operator fun get(id: CustomReportId): CustomReports? = queries.withResult {
    getById(id).awaitAsOneOrNull()
  }

  fun observeNames(): Flow<Map<CustomReportId, String>> =
    queries
      .getNames()
      .asFlow()
      .mapToList(contexts.default)
      .map { rows -> rows.associate { it.id to it.name } }
      .distinctUntilChanged()

  suspend fun getIds(): List<CustomReportId> = queries.withResult { getIds().awaitAsList() }

  suspend fun getIdByName(name: String): CustomReportId? = queries.withResult {
    getIdByName(name).awaitAsOneOrNull()
  }
}
