package aktual.budget.db.dao

import aktual.budget.db.Dashboard
import aktual.budget.db.Dashboard_pages
import aktual.budget.db.GetPositionAndSize
import aktual.budget.db.withoutResult
import aktual.budget.model.DashboardPageId
import aktual.budget.model.WidgetId
import aktual.test.runDatabaseTest
import alakazam.test.TestCoroutineContexts
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.extracting
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

internal class DashboardDaoTest {
  @Test
  fun `Only observe widgets on the requested page`() = runDaoTest {
    insert(WidgetId("a"), page = PAGE_1, type = NetWorth, x = 0, y = 0, meta = null)
    insert(WidgetId("b"), page = PAGE_2, type = CashFlow, x = 0, y = 0, meta = null)
    insert(WidgetId("c"), page = PAGE_1, type = AgeOfMoney, x = 4, y = 0, meta = null)

    assertThat(observeByPage(PAGE_1).first())
      .extracting(Dashboard::id)
      .containsExactly(WidgetId("c"), WidgetId("a"))
    assertThat(observeByPage(PAGE_2).first())
      .extracting(Dashboard::id)
      .containsExactly(WidgetId("b"))
    assertThat(getPositionAndSize(PAGE_2)).extracting(GetPositionAndSize::x).containsExactly(0L)
  }

  @Test
  fun `Observe pages without tombstones`() = runDaoTest {
    assertThat(observePages().first())
      .extracting(Dashboard_pages::id)
      .containsExactly(PAGE_1, PAGE_2)
  }

  @Test
  fun `Count pages without tombstones`() = runDaoTest { assertThat(countPages()).isEqualTo(2L) }

  @Test
  fun `Widget IDs on a page`() = runDaoTest {
    insert(WidgetId("a"), page = PAGE_1, type = NetWorth, x = 0, y = 0, meta = null)
    insert(WidgetId("b"), page = PAGE_2, type = CashFlow, x = 0, y = 0, meta = null)

    assertThat(widgetIds(PAGE_1)).containsExactly(WidgetId("a"))
  }

  @Test
  fun `Meta of a widget`() = runDaoTest {
    insert(WidgetId("a"), page = PAGE_1, type = NetWorth, x = 0, y = 0, meta = json("""{"a":1}"""))
    insert(WidgetId("b"), page = PAGE_1, type = CashFlow, x = 0, y = 0, meta = null)

    assertThat(meta(WidgetId("a"))).isEqualTo(json("""{"a":1}"""))
    assertThat(meta(WidgetId("b"))).isEqualTo(JsonObject(emptyMap()))
    assertThat(meta(WidgetId("missing"))).isNull()
  }

  private fun json(string: String): JsonObject = Json.decodeFromString(string)

  private fun runDaoTest(action: suspend DashboardDao.(TestScope) -> Unit) =
    runDatabaseTest { scope ->
      dashboardPagesQueries.withoutResult {
        insert(Dashboard_pages(PAGE_1, name = "One", tombstone = false))
        insert(Dashboard_pages(PAGE_2, name = "Two", tombstone = false))
        insert(Dashboard_pages(DashboardPageId("deleted"), name = "Gone", tombstone = true))
      }
      val dao =
        DashboardDao(this, TestCoroutineContexts(StandardTestDispatcher(scope.testScheduler)))
      action(dao, scope)
    }

  private companion object {
    val PAGE_1 = DashboardPageId("page-1")
    val PAGE_2 = DashboardPageId("page-2")
  }
}
