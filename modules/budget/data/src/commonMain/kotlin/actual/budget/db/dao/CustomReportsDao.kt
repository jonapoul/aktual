package actual.budget.db.dao

import actual.budget.db.BudgetDatabase
import actual.budget.db.CustomReports
import actual.budget.db.withResult
import actual.budget.model.CustomReportId
import actual.budget.model.ReportMetadata
import alakazam.kotlin.core.CoroutineContexts
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class CustomReportsDao(database: BudgetDatabase, private val contexts: CoroutineContexts) {
  private val queries = database.customReportsQueries

  suspend fun insert(reports: CustomReports): Long = queries.withResult { insert(reports) }

  fun observeMetadataById(id: CustomReportId): Flow<ReportMetadata?> = queries
    .getMetadataById(id)
    .asFlow()
    .mapToOneOrNull(contexts.default)
    .map { it?.metadata }
    .distinctUntilChanged()

  suspend fun getIds(): List<CustomReportId> = queries.withResult { getIds().executeAsList() }

  suspend fun getIdByName(name: String): List<CustomReportId> =
    queries.withResult { getIdByName(name).executeAsList() }

  suspend fun deleteById(id: CustomReportId): Long = queries.withResult { delete(id) }
}
