package aktual.budget.reports.vm.dashboard

import aktual.budget.db.Dashboard_pages
import aktual.budget.db.dao.DashboardDao
import aktual.budget.db.withoutResult
import aktual.budget.model.DashboardPageId
import aktual.test.TestBudgetLocalPreferences
import aktual.test.assertThatNextEmissionIsEqualTo
import aktual.test.runDatabaseTest
import alakazam.test.TestCoroutineContexts
import app.cash.turbine.test
import kotlin.test.Test
import kotlinx.coroutines.test.StandardTestDispatcher

class DashboardPagesTest {
  @Test
  fun `Selects first page by default`() = runPagesTest { pages ->
    pages.selected.test { assertThatNextEmissionIsEqualTo(PAGE_1) }
  }

  @Test
  fun `Remembers selected page`() = runPagesTest { pages ->
    pages.selected.test {
      assertThatNextEmissionIsEqualTo(PAGE_1)
      pages.select(PAGE_2.id)
      assertThatNextEmissionIsEqualTo(PAGE_2)
    }
  }

  @Test
  fun `Falls back to first page when selected page is missing`() = runPagesTest { pages ->
    pages.select(DashboardPageId("missing"))
    pages.selected.test { assertThatNextEmissionIsEqualTo(PAGE_1) }
  }

  private fun runPagesTest(action: suspend (DashboardPages) -> Unit) = runDatabaseTest { scope ->
    dashboardPagesQueries.withoutResult {
      insert(Dashboard_pages(PAGE_1.id, PAGE_1.name, tombstone = false))
      insert(Dashboard_pages(PAGE_2.id, PAGE_2.name, tombstone = false))
    }
    val contexts = TestCoroutineContexts(StandardTestDispatcher(scope.testScheduler))
    action(DashboardPages(DashboardDao(this, contexts), TestBudgetLocalPreferences()))
  }

  private companion object {
    val PAGE_1 = DashboardPage(DashboardPageId("page-1"), "One")
    val PAGE_2 = DashboardPage(DashboardPageId("page-2"), "Two")
  }
}
