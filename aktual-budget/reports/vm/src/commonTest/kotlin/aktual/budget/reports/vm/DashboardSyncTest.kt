package aktual.budget.reports.vm

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.Dashboard
import aktual.budget.db.Dashboard_pages
import aktual.budget.db.dao.DashboardDao
import aktual.budget.db.dao.DatabaseTables.CUSTOM_REPORTS
import aktual.budget.db.withoutResult
import aktual.budget.model.CustomReportId
import aktual.budget.model.DashboardPageId
import aktual.budget.model.LocalChange
import aktual.budget.model.MessageValue
import aktual.budget.model.WidgetId
import aktual.test.TestSyncController
import aktual.test.runDatabaseTest
import alakazam.test.TestCoroutineContexts
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.extracting
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlin.test.Test
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

class DashboardSyncTest {
  @Test
  fun `Insert a widget`() = runSyncTest { dao ->
    insertWidget(WIDGET, PAGE_1, NetWorth, x = 4, y = 2, meta = json("""{"name":"Net"}"""))

    val row = dao.observeByPage(PAGE_1).first().single()
    assertThat(row.id).isEqualTo(WIDGET)
    assertThat(row.type).isEqualTo(NetWorth)
    assertThat(row.x).isEqualTo(4L)
    assertThat(row.y).isEqualTo(2L)
    assertThat(row.width).isEqualTo(DashboardDao.DEFAULT_WIDTH)
    assertThat(row.height).isEqualTo(DashboardDao.DEFAULT_HEIGHT)
    assertThat(row.meta).isEqualTo(json("""{"name":"Net"}"""))
  }

  @Test
  fun `Renaming keeps values we don't recognise`() = runSyncTest { dao ->
    dao.insert(
      WIDGET,
      page = PAGE_1,
      type = NetWorth,
      x = 0,
      y = 0,
      meta = json("""{"name":"old","mode":"new-mode","extra":1}"""),
    )

    renameWidget(WIDGET, "new")

    val meta = dao.observeByPage(PAGE_1).first().single().meta
    assertThat(meta).isEqualTo(json("""{"name":"new","mode":"new-mode","extra":1}"""))
  }

  @Test
  fun `Renaming a widget with no meta`() = runSyncTest { dao ->
    dao.insert(WIDGET, page = PAGE_1, type = AgeOfMoney, x = 0, y = 0, meta = null)

    renameWidget(WIDGET, "new")

    val meta = dao.observeByPage(PAGE_1).first().single().meta
    assertThat(meta).isEqualTo(json("""{"name":"new"}"""))
  }

  @Test
  fun `Setting text content keeps other values`() = runSyncTest { dao ->
    dao.insert(
      WIDGET,
      page = PAGE_1,
      type = Markdown,
      x = 0,
      y = 0,
      meta = json("""{"content":"old","text_align":"center"}"""),
    )

    setWidgetContent(WIDGET, "# New")

    val meta = dao.observeByPage(PAGE_1).first().single().meta
    assertThat(meta).isEqualTo(json("""{"content":"# New","text_align":"center"}"""))
  }

  @Test
  fun `Delete a widget`() = runSyncTest { dao ->
    dao.insert(WIDGET, page = PAGE_1, type = NetWorth, x = 0, y = 0, meta = null)

    deleteWidget(WIDGET)

    assertThat(dao.observeByPage(PAGE_1).first()).isEmpty()
  }

  @Test
  fun `Rename a custom report`() = runDatabaseTest { scope ->
    val controller = TestSyncController()
    val sync = DashboardSync(dao(scope.testScheduler), controller)

    sync.renameCustomReport(CustomReportId("report"), "new")

    assertThat(controller.changes)
      .containsExactly(LocalChange(CUSTOM_REPORTS, "report", "name", MessageValue.String("new")))
  }

  @Test
  fun `Insert a page`() = runSyncTest { dao ->
    insertPage(PAGE_3, "Three")

    assertThat(dao.observePages().first())
      .extracting(Dashboard_pages::id)
      .containsExactly(PAGE_1, PAGE_2, PAGE_3)
  }

  @Test
  fun `Rename a page`() = runSyncTest { dao ->
    renamePage(PAGE_1, "New")

    assertThat(dao.observePages().first())
      .extracting(Dashboard_pages::name)
      .containsExactly("New", "Two")
  }

  @Test
  fun `Deleting a page removes its widgets`() = runSyncTest { dao ->
    dao.insert(WidgetId("a"), page = PAGE_1, type = NetWorth, x = 0, y = 0, meta = null)
    dao.insert(WidgetId("b"), page = PAGE_2, type = CashFlow, x = 0, y = 0, meta = null)

    assertThat(deletePage(PAGE_1)).isTrue()

    assertThat(dao.observePages().first()).extracting(Dashboard_pages::id).containsExactly(PAGE_2)
    assertThat(dao.observeByPage(PAGE_1).first()).isEmpty()
    assertThat(dao.observeByPage(PAGE_2).first())
      .extracting(Dashboard::id)
      .containsExactly(WidgetId("b"))
  }

  @Test
  fun `Can't delete the last page`() = runSyncTest { dao ->
    assertThat(deletePage(PAGE_1)).isTrue()
    assertThat(deletePage(PAGE_2)).isFalse()

    assertThat(dao.observePages().first()).extracting(Dashboard_pages::id).containsExactly(PAGE_2)
  }

  private fun json(string: String): JsonObject = Json.decodeFromString(string)

  private fun BudgetDatabase.dao(scheduler: TestCoroutineScheduler) =
    DashboardDao(this, TestCoroutineContexts(StandardTestDispatcher(scheduler)))

  private fun runSyncTest(action: suspend DashboardSync.(DashboardDao) -> Unit) =
    runSyncedDatabaseTest { scope, controller ->
      dashboardPagesQueries.withoutResult {
        insert(Dashboard_pages(PAGE_1, name = "One", tombstone = false))
        insert(Dashboard_pages(PAGE_2, name = "Two", tombstone = false))
      }
      val dao = dao(scope.testScheduler)
      action(DashboardSync(dao, controller), dao)
    }

  private companion object {
    val PAGE_1 = DashboardPageId("page-1")
    val PAGE_2 = DashboardPageId("page-2")
    val PAGE_3 = DashboardPageId("page-3")
    val WIDGET = WidgetId("widget")
  }
}
