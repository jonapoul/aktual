package aktual.budget.reports.vm.report

import aktual.budget.db.Dashboard_pages
import aktual.budget.db.dao.DashboardDao
import aktual.budget.db.dao.ReportsDao
import aktual.budget.db.withoutResult
import aktual.budget.model.DashboardPageId
import aktual.budget.model.WidgetId
import aktual.budget.reports.vm.DashboardSync
import aktual.budget.reports.vm.NetWorthData
import aktual.budget.reports.vm.NetWorthMode
import aktual.budget.reports.vm.NetWorthReportMeta
import aktual.budget.reports.vm.QueryEditorDataLoader
import aktual.budget.reports.vm.ReportMeta
import aktual.budget.reports.vm.chartDataLoader
import aktual.budget.reports.vm.dashboard.DashboardItemDecoder
import aktual.budget.reports.vm.report.ReportState.Loaded
import aktual.budget.reports.vm.runSyncedDatabaseTest
import aktual.core.Calendar
import aktual.di.BudgetCoroutineScope
import aktual.test.viewModelContext
import alakazam.test.TestCoroutineContexts
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import assertk.assertions.prop
import kotlin.test.Test
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject

class ReportViewModelTest {
  @Test
  fun `Starts showing the saved report`() = runReportTest {
    viewModel.state.test {
      val loaded = awaitLoaded { true }
      assertThat(loaded).all {
        prop(Loaded::hasChanges).isFalse()
        prop(Loaded::data).isInstanceOf<NetWorthData>().prop(NetWorthData::mode).isEqualTo(Trend)
      }
    }
  }

  @Test
  fun `Edits change the chart but not the widget`() = runReportTest {
    viewModel.state.test {
      awaitLoaded { true }
      viewModel.edit(setMode(Stacked))

      val edited = awaitLoaded { it.mode() == Stacked }
      assertThat(edited.hasChanges).isTrue()
      assertThat(viewModel.hasUnsavedChanges()).isTrue()
      cancelAndIgnoreRemainingEvents()
    }

    assertThat(dao.meta(ID)).isNotNull().transform { it["mode"] }.isEqualTo(JsonPrimitive("trend"))
  }

  @Test
  fun `Saving writes the draft to the widget`() = runReportTest {
    viewModel.state.test {
      awaitLoaded { true }
      viewModel.edit(setMode(Stacked))
      awaitLoaded { it.hasChanges }

      viewModel.save()

      val saved = awaitLoaded { !it.hasChanges }
      assertThat(saved.mode()).isEqualTo(Stacked)
      cancelAndIgnoreRemainingEvents()
    }

    assertThat(viewModel.hasUnsavedChanges()).isFalse()
    assertThat(dao.meta(ID))
      .isNotNull()
      .transform { it["mode"] }
      .isEqualTo(JsonPrimitive("stacked"))
  }

  @Test
  fun `Discarding puts the saved report back`() = runReportTest {
    viewModel.state.test {
      awaitLoaded { true }
      viewModel.edit(setMode(Stacked))
      awaitLoaded { it.hasChanges }

      viewModel.discard()

      val discarded = awaitLoaded { !it.hasChanges }
      assertThat(discarded.mode()).isEqualTo(Trend)
      cancelAndIgnoreRemainingEvents()
    }

    assertThat(viewModel.hasUnsavedChanges()).isFalse()
  }

  @Test
  fun `Renaming keeps the edits and saving keeps the new name`() = runReportTest {
    viewModel.state.test {
      awaitLoaded { true }
      viewModel.edit(setMode(Stacked))
      awaitLoaded { it.hasChanges }

      viewModel.rename("Wealth")

      val renamed = awaitLoaded { (it.draft as NetWorthReportMeta).name == "Wealth" }
      assertThat(renamed).all {
        prop(Loaded::hasChanges).isTrue()
        transform { it.mode() }.isEqualTo(Stacked)
      }

      viewModel.save()
      awaitLoaded { !it.hasChanges }
      cancelAndIgnoreRemainingEvents()
    }

    assertThat(dao.meta(ID)).isNotNull().all {
      transform { it["name"] }.isEqualTo(JsonPrimitive("Wealth"))
      transform { it["mode"] }.isEqualTo(JsonPrimitive("stacked"))
    }
  }

  private fun setMode(mode: NetWorthMode): (ReportMeta) -> ReportMeta = { meta ->
    (meta as NetWorthReportMeta).copy(mode = mode)
  }

  private fun Loaded.mode(): NetWorthMode = (data as NetWorthData).mode

  private suspend fun ReceiveTurbine<ReportState>.awaitLoaded(
    predicate: (Loaded) -> Boolean,
  ): Loaded {
    while (true) {
      val state = awaitItem()
      if (state is Loaded && predicate(state)) return state
    }
  }

  private data class Harness(
    val viewModel: ReportViewModel,
    val dao: DashboardDao,
    val scope: TestScope,
  )

  private fun runReportTest(action: suspend Harness.() -> Unit) =
    runSyncedDatabaseTest { scope, controller ->
      dashboardPagesQueries.withoutResult {
        insert(Dashboard_pages(PAGE, "Main", tombstone = false))
      }
      val dispatcher = StandardTestDispatcher(scope.testScheduler)
      // Keep viewModelScope on the test scheduler, so its queries can't outlive the database
      val contexts = TestCoroutineContexts(dispatcher)
      val calendar = Calendar { LocalDate(2026, 1, 15) }
      val dao = DashboardDao(this, contexts)
      val sync = DashboardSync(dao, controller)
      sync.insertWidget(ID, PAGE, NetWorth, x = 0, y = 0, meta = META)
      val viewModel =
        ReportViewModel(
          id = ID,
          dashboardDao = dao,
          queryEditorDataLoader = QueryEditorDataLoader(this, ReportsDao(this, contexts), calendar),
          sync = sync,
          chartDataLoader = chartDataLoader(contexts, calendar),
          decoder = DashboardItemDecoder(),
          budgetScope = BudgetCoroutineScope(scope),
          viewModelContext = scope.viewModelContext,
        )
      Harness(viewModel, dao, scope).action()
      scope.advanceUntilIdle()
    }

  private companion object {
    val PAGE = DashboardPageId("page")
    val ID = WidgetId("net-worth")
    val META = Json.parseToJsonElement("""{"name": "Net worth", "mode": "trend"}""").jsonObject
  }
}
