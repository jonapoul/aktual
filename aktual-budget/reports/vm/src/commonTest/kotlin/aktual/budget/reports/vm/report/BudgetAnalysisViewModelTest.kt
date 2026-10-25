package aktual.budget.reports.vm.report

import aktual.budget.db.Dashboard_pages
import aktual.budget.db.dao.DashboardDao
import aktual.budget.db.dao.ReportsDao
import aktual.budget.db.withoutResult
import aktual.budget.model.DashboardPageId
import aktual.budget.model.WidgetId
import aktual.budget.reports.vm.BudgetAnalysisData
import aktual.budget.reports.vm.BudgetAnalysisSettings
import aktual.budget.reports.vm.DashboardSync
import aktual.budget.reports.vm.QueryEditorDataLoader
import aktual.budget.reports.vm.TimeFrame
import aktual.budget.reports.vm.chartDataLoader
import aktual.budget.reports.vm.dashboard.DashboardItemDecoder
import aktual.budget.reports.vm.report.BudgetAnalysisState.Loaded
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
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import assertk.assertions.prop
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject

class BudgetAnalysisViewModelTest {
  @Test
  fun `Starts viewing the saved report, with upstream's defaults`() = runAnalysisTest {
    viewModel.state.test {
      val loaded = awaitLoaded { it.preview != null }
      assertThat(loaded).all {
        prop(Loaded::name).isEqualTo("Analysis")
        prop(Loaded::draft).isEqualTo(DEFAULTS)
        prop(Loaded::isEditing).isFalse()
        prop(Loaded::hasChanges).isFalse()
      }
    }
  }

  @Test
  fun `Edits change the preview but not the widget`() = runAnalysisTest {
    viewModel.state.test {
      awaitLoaded { it.preview != null }
      viewModel.startEditing()
      viewModel.edit { it.copy(graphType = Line, balanceOnly = true) }

      val edited = awaitLoaded { it.preview?.graphType == Line }
      assertThat(edited).all {
        prop(Loaded::isEditing).isTrue()
        prop(Loaded::hasChanges).isTrue()
        prop(Loaded::preview).isNotNull().prop(BudgetAnalysisData::balanceOnly).isTrue()
      }
      assertThat(viewModel.hasUnsavedChanges()).isTrue()
      cancelAndIgnoreRemainingEvents()
    }

    assertThat(dao.meta(ID)).isNotNull().transform { it["graphType"] }.isNull()
  }

  @Test
  fun `Closing the editor keeps the changes`() = runAnalysisTest {
    viewModel.state.test {
      awaitLoaded { it.preview != null }
      viewModel.startEditing()
      viewModel.edit { it.copy(showBalance = false) }
      awaitLoaded { it.hasChanges }

      viewModel.stopEditing()
      val closed = awaitLoaded { !it.isEditing }
      assertThat(closed).all {
        prop(Loaded::hasChanges).isTrue()
        prop(Loaded::draft).prop(BudgetAnalysisSettings::showBalance).isFalse()
      }
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Saving writes the draft to the widget and stops editing`() = runAnalysisTest {
    viewModel.state.test {
      awaitLoaded { it.preview != null }
      viewModel.startEditing()
      viewModel.edit { it.copy(graphType = Line, showHiddenCategories = true) }
      awaitLoaded { it.hasChanges }

      viewModel.save()
      scope.advanceUntilIdle()
      awaitLoaded { !it.isEditing && !it.hasChanges }
      cancelAndIgnoreRemainingEvents()
    }

    assertThat(viewModel.hasUnsavedChanges()).isFalse()
    assertThat(dao.meta(ID)).isNotNull().all {
      transform { it["name"] }.isEqualTo(JsonPrimitive("Analysis"))
      transform { it["graphType"] }.isEqualTo(JsonPrimitive("Line"))
      transform { it["showHiddenCategories"] }.isEqualTo(JsonPrimitive(true))
    }
  }

  @Test
  fun `Reverting puts the draft back`() = runAnalysisTest {
    viewModel.state.test {
      awaitLoaded { it.preview != null }
      viewModel.startEditing()
      viewModel.edit { it.copy(graphType = Line) }
      awaitLoaded { it.hasChanges }

      viewModel.revert()
      val reverted = awaitLoaded { !it.hasChanges }
      assertThat(reverted.draft).isEqualTo(DEFAULTS)
      cancelAndIgnoreRemainingEvents()
    }

    assertThat(viewModel.hasUnsavedChanges()).isFalse()
  }

  @Test
  fun `Edits are ignored while viewing`() = runAnalysisTest {
    viewModel.state.test {
      awaitLoaded { it.preview != null }
      viewModel.edit { it.copy(graphType = Line) }
      scope.advanceUntilIdle()
      expectNoEvents()
    }

    assertThat(viewModel.hasUnsavedChanges()).isFalse()
  }

  @Test
  fun `Reports a missing widget`() =
    runAnalysisTest(meta = null) {
      viewModel.state.test {
        var state = awaitItem()
        while (state == Loading) state = awaitItem()
        assertThat(state).isEqualTo(NotFound)
      }
    }

  private suspend fun ReceiveTurbine<BudgetAnalysisState>.awaitLoaded(
    predicate: (Loaded) -> Boolean,
  ): Loaded {
    while (true) {
      val state = awaitItem()
      if (state is Loaded && predicate(state)) return state
    }
  }

  private data class Harness(
    val viewModel: BudgetAnalysisViewModel,
    val dao: DashboardDao,
    val scope: TestScope,
  )

  private fun runAnalysisTest(meta: JsonObject? = META, action: suspend Harness.() -> Unit) =
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
      if (meta != null) sync.insertWidget(ID, PAGE, BudgetAnalysis, x = 0, y = 0, meta = meta)
      val viewModel =
        BudgetAnalysisViewModel(
          id = ID,
          dashboardDao = dao,
          decoder = DashboardItemDecoder(),
          chartDataLoader = chartDataLoader(contexts, calendar),
          queryEditorDataLoader = QueryEditorDataLoader(this, ReportsDao(this, contexts), calendar),
          sync = sync,
          calendar = calendar,
          budgetScope = BudgetCoroutineScope(scope),
          viewModelContext = scope.viewModelContext,
        )
      Harness(viewModel, dao, scope).action()
      scope.advanceUntilIdle()
    }

  private companion object {
    val PAGE = DashboardPageId("page")
    val ID = WidgetId("analysis")
    val META = Json.parseToJsonElement("""{"name": "Analysis"}""").jsonObject

    val DEFAULTS =
      BudgetAnalysisSettings(
        timeFrame = TimeFrame(YearMonth(2025, 8), YearMonth(2026, 1), SlidingWindow),
        conditions = persistentListOf(),
        conditionsOp = And,
        graphType = Bar,
        showBalance = true,
        balanceOnly = false,
        showHiddenCategories = false,
      )
  }
}
