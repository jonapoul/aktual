package aktual.budget.reports.vm

import aktual.budget.BudgetSyncController
import aktual.budget.db.DbJson
import aktual.budget.db.dao.DashboardDao
import aktual.budget.db.dao.DatabaseTables.CUSTOM_REPORTS
import aktual.budget.db.dao.DatabaseTables.DASHBOARD
import aktual.budget.db.dao.DatabaseTables.DASHBOARD_PAGES
import aktual.budget.model.CustomReportId
import aktual.budget.model.DashboardPageId
import aktual.budget.model.LocalChange
import aktual.budget.model.MessageValue
import aktual.budget.model.WidgetId
import aktual.budget.model.WidgetType
import aktual.budget.model.messageValue
import aktual.budget.model.tombstone
import dev.zacsweers.metro.Inject
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
    fun change(column: String, value: MessageValue) =
      LocalChange(DASHBOARD, id.value, column, value)

    sync.syncChanges(
      change("id", id.value.messageValue()),
      change("type", type.serialName().messageValue()),
      change("width", MessageValue.Number(DashboardDao.DEFAULT_WIDTH)),
      change("height", MessageValue.Number(DashboardDao.DEFAULT_HEIGHT)),
      change("x", MessageValue.Number(x)),
      change("y", MessageValue.Number(y)),
      change("meta", DbJson.encodeToString(meta).messageValue()),
      change("dashboard_page_id", page.value.messageValue()),
      change("tombstone", false.messageValue()),
    )
  }

  suspend fun renameWidget(id: WidgetId, name: String) = patchMeta(id, "name", name)

  suspend fun setWidgetContent(id: WidgetId, content: String) = patchMeta(id, "content", content)

  private suspend fun patchMeta(id: WidgetId, key: String, value: String) {
    val meta = dao.meta(id) ?: return
    val patched = JsonObject(meta + (key to JsonPrimitive(value)))
    sync.syncChanges(
      LocalChange(DASHBOARD, id.value, "meta", DbJson.encodeToString(patched).messageValue())
    )
  }

  suspend fun deleteWidget(id: WidgetId) = sync.syncChanges(tombstone(DASHBOARD, id.value))

  suspend fun renameCustomReport(id: CustomReportId, name: String) =
    sync.syncChanges(LocalChange(CUSTOM_REPORTS, id.value, "name", name.messageValue()))

  suspend fun insertPage(id: DashboardPageId, name: String) {
    fun change(column: String, value: MessageValue) =
      LocalChange(DASHBOARD_PAGES, id.value, column, value)

    sync.syncChanges(
      change("id", id.value.messageValue()),
      change("name", name.messageValue()),
      change("tombstone", false.messageValue()),
    )
  }

  suspend fun renamePage(id: DashboardPageId, name: String) =
    sync.syncChanges(LocalChange(DASHBOARD_PAGES, id.value, "name", name.messageValue()))

  // Refuses to delete the last page, like upstream. Returns whether the page was deleted
  suspend fun deletePage(id: DashboardPageId): Boolean {
    if (dao.countPages() <= 1) return false
    val widgets = dao.widgetIds(id).map { widget -> tombstone(DASHBOARD, widget.value) }
    sync.syncChanges(listOf(tombstone(DASHBOARD_PAGES, id.value)) + widgets)
    return true
  }

  private fun WidgetType.serialName(): String =
    WidgetType.serializer().descriptor.getElementName(ordinal)
}
