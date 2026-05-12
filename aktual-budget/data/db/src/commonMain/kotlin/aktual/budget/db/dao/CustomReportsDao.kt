package aktual.budget.db.dao

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.CustomReports
import aktual.budget.db.withResult
import aktual.budget.model.CustomReportId
import alakazam.kotlin.CoroutineContexts
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.JsonObject

@Inject
class CustomReportsDao(database: BudgetDatabase, private val contexts: CoroutineContexts) {
  private val queries = database.customReportsQueries

  suspend fun insert(reports: CustomReports): Long = queries.withResult { insert(reports) }

  suspend operator fun get(id: CustomReportId): CustomReports? = queries.withResult {
    getById(id).awaitAsOneOrNull()
  }

  fun observeMetadataById(id: CustomReportId): Flow<JsonObject?> =
    queries
      .getMetadataById(id)
      .asFlow()
      .mapToOneOrNull(contexts.default)
      .map { it?.metadata }
      .distinctUntilChanged()

  suspend fun getIds(): List<CustomReportId> = queries.withResult { getIds().awaitAsList() }

  suspend fun getIdByName(name: String): CustomReportId? = queries.withResult {
    getIdByName(name).awaitAsOneOrNull()
  }

  suspend fun deleteById(id: CustomReportId): Long = queries.withResult { delete(id) }

  suspend fun rename(id: CustomReportId, name: String): Long = queries.withResult {
    rename(name, id)
  }
}
