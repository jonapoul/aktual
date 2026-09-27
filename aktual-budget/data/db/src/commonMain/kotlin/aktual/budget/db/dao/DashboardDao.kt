package aktual.budget.db.dao

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.Dashboard
import aktual.budget.db.Dashboard_pages
import aktual.budget.db.GetPositionAndSize
import aktual.budget.db.withResult
import aktual.budget.db.withoutResult
import aktual.budget.model.DashboardPageId
import aktual.budget.model.WidgetId
import aktual.budget.model.WidgetType
import alakazam.kotlin.CoroutineContexts
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

@Inject
class DashboardDao(database: BudgetDatabase, private val contexts: CoroutineContexts) {
  private val queries = database.dashboardQueries
  private val pageQueries = database.dashboardPagesQueries

  suspend fun insert(
    id: WidgetId,
    page: DashboardPageId,
    type: WidgetType,
    x: Long,
    y: Long,
    meta: JsonObject?,
    width: Long = DEFAULT_WIDTH,
    height: Long = DEFAULT_HEIGHT,
  ) = queries.withoutResult {
    insert(
      id = id,
      type = type,
      width = width,
      height = height,
      x = x,
      y = y,
      meta = meta,
      dashboard_page_id = page,
    )
  }

  fun observePages(): Flow<List<Dashboard_pages>> =
    pageQueries.getAll().asFlow().mapToList(contexts.default).distinctUntilChanged()

  fun observeByPage(page: DashboardPageId): Flow<List<Dashboard>> =
    queries.getByPage(page).asFlow().mapToList(contexts.default).distinctUntilChanged()

  fun observeById(id: WidgetId): Flow<Dashboard?> =
    queries.getById(id).asFlow().mapToOneOrNull(contexts.default).distinctUntilChanged()

  suspend fun deleteById(id: WidgetId): Long = queries.withResult { delete(id) }

  suspend fun getPositionAndSize(page: DashboardPageId): List<GetPositionAndSize> =
    queries.withResult {
      getPositionAndSize(page).awaitAsList()
    }

  // Patches the stored json rather than re-encoding our model, so values we don't recognise are
  // kept
  suspend fun rename(id: WidgetId, name: String) = queries.withoutResult {
    val row = getMeta(id).awaitAsOneOrNull() ?: return@withoutResult
    val meta = row.meta ?: JsonObject(emptyMap())
    updateMeta(JsonObject(meta + ("name" to JsonPrimitive(name))), id)
  }

  companion object {
    const val DEFAULT_WIDTH = 4L
    const val DEFAULT_HEIGHT = 2L
    const val MAX_WIDTH = 12L
  }
}
