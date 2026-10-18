package aktual.budget.reports.vm.report

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.Dashboard_pages
import aktual.budget.db.dao.BudgetDao
import aktual.budget.db.dao.CustomReportsDao
import aktual.budget.db.dao.DashboardDao
import aktual.budget.db.dao.ReportsDao
import aktual.budget.db.dao.TagsDao
import aktual.budget.db.withoutResult
import aktual.budget.model.CustomReportId
import aktual.budget.model.DashboardPageId
import aktual.budget.model.WidgetId
import aktual.budget.reports.vm.CustomReportConfig
import aktual.budget.reports.vm.CustomReportSync
import aktual.budget.reports.vm.DashboardSync
import aktual.budget.reports.vm.QueryEditorDataLoader
import aktual.budget.reports.vm.chartDataLoader
import aktual.budget.reports.vm.emptyCustomReport
import aktual.budget.reports.vm.report.CustomReportState.Loaded
import aktual.budget.reports.vm.runSyncedDatabaseTest
import aktual.budget.reports.vm.toConfig
import aktual.budget.reports.vm.withMode
import aktual.core.Calendar
import aktual.test.assertThatNextEmissionIsEqualTo
import alakazam.test.TestCoroutineContexts
import app.cash.turbine.test
import assertk.all
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import assertk.assertions.isTrue
import assertk.assertions.prop
import kotlin.test.Test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class NewCustomReportViewModelTest {
  @Test
  fun `Starts editing upstream's default report`() = runNewTest {
    val state = viewModel.state.first { it is Loaded } as Loaded
    assertThat(state).all {
      prop(Loaded::draft).isEqualTo(Default)
      prop(Loaded::isEditing).isTrue()
      prop(Loaded::isNew).isTrue()
    }
  }

  @Test
  fun `Saving creates the report and a widget for it`() = runNewTest {
    viewModel.startEditing()
    viewModel.edit { it.withMode(Time) }

    viewModel.savedEvent.test {
      viewModel.save("Spending")
      assertThatNextEmissionIsEqualTo(WIDGET)
    }

    val report = customReports[REPORT]
    assertThat(report?.name).isEqualTo("Spending")
    assertThat(report?.toConfig()).isEqualTo(CustomReportConfig.Default.withMode(Time))

    val widget = dashboard.observeByPage(PAGE).first().single()
    assertThat(widget).all {
      prop("id") { it.id }.isEqualTo(WIDGET)
      prop("type") { it.type }.isEqualTo(Custom)
      prop("meta") { it.meta }.isEqualTo(JsonObject(mapOf("id" to JsonPrimitive(REPORT.value))))
    }
  }

  @Test
  fun `Can't save under a name another report uses`() = runNewTest {
    database.customReportsQueries.withoutResult {
      insert(emptyCustomReport(CustomReportId("other")).copy(name = "Spending", tombstone = false))
    }

    assertThat(viewModel.takenNames.first { it.isNotEmpty() }).isEqualTo(setOf("Spending"))

    viewModel.save("Spending")
    scope.advanceUntilIdle()

    assertThat(customReports[REPORT]).isNull()
    assertThat(dashboard.observeByPage(PAGE).first()).isEmpty()
  }

  private data class Harness(
    val database: BudgetDatabase,
    val viewModel: NewCustomReportViewModel,
    val customReports: CustomReportsDao,
    val dashboard: DashboardDao,
    val scope: TestScope,
  )

  private fun runNewTest(action: suspend Harness.() -> Unit) =
    runSyncedDatabaseTest { scope, controller ->
      dashboardPagesQueries.withoutResult {
        insert(Dashboard_pages(PAGE, "Main", tombstone = false))
      }
      val dispatcher = StandardTestDispatcher(scope.testScheduler)
      // Keep viewModelScope on the test scheduler, so its queries can't outlive the database
      Dispatchers.setMain(dispatcher)
      val contexts = TestCoroutineContexts(dispatcher)
      val calendar = Calendar { LocalDate(2026, 1, 1) }
      val dashboard = DashboardDao(this, contexts)
      val customReports = CustomReportsDao(this, contexts)
      val ids = ArrayDeque(listOf(REPORT.value, WIDGET.value))
      val viewModel =
        NewCustomReportViewModel(
          page = PAGE,
          uuidGenerator = { ids.removeFirst() },
          reports = CustomReportSync(customReports, controller, calendar),
          dashboard = DashboardSync(dashboard, controller),
          customReportsDao = customReports,
          budgetDao = BudgetDao(this, contexts),
          tagsDao = TagsDao(this, contexts),
          chartDataLoader = chartDataLoader(contexts, calendar),
          queryEditorDataLoader = QueryEditorDataLoader(this, ReportsDao(this, contexts), calendar),
        )
      try {
        Harness(this, viewModel, customReports, dashboard, scope).action()
        scope.advanceUntilIdle()
      } finally {
        Dispatchers.resetMain()
      }
    }

  private companion object {
    val PAGE = DashboardPageId("page")
    val WIDGET = WidgetId("widget")
    val REPORT = CustomReportId("report")
  }
}
