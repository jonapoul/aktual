package aktual.budget.db.dao

import aktual.budget.db.Dashboard_pages
import aktual.budget.db.withoutResult
import aktual.budget.model.DashboardPageId
import aktual.budget.model.WidgetId
import aktual.test.runDatabaseTest
import alakazam.test.TestCoroutineContexts
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

internal class DashboardDaoTest {
  @Test
  fun `Renaming keeps values we don't recognise`() = runDaoTest {
    val id = WidgetId("abc-123")
    insert(
      id,
      page = PAGE_1,
      type = NetWorth,
      x = 0,
      y = 0,
      meta = json("""{"name":"old","mode":"new-mode","extra":1}"""),
    )

    rename(id, "new")

    val meta = observeByPage(PAGE_1).first().single().meta
    assertThat(meta).isEqualTo(json("""{"name":"new","mode":"new-mode","extra":1}"""))
  }

  @Test
  fun `Renaming a widget with no meta`() = runDaoTest {
    val id = WidgetId("abc-123")
    insert(id, page = PAGE_1, type = AgeOfMoney, x = 0, y = 0, meta = null)

    rename(id, "new")

    val meta = observeByPage(PAGE_1).first().single().meta
    assertThat(meta).isEqualTo(json("""{"name":"new"}"""))
  }

  @Test
  fun `Only observe widgets on the requested page`() = runDaoTest {
    insert(WidgetId("a"), page = PAGE_1, type = NetWorth, x = 0, y = 0, meta = null)
    insert(WidgetId("b"), page = PAGE_2, type = CashFlow, x = 0, y = 0, meta = null)
    insert(WidgetId("c"), page = PAGE_1, type = AgeOfMoney, x = 4, y = 0, meta = null)

    assertThat(observeByPage(PAGE_1).first().map { row -> row.id })
      .containsExactly(WidgetId("c"), WidgetId("a"))
    assertThat(observeByPage(PAGE_2).first().map { row -> row.id }).containsExactly(WidgetId("b"))
    assertThat(getPositionAndSize(PAGE_2).map { row -> row.x }).containsExactly(0L)
  }

  @Test
  fun `Observe pages without tombstones`() = runDaoTest {
    assertThat(observePages().first().map { row -> row.id }).containsExactly(PAGE_1, PAGE_2)
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
