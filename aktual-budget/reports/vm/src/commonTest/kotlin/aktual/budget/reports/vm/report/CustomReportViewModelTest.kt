package aktual.budget.reports.vm.report

import aktual.budget.BudgetSyncController
import aktual.budget.db.CustomReports
import aktual.budget.db.Dashboard_pages
import aktual.budget.db.dao.BudgetDao
import aktual.budget.db.dao.CustomReportsDao
import aktual.budget.db.dao.DashboardDao
import aktual.budget.db.dao.DatabaseTables.CUSTOM_REPORTS
import aktual.budget.db.dao.ReportsDao
import aktual.budget.db.dao.TagsDao
import aktual.budget.db.withoutResult
import aktual.budget.model.CustomReportId
import aktual.budget.model.DashboardPageId
import aktual.budget.model.WidgetId
import aktual.budget.model.localChange
import aktual.budget.reports.vm.CustomData
import aktual.budget.reports.vm.CustomReportConfig
import aktual.budget.reports.vm.DashboardSync
import aktual.budget.reports.vm.QueryEditorDataLoader
import aktual.budget.reports.vm.chartDataLoader
import aktual.budget.reports.vm.dashboard.DashboardItemDecoder
import aktual.budget.reports.vm.emptyCustomReport
import aktual.budget.reports.vm.report.CustomReportState.Loaded
import aktual.budget.reports.vm.runSyncedDatabaseTest
import aktual.budget.reports.vm.withMode
import aktual.core.Calendar
import alakazam.test.TestCoroutineContexts
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import assertk.assertions.prop
import kotlin.test.Test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class CustomReportViewModelTest {
  @Test
  fun `Starts viewing the saved report`() = runCustomTest {
    viewModel.state.test {
      val loaded = awaitLoaded { it.preview != null }
      assertThat(loaded).all {
        prop(Loaded::name).isEqualTo("Spending")
        prop(Loaded::isEditing).isFalse()
        prop(Loaded::hasChanges).isFalse()
        prop(Loaded::draft).isNotNull().prop(CustomReportConfig::mode).isEqualTo(Total)
        prop(Loaded::preview).isNotNull().isInstanceOf<CustomData>()
      }
    }
  }

  @Test
  fun `Edits change the preview but not the report`() = runCustomTest {
    viewModel.state.test {
      awaitLoaded { it.preview != null }
      viewModel.startEditing()
      viewModel.edit { it.withMode(Time) }

      val edited = awaitLoaded { (it.preview as? CustomData)?.config?.mode == Time }
      assertThat(edited).all {
        prop(Loaded::isEditing).isTrue()
        prop(Loaded::hasChanges).isTrue()
      }
      assertThat(viewModel.hasUnsavedChanges()).isTrue()
      cancelAndIgnoreRemainingEvents()
    }

    assertThat(customReports[REPORT]?.mode).isNull()
  }

  @Test
  fun `Edits are ignored while viewing`() = runCustomTest {
    viewModel.state.test {
      awaitLoaded { it.preview != null }
      viewModel.edit { it.withMode(Time) }
      scope.advanceUntilIdle()
      expectNoEvents()
    }

    assertThat(viewModel.hasUnsavedChanges()).isFalse()
  }

  @Test
  fun `Closing the editor keeps the changes`() = runCustomTest {
    viewModel.state.test {
      awaitLoaded { it.preview != null }
      viewModel.startEditing()
      viewModel.edit { it.withMode(Time) }
      viewModel.stopEditing()

      val closed = awaitLoaded { !it.isEditing && it.hasChanges }
      assertThat(closed.draft?.mode).isEqualTo(Time)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Reverting puts the saved config back`() = runCustomTest {
    viewModel.state.test {
      awaitLoaded { it.preview != null }
      viewModel.startEditing()
      viewModel.edit { it.withMode(Time) }
      awaitLoaded { it.hasChanges }

      viewModel.revert()
      val reverted = awaitLoaded { !it.hasChanges }
      assertThat(reverted).all {
        prop(Loaded::isEditing).isTrue()
        transform { it.draft?.mode }.isEqualTo(Total)
      }
      cancelAndIgnoreRemainingEvents()
    }

    assertThat(viewModel.hasUnsavedChanges()).isFalse()
  }

  @Test
  fun `Follows changes to the saved report`() = runCustomTest {
    viewModel.state.test {
      awaitLoaded { it.preview != null }
      sync.syncChanges(localChange(CUSTOM_REPORTS, REPORT.value, "mode", "time"))

      val updated = awaitLoaded { it.draft?.mode == Time }
      assertThat(updated.hasChanges).isFalse()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Unreadable reports can't be edited`() =
    runCustomTest(report = emptyCustomReport(REPORT).copy(graph_type = Unknown)) {
      viewModel.state.test {
        val loaded = awaitLoaded { it.preview != null }
        assertThat(loaded.draft).isNull()

        viewModel.startEditing()
        scope.advanceUntilIdle()
        expectNoEvents()
      }
    }

  @Test
  fun `Reports a missing widget`() =
    runCustomTest(meta = null) {
      viewModel.state.test {
        var state = awaitItem()
        while (state == Loading) state = awaitItem()
        assertThat(state).isEqualTo(NotFound)
      }
    }

  private suspend fun ReceiveTurbine<CustomReportState>.awaitLoaded(
    predicate: (Loaded) -> Boolean,
  ): Loaded {
    while (true) {
      val state = awaitItem()
      if (state is Loaded && predicate(state)) return state
    }
  }

  private data class Harness(
    val viewModel: CustomReportViewModel,
    val customReports: CustomReportsDao,
    val sync: BudgetSyncController,
    val scope: TestScope,
  )

  private fun runCustomTest(
    meta: JsonObject? = META,
    report: CustomReports = emptyCustomReport(REPORT).copy(name = "Spending"),
    action: suspend Harness.() -> Unit,
  ) = runSyncedDatabaseTest { scope, controller ->
    dashboardPagesQueries.withoutResult { insert(Dashboard_pages(PAGE, "Main", tombstone = false)) }
    customReportsQueries.withoutResult { insert(report) }
    val dispatcher = StandardTestDispatcher(scope.testScheduler)
    // Keep viewModelScope on the test scheduler, so its queries can't outlive the database
    Dispatchers.setMain(dispatcher)
    val contexts = TestCoroutineContexts(dispatcher)
    val calendar = Calendar { LocalDate(2026, 1, 1) }
    val dao = DashboardDao(this, contexts)
    val dashboardSync = DashboardSync(dao, controller)
    if (meta != null) dashboardSync.insertWidget(ID, PAGE, Custom, x = 0, y = 0, meta = meta)
    val customReports = CustomReportsDao(this, contexts)
    val viewModel =
      CustomReportViewModel(
        id = ID,
        dashboardDao = dao,
        decoder = DashboardItemDecoder(),
        customReports = customReports,
        budgetDao = BudgetDao(this, contexts),
        tagsDao = TagsDao(this, contexts),
        chartDataLoader = chartDataLoader(contexts, calendar),
        queryEditorDataLoader = QueryEditorDataLoader(this, ReportsDao(this, contexts), calendar),
      )
    try {
      Harness(viewModel, customReports, controller, scope).action()
      scope.advanceUntilIdle()
    } finally {
      Dispatchers.resetMain()
    }
  }

  private companion object {
    val PAGE = DashboardPageId("page")
    val ID = WidgetId("custom")
    val REPORT = CustomReportId("report-1")
    val META = JsonObject(mapOf("id" to JsonPrimitive(REPORT.value)))
  }
}
