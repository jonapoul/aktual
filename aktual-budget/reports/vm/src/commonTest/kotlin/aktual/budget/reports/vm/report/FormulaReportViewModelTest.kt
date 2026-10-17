package aktual.budget.reports.vm.report

import aktual.budget.db.Dashboard_pages
import aktual.budget.db.dao.DashboardDao
import aktual.budget.db.dao.ReportsDao
import aktual.budget.db.withoutResult
import aktual.budget.model.Amount
import aktual.budget.model.DashboardPageId
import aktual.budget.model.WidgetId
import aktual.budget.reports.vm.DashboardSync
import aktual.budget.reports.vm.FormulaData
import aktual.budget.reports.vm.FormulaOutput
import aktual.budget.reports.vm.QueryEditorDataLoader
import aktual.budget.reports.vm.chartDataLoader
import aktual.budget.reports.vm.dashboard.DashboardItemDecoder
import aktual.budget.reports.vm.report.FormulaReportState.Loaded
import aktual.budget.reports.vm.runSyncedDatabaseTest
import aktual.core.Calendar
import aktual.di.BudgetCoroutineScope
import alakazam.test.TestCoroutineContexts
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject

class FormulaReportViewModelTest {
  @Test
  fun `Starts viewing the saved card`() = runFormulaTest {
    viewModel.state.test {
      val loaded = awaitLoaded { it.preview != null }
      assertThat(loaded).all {
        prop(Loaded::isEditing).isFalse()
        prop(Loaded::hasChanges).isFalse()
        transform { it.draft.name }.isEqualTo("Card")
        transform { it.draft.formula }.isEqualTo("=1")
        transform { it.draft.showTitle }.isTrue()
        transform { it.preview?.output }.isEqualTo(FormulaOutput.Money(Amount(100)))
      }
    }
  }

  @Test
  fun `Edits change the preview but not the card`() = runFormulaTest {
    viewModel.state.test {
      awaitLoaded { it.preview != null }
      viewModel.startEditing()
      viewModel.edit { it.copy(formula = "=2", showTitle = false) }

      val edited = awaitLoaded { it.preview?.output == FormulaOutput.Money(Amount(200)) }
      assertThat(edited).all {
        prop(Loaded::isEditing).isTrue()
        prop(Loaded::hasChanges).isTrue()
        prop(Loaded::preview).isNotNull().prop(FormulaData::showTitle).isFalse()
      }
      assertThat(viewModel.hasUnsavedChanges()).isTrue()
      cancelAndIgnoreRemainingEvents()
    }

    assertThat(dao.meta(ID)).isNotNull().transform { it["formula"] }.isEqualTo(JsonPrimitive("=1"))
  }

  @Test
  fun `Saving writes the draft to the card and stops editing`() = runFormulaTest {
    viewModel.state.test {
      awaitLoaded { it.preview != null }
      viewModel.startEditing()
      viewModel.edit { it.copy(name = "Renamed", formula = "=2") }
      awaitLoaded { it.hasChanges }

      viewModel.save()
      scope.advanceUntilIdle()
      awaitLoaded { !it.isEditing && it.draft.formula == "=2" }
      cancelAndIgnoreRemainingEvents()
    }

    assertThat(viewModel.hasUnsavedChanges()).isFalse()
    assertThat(dao.meta(ID)).isNotNull().all {
      transform { it["name"] }.isEqualTo(JsonPrimitive("Renamed"))
      transform { it["formula"] }.isEqualTo(JsonPrimitive("=2"))
    }
  }

  @Test
  fun `Discarding puts the draft back`() = runFormulaTest {
    viewModel.state.test {
      awaitLoaded { it.preview != null }
      viewModel.startEditing()
      viewModel.edit { it.copy(formula = "=2") }
      awaitLoaded { it.hasChanges }

      viewModel.discard()
      val discarded = awaitLoaded { !it.isEditing }
      assertThat(discarded.draft.formula).isEqualTo("=1")
      cancelAndIgnoreRemainingEvents()
    }

    assertThat(viewModel.hasUnsavedChanges()).isFalse()
    assertThat(dao.meta(ID)).isNotNull().transform { it["formula"] }.isEqualTo(JsonPrimitive("=1"))
  }

  @Test
  fun `Edits are ignored while viewing`() = runFormulaTest {
    viewModel.state.test {
      awaitLoaded { it.preview != null }
      viewModel.edit { it.copy(formula = "=2") }
      scope.advanceUntilIdle()
      expectNoEvents()
    }

    assertThat(viewModel.hasUnsavedChanges()).isFalse()
  }

  @Test
  fun `Viewing follows changes to the card`() = runFormulaTest {
    viewModel.state.test {
      val loaded = awaitLoaded { it.preview != null }
      sync.setFormula(ID, loaded.draft.copy(formula = "=3"))

      val updated = awaitLoaded { it.preview?.output == FormulaOutput.Money(Amount(300)) }
      assertThat(updated.draft.formula).isEqualTo("=3")
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Reports a missing widget`() =
    runFormulaTest(meta = null) {
      viewModel.state.test {
        var state = awaitItem()
        while (state == Loading) state = awaitItem()
        assertThat(state).isEqualTo(NotFound)
      }
    }

  private suspend fun ReceiveTurbine<FormulaReportState>.awaitLoaded(
    predicate: (Loaded) -> Boolean,
  ): Loaded {
    while (true) {
      val state = awaitItem()
      if (state is Loaded && predicate(state)) return state
    }
  }

  private data class Harness(
    val viewModel: FormulaReportViewModel,
    val dao: DashboardDao,
    val sync: DashboardSync,
    val scope: TestScope,
  )

  private fun runFormulaTest(meta: JsonObject? = META, action: suspend Harness.() -> Unit) =
    runSyncedDatabaseTest { scope, controller ->
      dashboardPagesQueries.withoutResult {
        insert(Dashboard_pages(PAGE, "Main", tombstone = false))
      }
      val dispatcher = StandardTestDispatcher(scope.testScheduler)
      // Keep viewModelScope on the test scheduler, so its queries can't outlive the database
      Dispatchers.setMain(dispatcher)
      val contexts = TestCoroutineContexts(dispatcher)
      val calendar = Calendar { LocalDate(2026, 1, 1) }
      val dao = DashboardDao(this, contexts)
      val sync = DashboardSync(dao, controller)
      if (meta != null) sync.insertWidget(ID, PAGE, Formula, x = 0, y = 0, meta = meta)
      val viewModel =
        FormulaReportViewModel(
          id = ID,
          dashboardDao = dao,
          decoder = DashboardItemDecoder(),
          chartDataLoader = chartDataLoader(contexts, calendar),
          queryEditorDataLoader = QueryEditorDataLoader(this, ReportsDao(this, contexts), calendar),
          sync = sync,
          budgetScope = BudgetCoroutineScope(scope),
        )
      try {
        Harness(viewModel, dao, sync, scope).action()
        scope.advanceUntilIdle()
      } finally {
        Dispatchers.resetMain()
      }
    }

  private companion object {
    val PAGE = DashboardPageId("page")
    val ID = WidgetId("formula")
    val META =
      Json.parseToJsonElement("""{"name": "Card", "formula": "=1", "queries": {}}""").jsonObject
  }
}
