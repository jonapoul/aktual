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
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
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
  // Places the widget after the last one on the page
  suspend fun addWidget(id: WidgetId, page: DashboardPageId, type: WidgetType, meta: JsonObject) {
    val (x, y) = newWidgetPosition(page)
    insertWidget(id, page, type, x, y, meta)
  }

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

  suspend fun setFormula(id: WidgetId, edits: FormulaEdits) =
    patchMeta(
      id,
      listOfNotNull(
          edits.name?.let { "name" to JsonPrimitive(it) },
          "formula" to JsonPrimitive(edits.formula),
          "colorFormula" to JsonPrimitive(edits.colorFormula),
          "fontSizeMode" to
            DbJson.encodeToJsonElement(FontSizeMode.serializer(), edits.fontSizeMode),
          "staticFontSize" to JsonPrimitive(edits.staticFontSize),
          "showTitle" to JsonPrimitive(edits.showTitle),
          "queries" to DbJson.encodeToJsonElement(QUERIES_SERIALIZER, edits.queries),
        )
        .toMap(),
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

  // packages/loot-core/src/server/reports/app.ts deleteReport(). Other widgets showing the report
  // are left in place, and show it as missing
  suspend fun deleteCustomReport(id: CustomReportId) =
    sync.syncChanges(tombstone(CUSTOM_REPORTS, id.value))

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

  private suspend fun newWidgetPosition(page: DashboardPageId): Pair<Long, Long> {
    val positions = dao.getPositionAndSize(page)
    if (positions.isEmpty()) return 0L to 0L

    val highest = positions.maxWith(compareBy({ it.y }, { it.x }))
    val x = highest.x ?: 0L
    val y = highest.y ?: 0L
    val w = highest.width ?: 0L

    return if (x + w >= DashboardDao.MAX_WIDTH) {
      // start of the next row down
      0L to y + DashboardDao.DEFAULT_HEIGHT
    } else {
      // next column over on the same row
      x + DashboardDao.DEFAULT_WIDTH to y
    }
  }

  private fun WidgetType.serialName(): String =
    WidgetType.serializer().descriptor.getElementName(ordinal)

  private companion object {
    val QUERIES_SERIALIZER = MapSerializer(String.serializer(), FormulaQuery.serializer())

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
