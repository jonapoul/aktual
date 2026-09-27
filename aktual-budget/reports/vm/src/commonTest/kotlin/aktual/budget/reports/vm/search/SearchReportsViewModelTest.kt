package aktual.budget.reports.vm.search

import aktual.budget.db.Dashboard_pages
import aktual.budget.db.dao.CustomReportsDao
import aktual.budget.db.dao.DashboardDao
import aktual.budget.db.withoutResult
import aktual.budget.model.CustomReportId
import aktual.budget.model.DashboardPageId
import aktual.budget.model.WidgetId
import aktual.budget.model.WidgetType
import aktual.budget.reports.vm.DashboardSync
import aktual.budget.reports.vm.dashboard.DashboardItemDecoder
import aktual.budget.reports.vm.dashboard.DashboardPage
import aktual.budget.reports.vm.dashboard.DashboardPages
import aktual.budget.reports.vm.runSyncedDatabaseTest
import aktual.test.TestBudgetLocalPreferences
import alakazam.test.TestCoroutineContexts
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

class SearchReportsViewModelTest {
  @Test
  fun `Blank query prompts for input`() = runSearchTest { viewModel, _ ->
    viewModel.state.test {
      assertThat(awaitItem()).isEqualTo(NoQuery)
      viewModel.setQuery("   ")
      expectNoEvents()
    }
  }

  @Test
  fun `Matches names case-insensitively, grouped by page`() = runSearchTest { viewModel, sync ->
    sync.insertWidget(
      id = NET_WORTH,
      page = PAGE_1.id,
      type = NetWorth,
      x = 0,
      y = 1,
      meta = json("""{"name":"Net worth"}"""),
    )
    sync.insertWidget(
      id = CASH_FLOW,
      page = PAGE_1.id,
      type = CashFlow,
      x = 0,
      y = 0,
      meta = json("""{"name":"Cash"}"""),
    )
    sync.insertWidget(
      id = SPENDING,
      page = PAGE_2.id,
      type = Spending,
      x = 0,
      y = 0,
      meta = json("""{"name":"NETFLIX"}"""),
    )

    viewModel.setQuery("net")

    viewModel.state.test {
      assertThat(awaitResults())
        .isEqualTo(
          listOf(
            SearchReportsGroup(PAGE_1, persistentListOf(item(NET_WORTH, NetWorth, "Net worth"))),
            SearchReportsGroup(PAGE_2, persistentListOf(item(SPENDING, Spending, "NETFLIX"))),
          )
        )
    }
  }

  @Test
  fun `Matches markdown content`() = runSearchTest { viewModel, sync ->
    sync.insertWidget(
      id = TEXT,
      page = PAGE_1.id,
      type = Markdown,
      x = 0,
      y = 0,
      meta = json("""{"content":"Save for a *Holiday*"}"""),
    )

    viewModel.setQuery("holiday")

    viewModel.state.test {
      assertThat(awaitResults())
        .isEqualTo(
          listOf(
            SearchReportsGroup(
              PAGE_1,
              persistentListOf(item(TEXT, Markdown, name = null, content = "Save for a Holiday")),
            )
          )
        )
    }
  }

  @Test
  fun `Matches custom report names`() = runSearchTest { viewModel, sync ->
    sync.renameCustomReport(CUSTOM_REPORT, "Groceries by month")
    sync.insertWidget(
      id = CUSTOM,
      page = PAGE_2.id,
      type = Custom,
      x = 0,
      y = 0,
      meta = json("""{"id":"custom-report"}"""),
    )

    viewModel.setQuery("GROCER")

    viewModel.state.test {
      assertThat(awaitResults())
        .isEqualTo(
          listOf(
            SearchReportsGroup(PAGE_2, persistentListOf(item(CUSTOM, Custom, "Groceries by month")))
          )
        )
    }
  }

  @Test
  fun `Matches widget type ignoring spaces and hyphens`() = runSearchTest { viewModel, sync ->
    sync.insertWidget(CASH_FLOW, PAGE_1.id, CashFlow, x = 0, y = 0, meta = json("{}"))
    sync.insertWidget(NET_WORTH, PAGE_1.id, NetWorth, x = 0, y = 0, meta = json("{}"))

    viewModel.setQuery("cash-flow")

    viewModel.state.test {
      assertThat(awaitResults())
        .isEqualTo(
          listOf(
            SearchReportsGroup(PAGE_1, persistentListOf(item(CASH_FLOW, CashFlow, name = null)))
          )
        )
    }
  }

  @Test
  fun `No matches shows no results`() = runSearchTest { viewModel, sync ->
    sync.insertWidget(
      id = NET_WORTH,
      page = PAGE_1.id,
      type = NetWorth,
      x = 0,
      y = 0,
      meta = json("""{"name":"Net worth"}"""),
    )

    viewModel.setQuery("xyz")

    viewModel.state.test {
      var state = awaitItem()
      while (state == NoQuery) state = awaitItem()
      assertThat(state).isEqualTo(NoResults)
    }
  }

  @Test
  fun `Skips reports on deleted pages`() = runSearchTest { viewModel, sync ->
    sync.insertWidget(
      id = NET_WORTH,
      page = PAGE_1.id,
      type = NetWorth,
      x = 0,
      y = 0,
      meta = json("""{"name":"Net worth"}"""),
    )
    sync.insertWidget(
      id = SPENDING,
      page = PAGE_2.id,
      type = Spending,
      x = 0,
      y = 0,
      meta = json("""{"name":"Net spend"}"""),
    )
    sync.deletePage(PAGE_2.id)

    viewModel.setQuery("net")

    viewModel.state.test {
      assertThat(awaitResults())
        .transform { groups -> groups.map { it.page } }
        .containsExactly(PAGE_1)
    }
  }

  private suspend fun ReceiveTurbine<SearchReportsState>.awaitResults(): List<SearchReportsGroup> {
    var state = awaitItem()
    while (state !is Results) state = awaitItem()
    cancelAndIgnoreRemainingEvents()
    return state.groups
  }

  private fun item(
    id: WidgetId,
    type: WidgetType,
    name: String?,
    content: String? = null,
  ) = SearchReportsItem(id, type, name, content)

  private fun json(string: String): JsonObject = Json.decodeFromString(string)

  private fun runSearchTest(action: suspend (SearchReportsViewModel, DashboardSync) -> Unit) =
    runSyncedDatabaseTest { scope, controller ->
      dashboardPagesQueries.withoutResult {
        insert(Dashboard_pages(PAGE_1.id, PAGE_1.name, tombstone = false))
        insert(Dashboard_pages(PAGE_2.id, PAGE_2.name, tombstone = false))
      }
      val dispatcher = StandardTestDispatcher(scope.testScheduler)
      // Keep viewModelScope on the test scheduler, so its queries can't outlive the database
      Dispatchers.setMain(dispatcher)
      val contexts = TestCoroutineContexts(dispatcher)
      val dao = DashboardDao(this, contexts)
      val sync = DashboardSync(dao, controller)
      val viewModel =
        SearchReportsViewModel(
          savedState = SavedStateHandle(),
          dashboardDao = dao,
          customReportsDao = CustomReportsDao(this, contexts),
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
    val PAGE_1 = DashboardPage(DashboardPageId("page-1"), "One")
    val PAGE_2 = DashboardPage(DashboardPageId("page-2"), "Two")
    val NET_WORTH = WidgetId("net-worth")
    val CASH_FLOW = WidgetId("cash-flow")
    val SPENDING = WidgetId("spending")
    val TEXT = WidgetId("text")
    val CUSTOM = WidgetId("custom")
    val CUSTOM_REPORT = CustomReportId("custom-report")
  }
}
