package aktual.budget.reports.vm

import aktual.budget.BudgetSyncController
import aktual.budget.db.DbJson
import aktual.budget.db.dao.CustomReportsDao
import aktual.budget.db.dao.DatabaseTables.CUSTOM_REPORTS
import aktual.budget.model.BalanceType
import aktual.budget.model.Condition
import aktual.budget.model.ConditionOp
import aktual.budget.model.CustomReportId
import aktual.budget.model.CustomReportMode
import aktual.budget.model.DateRangeType
import aktual.budget.model.GraphType
import aktual.budget.model.GroupBy
import aktual.budget.model.Interval
import aktual.budget.model.LocalChange
import aktual.budget.model.MessageValue
import aktual.budget.model.ReportDate
import aktual.budget.model.SortBy
import aktual.budget.model.messageValue
import aktual.core.Calendar
import dev.zacsweers.metro.Inject
import kotlinx.datetime.minus
import kotlinx.datetime.yearMonth
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

enum class ReportNameError {
  Blank,
  Taken,
}

// Writes custom reports through the sync log, like packages/loot-core/src/server/reports/app.ts
// createReport() and updateReport(). Columns are encoded as reportModel.fromJS() and
// convertForInsert() would, so upstream reads them back identically
@Inject
internal class CustomReportSync(
  private val dao: CustomReportsDao,
  private val sync: BudgetSyncController,
  private val calendar: Calendar,
) {
  // reportNameExists(): a report can keep its own name, but can't take another's
  suspend fun validateName(name: String, id: CustomReportId? = null): ReportNameError? {
    if (name.isBlank()) return Blank
    val existing = dao.getIdByName(name) ?: return null
    return if (existing == id) null else Taken
  }

  suspend fun insert(id: CustomReportId, name: String, config: CustomReportConfig) {
    // A live report doesn't read its dates, so these are upstream's defaultReport values
    val today = calendar.today()
    val dates =
      mapOf(
        "start_date" to
          ReportDate.Date(today.yearMonth.minus(DEFAULT_MONTHS_BACK, MONTH).firstDay).value(),
        "end_date" to ReportDate.Date(today).value(),
      )
    val columns =
      mapOf("id" to id.value.messageValue(), "name" to name.messageValue()) +
        dates +
        config.toColumns(metadata = null) +
        ("tombstone" to false.messageValue())
    sync.syncChanges(columns.toChanges(id))
  }

  // Only writes the columns that changed, so it doesn't overwrite changes made elsewhere
  suspend fun update(id: CustomReportId, saved: CustomReportConfig, config: CustomReportConfig) {
    val metadata = dao[id]?.metadata
    val old = saved.toColumns(metadata)
    val changes = config.toColumns(metadata).filter { (column, value) -> old[column] != value }
    if (changes.isNotEmpty()) sync.syncChanges(changes.toChanges(id))
  }

  private fun Map<String, MessageValue>.toChanges(id: CustomReportId): List<LocalChange> =
    map { (column, value) ->
      LocalChange(CUSTOM_REPORTS, id.value, column, value)
    }

  // selected_categories and color_scheme are left alone, since upstream no longer writes them
  private fun CustomReportConfig.toColumns(metadata: JsonObject?): Map<String, MessageValue> =
    buildMap {
      when (dates) {
        is Live -> {
          put("date_static", false.messageValue())
          put("date_range", DateRangeType.serializer().encode(dates.type))
          put("include_current", dates.includeCurrent.messageValue())
        }
        is Static -> {
          put("date_static", true.messageValue())
          put("start_date", ReportDate.Date(dates.start).value())
          put("end_date", ReportDate.Date(dates.end).value())
        }
      }
      put("mode", CustomReportMode.serializer().encode(mode))
      put("group_by", GroupBy.serializer().encode(groupBy))
      put("sort_by", SortBy.serializer().encode(sortBy))
      put("interval", Interval.serializer().encode(interval))
      put("balance_type", BalanceType.serializer().encode(balanceType))
      put("show_empty", showEmpty.messageValue())
      put("show_offbudget", showOffBudget.messageValue())
      put("show_hidden", showHidden.messageValue())
      put("show_uncategorized", showUncategorized.messageValue())
      put("trim_intervals", trimIntervals.messageValue())
      put("show_trend_lines", showTrendLines.messageValue())
      put("graph_type", GraphType.serializer().encode(graphType))
      put("conditions", DbJson.encodeToString(CONDITIONS, conditions).messageValue())
      put("conditions_op", ConditionOp.serializer().encode(conditionsOp))
      val withScope = metadata.orEmpty() + ("tagScope" to tagScope.toJson())
      put("metadata", DbJson.encodeToString(JsonObject(withScope)).messageValue())
    }

  // packages/desktop-client/src/components/reports/tagScope.ts
  private fun CustomTagScope.toJson(): JsonObject =
    when (this) {
      All -> JsonObject(mapOf("mode" to JsonPrimitive("all")))
      is Selected ->
        JsonObject(
          mapOf(
            "mode" to JsonPrimitive("selected"),
            "tagIds" to JsonArray(tagIds.map { JsonPrimitive(it.value) }),
          ),
        )
    }

  private fun ReportDate.value(): MessageValue = toString().messageValue()

  private fun <T> KSerializer<T>.encode(value: T): MessageValue =
    DbJson.encodeToJsonElement(this, value).jsonPrimitive.content.messageValue()

  private companion object {
    val CONDITIONS = ListSerializer(Condition.serializer())
    const val DEFAULT_MONTHS_BACK = 5
  }
}
