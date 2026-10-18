package aktual.budget.reports.vm

import aktual.budget.BudgetSyncController
import aktual.budget.db.BudgetDatabase
import aktual.budget.db.DbJson
import aktual.budget.db.dao.CustomReportsDao
import aktual.budget.db.dao.DatabaseTables.CUSTOM_REPORTS
import aktual.budget.model.Condition
import aktual.budget.model.CustomReportId
import aktual.budget.model.LocalChange
import aktual.budget.model.MessageValue
import aktual.budget.model.TagId
import aktual.budget.model.localChange
import aktual.budget.model.tombstone
import aktual.core.Calendar
import aktual.test.TestSyncController
import aktual.test.runDatabaseTest
import alakazam.test.TestCoroutineContexts
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.datetime.LocalDate
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

// The upstream rows are what packages/loot-core/src/server/reports/app.ts reportModel.fromJS()
// gives, passed through convertForInsert() with the custom_reports schema
class CustomReportSyncTest {
  @Test
  fun `Insert the default report like upstream`() = runDatabaseTest { scope ->
    val controller = TestSyncController()
    val sync = customReportSync(scope.testScheduler, controller)

    sync.insert(ID, "Spending", CustomReportConfig.Default)

    assertThat(controller.changes.toRow()).isEqualTo(UPSTREAM_DEFAULT.toRow() + TOMBSTONE)
  }

  @Test
  fun `Insert a static report like upstream`() = runDatabaseTest { scope ->
    val controller = TestSyncController()
    val sync = customReportSync(scope.testScheduler, controller)

    sync.insert(ID, "Tags", STATIC_CONFIG)

    // A static report has no live range, so those columns aren't written
    val expected = UPSTREAM_STATIC.toRow() - "date_range" - "include_current" - "metadata"
    assertThat(controller.changes.toRow() - "metadata").isEqualTo(expected + TOMBSTONE)
    assertThat(controller.changes.toRow()["metadata"])
      .isEqualTo(json("""{"tagScope":{"mode":"selected","tagIds":["tag-1","tag-2"]}}"""))
  }

  @Test
  fun `Upstream's rows read back as the config they were written from`() =
    runSyncedDatabaseTest { scope, controller ->
      controller.syncChanges(UPSTREAM_DEFAULT.toChanges("default"))
      controller.syncChanges(UPSTREAM_STATIC.toChanges("static"))
      val dao = dao(scope.testScheduler)

      assertThat(dao[CustomReportId("default")]?.toConfig()).isEqualTo(CustomReportConfig.Default)
      assertThat(dao[CustomReportId("static")]?.toConfig()).isEqualTo(STATIC_CONFIG)
    }

  @Test
  fun `Inserted reports read back unchanged`() = runSyncedDatabaseTest { scope, controller ->
    val dao = dao(scope.testScheduler)
    val sync = CustomReportSync(dao, controller, CALENDAR)

    sync.insert(ID, "Spending", CustomReportConfig.Default)
    sync.insert(CustomReportId("other"), "Tags", STATIC_CONFIG)

    assertThat(dao[ID]?.name).isEqualTo("Spending")
    assertThat(dao[ID]?.toConfig()).isEqualTo(CustomReportConfig.Default)
    assertThat(dao[CustomReportId("other")]?.toConfig()).isEqualTo(STATIC_CONFIG)
  }

  @Test
  fun `Update only writes the changed columns`() = runDatabaseTest { scope ->
    val controller = TestSyncController()
    val sync = customReportSync(scope.testScheduler, controller)
    val saved = CustomReportConfig.Default

    sync.update(ID, saved, saved.copy(showEmpty = true, graphType = LineGraph))

    assertThat(controller.changes)
      .containsExactly(
        localChange(CUSTOM_REPORTS, ID.value, "show_empty", true),
        localChange(CUSTOM_REPORTS, ID.value, "graph_type", "LineGraph"),
      )
  }

  @Test
  fun `Update with no changes writes nothing`() = runDatabaseTest { scope ->
    val controller = TestSyncController()
    val sync = customReportSync(scope.testScheduler, controller)

    sync.update(ID, CustomReportConfig.Default, CustomReportConfig.Default)

    assertThat(controller.changes).isEqualTo(emptyList())
  }

  @Test
  fun `Switching to static dates writes the dates`() = runDatabaseTest { scope ->
    val controller = TestSyncController()
    val sync = customReportSync(scope.testScheduler, controller)
    val saved = CustomReportConfig.Default
    val dates = CustomReportDates.Static(LocalDate(2025, 1, 1), LocalDate(2025, 12, 31))

    sync.update(ID, saved, saved.copy(dates = dates))

    assertThat(controller.changes)
      .containsExactly(
        localChange(CUSTOM_REPORTS, ID.value, "date_static", true),
        localChange(CUSTOM_REPORTS, ID.value, "start_date", "2025-01-01"),
        localChange(CUSTOM_REPORTS, ID.value, "end_date", "2025-12-31"),
      )
  }

  @Test
  fun `Changing the tag scope keeps other metadata`() = runSyncedDatabaseTest { scope, controller ->
    controller.syncChanges(
      listOf(
        localChange(CUSTOM_REPORTS, ID.value, "id", ID.value),
        localChange(
          CUSTOM_REPORTS,
          ID.value,
          "metadata",
          """{"other":1,"tagScope":{"mode":"all"}}""",
        ),
      ),
    )
    val dao = dao(scope.testScheduler)
    val sync = CustomReportSync(dao, controller, CALENDAR)
    val saved = CustomReportConfig.Default
    val scoped = CustomTagScope.Selected(persistentListOf(TagId("tag-1")))

    sync.update(ID, saved, saved.copy(tagScope = scoped))

    assertThat(dao[ID]?.metadata)
      .isEqualTo(json("""{"other":1,"tagScope":{"mode":"selected","tagIds":["tag-1"]}}"""))
  }

  @Test
  fun `Names must be set`() = runDatabaseTest { scope ->
    val sync = customReportSync(scope.testScheduler, TestSyncController())

    assertThat(sync.validateName("")).isEqualTo(Blank)
    assertThat(sync.validateName("  ")).isEqualTo(Blank)
  }

  @Test
  fun `Names must not be used by another report`() = runSyncedDatabaseTest { scope, controller ->
    val dao = dao(scope.testScheduler)
    val sync = CustomReportSync(dao, controller, CALENDAR)
    sync.insert(ID, "Spending", CustomReportConfig.Default)

    assertThat(sync.validateName("Spending")).isEqualTo(Taken)
    assertThat(sync.validateName("Spending", CustomReportId("other"))).isEqualTo(Taken)
    assertThat(sync.validateName("Spending", ID)).isNull()
    assertThat(sync.validateName("Other")).isNull()
  }

  @Test
  fun `Deleted reports free up their name`() = runSyncedDatabaseTest { scope, controller ->
    val dao = dao(scope.testScheduler)
    val sync = CustomReportSync(dao, controller, CALENDAR)
    sync.insert(ID, "Spending", CustomReportConfig.Default)
    controller.syncChanges(listOf(tombstone(CUSTOM_REPORTS, ID.value)))

    assertThat(sync.validateName("Spending")).isNull()
  }

  private fun json(string: String): JsonObject = Json.decodeFromString(string)

  private fun BudgetDatabase.dao(scheduler: TestCoroutineScheduler) =
    CustomReportsDao(this, TestCoroutineContexts(StandardTestDispatcher(scheduler)))

  private fun BudgetDatabase.customReportSync(
    scheduler: TestCoroutineScheduler,
    controller: BudgetSyncController,
  ) = CustomReportSync(dao(scheduler), controller, CALENDAR)

  // JSON columns are parsed, so they compare regardless of key order
  private fun List<LocalChange>.toRow(): Map<String, JsonElement> = associate { change ->
    change.column to
      when (val value = change.value) {
        is MessageValue.Number -> JsonPrimitive(value.value)
        is MessageValue.String ->
          if (change.column in JSON_COLUMNS) {
            Json.parseToJsonElement(value.value)
          } else {
            JsonPrimitive(value.value)
          }
        else -> error("Unexpected $value")
      }
  }

  private fun String.toRow(): Map<String, JsonElement> =
    Json.parseToJsonElement(this).jsonObject.mapValues { (column, value) ->
      if (column in JSON_COLUMNS) Json.parseToJsonElement(value.jsonPrimitive.content) else value
    }

  private fun String.toChanges(id: String): List<LocalChange> =
    Json.parseToJsonElement(this).jsonObject.map { (column, element) ->
      val primitive = element.jsonPrimitive
      val value = if (column == "id") id else primitive.content
      primitive.longOrNull?.let { localChange(CUSTOM_REPORTS, id, column, it) }
        ?: localChange(CUSTOM_REPORTS, id, column, value)
    }

  private companion object {
    val ID = CustomReportId("report-1")
    val CALENDAR = Calendar { LocalDate(2026, 10, 18) }
    val JSON_COLUMNS = setOf("conditions", "metadata")
    val TOMBSTONE = "tombstone" to JsonPrimitive(0)

    // defaultReport, saved on 2026-10-18
    val UPSTREAM_DEFAULT =
      """
      {"id":"report-1","name":"Spending","start_date":"2026-05-01","end_date":"2026-10-18",
      "date_static":0,"date_range":"Last 6 months","mode":"total","group_by":"Category",
      "sort_by":"desc","interval":"Monthly","balance_type":"Payment","show_empty":0,
      "show_offbudget":0,"show_hidden":1,"show_uncategorized":1,"trim_intervals":0,
      "show_trend_lines":0,"include_current":1,"graph_type":"BarGraph","conditions":"[]",
      "conditions_op":"and","metadata":"{\"tagScope\":{\"mode\":\"all\"}}"}
      """
        .trimIndent()

    val UPSTREAM_STATIC =
      """
      {"id":"report-1","name":"Tags","start_date":"2025-01-01","end_date":"2025-12-31",
      "date_static":1,"date_range":"Last 6 months","mode":"time","group_by":"Tag",
      "sort_by":"budget","interval":"Yearly","balance_type":"Net","show_empty":1,
      "show_offbudget":1,"show_hidden":0,"show_uncategorized":0,"trim_intervals":1,
      "show_trend_lines":1,"include_current":0,"graph_type":"LineGraph",
      "conditions":"[{\"field\":\"category\",\"op\":\"oneOf\",\"value\":[\"cat-1\",\"cat-2\"],\"type\":\"id\"},{\"field\":\"amount\",\"op\":\"gt\",\"value\":1000,\"type\":\"number\"},{\"field\":\"notes\",\"op\":\"contains\",\"value\":\"rent\",\"type\":\"string\"}]",
      "conditions_op":"or",
      "metadata":"{\"tagScope\":{\"mode\":\"selected\",\"tagIds\":[\"tag-1\",\"tag-2\"]}}"}
      """
        .trimIndent()

    val STATIC_CONFIG =
      CustomReportConfig(
        mode = Time,
        groupBy = Tag,
        balanceType = Net,
        graphType = LineGraph,
        interval = Yearly,
        dates = CustomReportDates.Static(LocalDate(2025, 1, 1), LocalDate(2025, 12, 31)),
        showEmpty = true,
        showOffBudget = true,
        showHidden = false,
        showUncategorized = false,
        selectedCategories = persistentListOf(),
        conditions =
          DbJson.decodeFromString(
              ListSerializer(Condition.serializer()),
              Json.parseToJsonElement(UPSTREAM_STATIC)
                .jsonObject
                .getValue("conditions")
                .jsonPrimitive
                .content,
            )
            .toImmutableList(),
        conditionsOp = Or,
        sortBy = Budget,
        trimIntervals = true,
        showTrendLines = true,
        colorScheme = null,
        tagScope = CustomTagScope.Selected(persistentListOf(TagId("tag-1"), TagId("tag-2"))),
      )
  }
}
