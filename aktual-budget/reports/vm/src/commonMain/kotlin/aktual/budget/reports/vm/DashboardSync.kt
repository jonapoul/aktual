package aktual.budget.reports.vm

import aktual.budget.BudgetSyncController
import aktual.budget.db.DbJson
import aktual.budget.db.dao.DashboardDao
import aktual.budget.db.dao.DatabaseTables.CUSTOM_REPORTS
import aktual.budget.db.dao.DatabaseTables.DASHBOARD
import aktual.budget.db.dao.DatabaseTables.DASHBOARD_PAGES
import aktual.budget.model.CustomReportId
import aktual.budget.model.DashboardPageId
import aktual.budget.model.WidgetId
import aktual.budget.model.WidgetType
import aktual.budget.model.localChange
import aktual.budget.model.tombstone
import dev.zacsweers.metro.Inject
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

// Writes dashboard changes through the sync log, so they reach the server
@Inject
internal class DashboardSync(
  private val dao: DashboardDao,
  private val sync: BudgetSyncController,
) {
  suspend fun insertWidget(
    id: WidgetId,
    page: DashboardPageId,
    type: WidgetType,
    x: Long,
    y: Long,
    meta: JsonObject,
  ) {
    sync.syncChanges(
      localChange(DASHBOARD, id.value, "id", id.value),
      localChange(DASHBOARD, id.value, "type", type.serialName()),
      localChange(DASHBOARD, id.value, "width", DashboardDao.DEFAULT_WIDTH),
      localChange(DASHBOARD, id.value, "height", DashboardDao.DEFAULT_HEIGHT),
      localChange(DASHBOARD, id.value, "x", x),
      localChange(DASHBOARD, id.value, "y", y),
      localChange(DASHBOARD, id.value, "meta", DbJson.encodeToString(meta)),
      localChange(DASHBOARD, id.value, "dashboard_page_id", page.value),
      localChange(DASHBOARD, id.value, "tombstone", false),
    )
  }

  suspend fun renameWidget(id: WidgetId, name: String) =
    patchMeta(id, "name" to JsonPrimitive(name))

  suspend fun setWidgetText(id: WidgetId, content: String, align: TextAlign) =
    patchMeta(
      id,
      "content" to JsonPrimitive(content),
      "text_align" to DbJson.encodeToJsonElement(TextAlign.serializer(), align),
    )

  // Writes the plan's settings over the stored meta, like upstream's save. Nulls are written out,
  // since a missing inflationMean means upstream's default rather than no inflation
  suspend fun setMonteCarloConfig(id: WidgetId, meta: MonteCarloReportMeta) {
    val encoded = ExplicitNullsJson.encodeToJsonElement(MonteCarloReportMeta.serializer(), meta)
    patchMeta(id, (encoded as JsonObject).filterKeys { it in MONTE_CARLO_CONFIG_KEYS })
  }

  private suspend fun patchMeta(id: WidgetId, vararg values: Pair<String, JsonElement>) =
    patchMeta(id, values.toMap())

  private suspend fun patchMeta(id: WidgetId, values: Map<String, JsonElement>) {
    val meta = dao.meta(id) ?: return
    val patched = JsonObject(meta + values)
    sync.syncChanges(
      localChange(DASHBOARD, id.value, "meta", DbJson.encodeToString(patched)),
    )
  }

  suspend fun deleteWidget(id: WidgetId) = sync.syncChanges(tombstone(DASHBOARD, id.value))

  suspend fun renameCustomReport(id: CustomReportId, name: String) =
    sync.syncChanges(localChange(CUSTOM_REPORTS, id.value, "name", name))

  suspend fun insertPage(id: DashboardPageId, name: String) {
    sync.syncChanges(
      localChange(DASHBOARD_PAGES, id.value, "id", id.value),
      localChange(DASHBOARD_PAGES, id.value, "name", name),
      localChange(DASHBOARD_PAGES, id.value, "tombstone", false),
    )
  }

  suspend fun renamePage(id: DashboardPageId, name: String) =
    sync.syncChanges(localChange(DASHBOARD_PAGES, id.value, "name", name))

  // Refuses to delete the last page, like upstream. Returns whether the page was deleted
  suspend fun deletePage(id: DashboardPageId): Boolean {
    if (dao.countPages() <= 1) return false
    val widgets = dao.widgetIds(id).map { widget -> tombstone(DASHBOARD, widget.value) }
    sync.syncChanges(listOf(tombstone(DASHBOARD_PAGES, id.value)) + widgets)
    return true
  }

  private fun WidgetType.serialName(): String =
    WidgetType.serializer().descriptor.getElementName(ordinal)

  private companion object {
    val ExplicitNullsJson = Json {
      encodeDefaults = true
      explicitNulls = true
    }

    // The meta keys the Monte Carlo configuration owns, leaving the name and anything unmodelled
    val MONTE_CARLO_CONFIG_KEYS =
      setOf(
        "pots",
        "withdrawalStrategy",
        "returnModel",
        "withdrawalRule",
        "minimumSpending",
        "spendingPhases",
        "contributions",
        "incomeStreams",
        "inflationMean",
        "inflationStdDev",
        "taxModel",
        "taxBands",
        "currentAge",
        "targetAge",
        "simulationCount",
      )
  }
}
