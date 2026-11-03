package aktual.budget.reports.vm.dashboard

import aktual.budget.db.Dashboard_pages
import aktual.budget.db.dao.DashboardDao
import aktual.budget.db.withoutResult
import aktual.budget.model.DashboardPageId
import aktual.budget.reports.vm.DashboardSync
import aktual.budget.reports.vm.runSyncedDatabaseTest
import aktual.test.TestBudgetLocalPreferences
import aktual.test.assertThatNextEmissionIsEqualTo
import alakazam.test.TestCoroutineContexts
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
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

  @Test
  fun `Creating a page selects it`() = runPagesTest { pages ->
    pages.selected.test {
      assertThatNextEmissionIsEqualTo(PAGE_1)
      pages.create("New")
      assertThatNextEmissionIsEqualTo(DashboardPage(NEW_ID, "New"))
    }
  }

  @Test
  fun `Deleting the selected page falls back to the first page`() = runPagesTest { pages ->
    pages.select(PAGE_1.id)
    pages.selected.test {
      assertThatNextEmissionIsEqualTo(PAGE_1)
      pages.delete(PAGE_1.id)
      assertThatNextEmissionIsEqualTo(PAGE_2)
    }
  }

  @Test
  fun `Can't delete the last page`() = runPagesTest { pages ->
    pages.all.test {
      assertThatNextEmissionIsEqualTo([PAGE_1, PAGE_2])
      pages.delete(PAGE_1.id)
      assertThatNextEmissionIsEqualTo([PAGE_2])
      pages.delete(PAGE_2.id)
      expectNoEvents()
    }
  }

  @Test
  fun `Uses the selected page for a new report`() = runPagesTest { pages ->
    pages.select(PAGE_2.id)
    assertThat(pages.selectedOrCreate()).isEqualTo(PAGE_2.id)
  }

  @Test
  fun `Creates a page for a new report when there are none`() =
    runPagesTest(seed = false) { pages ->
      assertThat(pages.selectedOrCreate()).isEqualTo(NEW_ID)
      pages.all.test { assertThatNextEmissionIsEqualTo([DashboardPage(NEW_ID, "Main")]) }
    }

  private fun runPagesTest(seed: Boolean = true, action: suspend (DashboardPages) -> Unit) =
    runSyncedDatabaseTest { scope, controller ->
      if (seed) {
        dashboardPagesQueries.withoutResult {
          insert(Dashboard_pages(PAGE_1.id, PAGE_1.name, tombstone = false))
          insert(Dashboard_pages(PAGE_2.id, PAGE_2.name, tombstone = false))
        }
      }
      val contexts = TestCoroutineContexts(StandardTestDispatcher(scope.testScheduler))
      val dao = DashboardDao(this, contexts)
      val pages =
        DashboardPages(
          dao = dao,
          sync = DashboardSync(dao, controller),
          prefs = TestBudgetLocalPreferences(),
          uuidGenerator = { NEW_ID.value },
        )
      action(pages)
    }

  private companion object {
    val PAGE_1 = DashboardPage(DashboardPageId("page-1"), "One")
    val PAGE_2 = DashboardPage(DashboardPageId("page-2"), "Two")
    val NEW_ID = DashboardPageId("new-page")
  }
}
