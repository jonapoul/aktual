package aktual.budget.reports.vm.montecarlo

import aktual.budget.db.Dashboard_pages
import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.DashboardDao
import aktual.budget.db.dao.ReportsDao
import aktual.budget.db.withoutResult
import aktual.budget.model.DashboardPageId
import aktual.budget.model.WidgetId
import aktual.budget.reports.vm.DashboardSync
import aktual.budget.reports.vm.McIncomeStream
import aktual.budget.reports.vm.McPot
import aktual.budget.reports.vm.dashboard.DashboardItemDecoder
import aktual.budget.reports.vm.keepsSurplus
import aktual.budget.reports.vm.runSyncedDatabaseTest
import alakazam.test.TestCoroutineContexts
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.containsOnly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject

// Desktop only: Molecule needs Robolectric on the Android host target
class MonteCarloViewModelTest {
  @Test
  fun `Simulates the saved plan`() = runMonteCarloTest { viewModel, _, _ ->
    viewModel.state.test {
      val loaded = awaitLoaded { it.results != null }
      assertThat(loaded).all {
        prop(MonteCarloState.Loaded::title).isEqualTo("Plan")
        prop(MonteCarloState.Loaded::hasChanges).isFalse()
        prop(MonteCarloState.Loaded::results).isNotNull().all {
          prop(MonteCarloResults::currentAge).isEqualTo(60)
          prop(MonteCarloResults::endAge).isEqualTo(70)
          prop(MonteCarloResults::simulationCount).isEqualTo(1000)
        }
      }
    }
  }

  @Test
  fun `Reports a missing widget`() =
    runMonteCarloTest(insert = false) { viewModel, _, _ ->
      viewModel.state.test {
        var state = awaitItem()
        while (state == Loading) state = awaitItem()
        assertThat(state).isEqualTo(NotFound)
      }
    }

  @Test
  fun `Saving writes the plan over the meta`() = runMonteCarloTest { viewModel, dao, scope ->
    viewModel.state.test {
      val loaded = awaitLoaded { it.results != null }
      viewModel.setConfig(loaded.config.copy(currentAge = 55, inflationMean = null))
      awaitLoaded { it.hasChanges }

      viewModel.save()
      scope.advanceUntilIdle()
      awaitLoaded { !it.hasChanges && it.config.currentAge == 55 }
      cancelAndIgnoreRemainingEvents()
    }

    val meta = dao.meta(ID)
    assertThat(meta).isNotNull().all {
      // Fields the config doesn't own are kept
      transform { it["name"] }.isEqualTo(JsonPrimitive("Plan"))
      transform { it["currentAge"] }.isEqualTo(JsonPrimitive(55))
      // An explicit null, since a missing key means upstream's default inflation
      transform { it["inflationMean"] }.isEqualTo(JsonNull)
    }
  }

  @Test
  fun `Added items take generated ids`() = runMonteCarloTest { viewModel, _, _ ->
    viewModel.state.test {
      awaitLoaded { it.results != null }
      viewModel.addPot()
      viewModel.addIncomeStream()
      viewModel.setKeepSurplus(false)

      val edited = awaitLoaded { it.config.incomeStreams.isNotEmpty() && !it.config.keepsSurplus }
      assertThat(edited).all {
        prop(MonteCarloState.Loaded::hasChanges).isTrue()
        transform { it.config.pots.map(McPot::id) }.containsExactly("pot-1", "new-id")
        transform { it.config.incomeStreams.map(McIncomeStream::id) }.containsExactly("new-id")
      }
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Renaming writes the name without touching the plan`() =
    runMonteCarloTest { viewModel, dao, scope ->
      viewModel.state.test {
        awaitLoaded { it.results != null }
        viewModel.rename("Retirement")
        scope.advanceUntilIdle()

        val renamed = awaitLoaded { it.title == "Retirement" }
        assertThat(renamed.hasChanges).isFalse()
        cancelAndIgnoreRemainingEvents()
      }

      assertThat(dao.meta(ID)).isNotNull().all {
        transform { it["name"] }.isEqualTo(JsonPrimitive("Retirement"))
        transform { it["currentAge"] }.isEqualTo(JsonPrimitive(60))
      }
    }

  @Test
  fun `Sections collapse and expand`() = runMonteCarloTest { viewModel, _, _ ->
    viewModel.state.test {
      val loaded = awaitLoaded { it.results != null }
      assertThat(loaded.collapsedSections).isEmpty()

      viewModel.toggleSection(Configuration)
      viewModel.toggleSection(HowItWorks)
      val collapsed = awaitLoaded { it.collapsedSections.size == 2 }
      assertThat(collapsed.collapsedSections)
        .containsOnly(MonteCarloSection.Configuration, MonteCarloSection.HowItWorks)

      viewModel.toggleSection(Configuration)
      val expanded = awaitLoaded { it.collapsedSections.size == 1 }
      assertThat(expanded.collapsedSections).containsOnly(MonteCarloSection.HowItWorks)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `A selected run expires when the plan changes`() = runMonteCarloTest { viewModel, _, _ ->
    viewModel.state.test {
      val loaded = awaitLoaded { it.results != null }
      viewModel.setResultsView(Runs)
      viewModel.selectRun(3)
      val selected = awaitLoaded { it.runDetail != null }
      assertThat(selected).all {
        prop(MonteCarloState.Loaded::selectedRun).isEqualTo(3)
        prop(MonteCarloState.Loaded::runDetail).isNotNull().all {
          prop(MonteCarloRunDetail::index).isEqualTo(3)
          transform { it.rows.isNotEmpty() }.isTrue()
        }
      }

      viewModel.setConfig(loaded.config.copy(targetAge = 75))
      val changed = awaitLoaded { it.config.targetAge == 75 }
      assertThat(changed.selectedRun).isNull()
      cancelAndIgnoreRemainingEvents()
    }
  }

  private suspend fun ReceiveTurbine<MonteCarloState>.awaitLoaded(
    predicate: (MonteCarloState.Loaded) -> Boolean
  ): MonteCarloState.Loaded {
    while (true) {
      val state = awaitItem()
      if (state is Loaded && predicate(state)) return state
    }
  }

  private fun runMonteCarloTest(
    insert: Boolean = true,
    action:
      suspend (
        MonteCarloViewModel,
        DashboardDao,
        TestScope,
      ) -> Unit,
  ) = runSyncedDatabaseTest { scope, controller ->
    dashboardPagesQueries.withoutResult {
      insert(Dashboard_pages(PAGE, "Main", tombstone = false))
    }
    val dispatcher = StandardTestDispatcher(scope.testScheduler)
    // Keep viewModelScope on the test scheduler, so its queries can't outlive the database
    Dispatchers.setMain(dispatcher)
    val contexts = TestCoroutineContexts(dispatcher)
    val dao = DashboardDao(this, contexts)
    val sync = DashboardSync(dao, controller)
    if (insert) sync.insertWidget(ID, PAGE, MonteCarlo, x = 0, y = 0, meta = META)
    val viewModel =
      MonteCarloViewModel(
        id = ID,
        dashboardDao = dao,
        accountDao = AccountDao(this),
        reportsDao = ReportsDao(this, contexts),
        decoder = DashboardItemDecoder(),
        sync = sync,
        uuidGenerator = { "new-id" },
        contexts = contexts,
      )
    try {
      action(viewModel, dao, scope)
      scope.advanceUntilIdle()
    } finally {
      Dispatchers.resetMain()
    }
  }

  private companion object {
    val PAGE = DashboardPageId("page")
    val ID = WidgetId("monte-carlo")
    val META: JsonObject =
      Json.parseToJsonElement(
          """{"name": "Plan", "currentAge": 60, "targetAge": 70, "simulationCount": 1000}"""
        )
        .jsonObject
  }
}
