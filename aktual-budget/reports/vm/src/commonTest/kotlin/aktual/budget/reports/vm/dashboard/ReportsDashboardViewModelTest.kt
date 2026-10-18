package aktual.budget.reports.vm.dashboard

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.Dashboard_pages
import aktual.budget.db.DbJson
import aktual.budget.db.dao.CustomReportsDao
import aktual.budget.db.dao.DashboardDao
import aktual.budget.db.withResult
import aktual.budget.db.withoutResult
import aktual.budget.model.CustomReportId
import aktual.budget.model.DashboardPageId
import aktual.budget.model.WidgetId
import aktual.budget.reports.vm.CustomReportMeta
import aktual.budget.reports.vm.CustomReportSync
import aktual.budget.reports.vm.DashboardSync
import aktual.budget.reports.vm.chartDataLoader
import aktual.budget.reports.vm.emptyCustomReport
import aktual.budget.reports.vm.pinned.PinnedReportsImpl
import aktual.budget.reports.vm.runSyncedDatabaseTest
import aktual.core.Calendar
import aktual.test.TestBudgetLocalPreferences
import aktual.test.assertThatNextEmission
import aktual.test.assertThatNextEmissionIsEqualTo
import alakazam.test.TestCoroutineContexts
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.turbine.test
import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.prop
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

class ReportsDashboardViewModelTest {
  @Test
  fun `Loading, then empty page`() =
    runDashboardTest(PAGE) { viewModel, _, _ ->
      viewModel.content.test {
        assertThatNextEmissionIsEqualTo(DashboardContent(page = null, items = null))
        assertThatNextEmissionIsEqualTo(DashboardContent(PAGE, persistentListOf()))
      }
    }

  @Test
  fun `Loading, then page items`() =
    runDashboardTest(PAGE) { viewModel, sync, _ ->
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
  fun `Loading, then empty with no pages`() = runDashboardTest { viewModel, _, _ ->
    viewModel.content.test {
      assertThatNextEmissionIsEqualTo(DashboardContent(page = null, items = null))
      assertThatNextEmissionIsEqualTo(DashboardContent(page = null, items = persistentListOf()))
    }
  }

  @Test
  fun `Deleting a saved report also removes its widget`() =
    runDashboardTest(PAGE) { viewModel, sync, _ ->
      customReportsQueries.withoutResult { insert(emptyCustomReport(REPORT)) }
      val meta = DbJson.encodeToJsonElement(CustomReportMeta.serializer(), CustomReportMeta(REPORT))
      sync.insertWidget(CUSTOM, PAGE.id, Custom, x = 0, y = 0, meta = meta.jsonObject)

      viewModel.content.test {
        assertThatNextEmissionIsEqualTo(DashboardContent(page = null, items = null))
        val item = awaitItem().items.orEmpty().single()

        viewModel.deleteSavedReport(item)
        assertThatNextEmissionIsEqualTo(DashboardContent(PAGE, persistentListOf()))
      }
      val report = customReportsQueries.withResult { getById(REPORT).awaitAsOneOrNull() }
      assertThat(report?.tombstone).isEqualTo(true)
    }

  @Test
  fun `Renaming a custom report to another's name is refused`() =
    runDashboardTest(PAGE) { viewModel, sync, scope ->
      customReportsQueries.withoutResult {
        insert(emptyCustomReport(REPORT).copy(name = "Spending", tombstone = false))
        insert(emptyCustomReport(OTHER_REPORT).copy(name = "Bills", tombstone = false))
      }
      val meta = DbJson.encodeToJsonElement(CustomReportMeta.serializer(), CustomReportMeta(REPORT))
      sync.insertWidget(CUSTOM, PAGE.id, Custom, x = 0, y = 0, meta = meta.jsonObject)
      val item = viewModel.content.first { !it.items.isNullOrEmpty() }.items.orEmpty().single()

      viewModel.renameReport(item, "Bills")
      scope.advanceUntilIdle()
      assertThat(name(REPORT)).isEqualTo("Spending")

      viewModel.renameReport(item, "Groceries")
      scope.advanceUntilIdle()
      assertThat(name(REPORT)).isEqualTo("Groceries")
    }

  private suspend fun BudgetDatabase.name(id: CustomReportId): String? =
    customReportsQueries.withResult { getById(id).awaitAsOneOrNull() }?.name

  private fun runDashboardTest(
    vararg pages: DashboardPage,
    action: suspend BudgetDatabase.(ReportsDashboardViewModel, DashboardSync, TestScope) -> Unit,
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
    val calendar = Calendar { LocalDate(2026, 1, 1) }
    val loader = chartDataLoader(contexts, calendar)
    val customReports = CustomReportsDao(this, contexts)
    val prefs = TestBudgetLocalPreferences()
    val dashboardPages =
      DashboardPages(dao = dao, sync = sync, prefs = prefs, uuidGenerator = { "unused" })
    val viewModel =
      ReportsDashboardViewModel(
        chartDataLoader = loader,
        dashboardDao = dao,
        sync = sync,
        pages = dashboardPages,
        decoder = DashboardItemDecoder(),
        pinned =
          PinnedReportsImpl(
            prefs = prefs,
            dashboardDao = dao,
            customReportsDao = customReports,
            pages = dashboardPages,
            decoder = DashboardItemDecoder(),
            chartDataLoader = loader,
          ),
        customReports = CustomReportSync(customReports, controller, calendar),
        customReportsDao = customReports,
      )
    try {
      action(viewModel, sync, scope)
      // Let the VM's eager queries finish before the database closes
      scope.advanceUntilIdle()
    } finally {
      Dispatchers.resetMain()
    }
  }

  private companion object {
    val PAGE = DashboardPage(DashboardPageId("page"), "Main")
    val NET_WORTH = WidgetId("net-worth")
    val CUSTOM = WidgetId("custom")
    val REPORT = CustomReportId("report")
    val OTHER_REPORT = CustomReportId("other-report")
  }
}
