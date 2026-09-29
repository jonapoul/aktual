package aktual.budget.reports.vm.dashboard

import aktual.budget.db.Dashboard_pages
import aktual.budget.db.dao.DashboardDao
import aktual.budget.db.dao.PreferencesDao
import aktual.budget.db.dao.ReportsDao
import aktual.budget.db.withoutResult
import aktual.budget.model.DashboardPageId
import aktual.budget.model.WidgetId
import aktual.budget.reports.vm.ChartDataLoader
import aktual.budget.reports.vm.DashboardSync
import aktual.budget.reports.vm.runSyncedDatabaseTest
import aktual.test.TestBudgetLocalPreferences
import aktual.test.assertThatNextEmission
import aktual.test.assertThatNextEmissionIsEqualTo
import alakazam.test.TestCoroutineContexts
import app.cash.turbine.test
import assertk.all
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.prop
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonObject

class ReportsDashboardViewModelTest {
  @Test
  fun `Loading, then empty page`() =
    runDashboardTest(PAGE) { viewModel, _ ->
      viewModel.content.test {
        assertThatNextEmissionIsEqualTo(DashboardContent(page = null, items = null))
        assertThatNextEmissionIsEqualTo(DashboardContent(PAGE, persistentListOf()))
      }
    }

  @Test
  fun `Loading, then page items`() =
    runDashboardTest(PAGE) { viewModel, sync ->
      sync.insertWidget(NET_WORTH, PAGE.id, NetWorth, x = 0, y = 0, meta = JsonObject(emptyMap()))

      viewModel.content.test {
        assertThatNextEmissionIsEqualTo(DashboardContent(page = null, items = null))
        assertThatNextEmission().all {
          prop(DashboardContent::page).isEqualTo(PAGE)
          prop(DashboardContent::items)
            .isNotNull()
            .transform { items -> items.map { it.id } }
            .containsExactly(NET_WORTH)
        }
      }
    }

  @Test
  fun `Loading, then empty with no pages`() = runDashboardTest { viewModel, _ ->
    viewModel.content.test {
      assertThatNextEmissionIsEqualTo(DashboardContent(page = null, items = null))
      assertThatNextEmissionIsEqualTo(DashboardContent(page = null, items = persistentListOf()))
    }
  }

  private fun runDashboardTest(
    vararg pages: DashboardPage,
    action: suspend (ReportsDashboardViewModel, DashboardSync) -> Unit,
  ) = runSyncedDatabaseTest { scope, controller ->
    dashboardPagesQueries.withoutResult {
      pages.forEach { insert(Dashboard_pages(it.id, it.name, tombstone = false)) }
    }
    val dispatcher = StandardTestDispatcher(scope.testScheduler)
    // Keep viewModelScope on the test scheduler, so its queries can't outlive the database
    Dispatchers.setMain(dispatcher)
    val contexts = TestCoroutineContexts(dispatcher)
    val dao = DashboardDao(this, contexts)
    val sync = DashboardSync(dao, controller)
    val viewModel =
      ReportsDashboardViewModel(
        chartDataLoader =
          ChartDataLoader(
            ReportsDao(this, contexts),
            PreferencesDao(this, contexts),
            calendar = { LocalDate(2026, 1, 1) },
          ),
        dashboardDao = dao,
        sync = sync,
        pages =
          DashboardPages(
            dao = dao,
            sync = sync,
            prefs = TestBudgetLocalPreferences(),
            uuidGenerator = { "unused" },
          ),
        decoder = DashboardItemDecoder(),
      )
    try {
      action(viewModel, sync)
      // Let the VM's eager queries finish before the database closes
      scope.advanceUntilIdle()
    } finally {
      Dispatchers.resetMain()
    }
  }

  private companion object {
    val PAGE = DashboardPage(DashboardPageId("page"), "Main")
    val NET_WORTH = WidgetId("net-worth")
  }
}
