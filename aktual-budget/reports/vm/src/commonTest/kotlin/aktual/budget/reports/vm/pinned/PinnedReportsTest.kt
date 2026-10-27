package aktual.budget.reports.vm.pinned

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.Dashboard_pages
import aktual.budget.db.dao.CustomReportsDao
import aktual.budget.db.dao.DashboardDao
import aktual.budget.db.withoutResult
import aktual.budget.model.Currency
import aktual.budget.model.DashboardPageId
import aktual.budget.model.DbMetadataKey
import aktual.budget.model.WidgetId
import aktual.budget.reports.vm.DashboardSync
import aktual.budget.reports.vm.TestCurrencyPreferences
import aktual.budget.reports.vm.TextData
import aktual.budget.reports.vm.chartDataLoader
import aktual.budget.reports.vm.dashboard.DashboardItemDecoder
import aktual.budget.reports.vm.dashboard.DashboardPage
import aktual.budget.reports.vm.dashboard.DashboardPages
import aktual.budget.reports.vm.runSyncedDatabaseTest
import aktual.prefs.CurrencyPreferences
import aktual.prefs.Preference
import aktual.test.TestBudgetLocalPreferences
import aktual.test.assertThatNextEmissionIsEqualTo
import alakazam.test.TestCoroutineContexts
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class PinnedReportsTest {
  @Test
  fun `Pins up to three reports`() = runPinnedTest { pinned, _, prefs ->
    pinned.pin(A)
    pinned.pin(B)
    pinned.pin(C)
    pinned.pin(D)
    assertThat(prefs[DbMetadataKey.HomeReports]).isEqualTo(listOf("a", "b", "c"))
  }

  @Test
  fun `Pinning a report twice keeps one pin`() = runPinnedTest { pinned, _, prefs ->
    pinned.pin(A)
    pinned.pin(A)
    assertThat(prefs[DbMetadataKey.HomeReports]).isEqualTo(listOf("a"))
  }

  @Test
  fun `Replacing takes the old report's place`() = runPinnedTest { pinned, _, prefs ->
    pinned.pin(A)
    pinned.pin(B)
    pinned.replace(A, C)
    assertThat(prefs[DbMetadataKey.HomeReports]).isEqualTo(listOf("c", "b"))
  }

  @Test
  fun `Replacing with a pinned report changes nothing`() = runPinnedTest { pinned, _, prefs ->
    pinned.pin(A)
    pinned.pin(B)
    pinned.replace(A, B)
    assertThat(prefs[DbMetadataKey.HomeReports]).isEqualTo(listOf("a", "b"))
  }

  @Test
  fun `Unpinning removes the report`() = runPinnedTest { pinned, _, prefs ->
    pinned.pin(A)
    pinned.pin(B)
    pinned.unpin(A)
    assertThat(prefs[DbMetadataKey.HomeReports]).isEqualTo(listOf("b"))
  }

  @Test
  fun `Deleted reports are skipped and free their slot`() = runPinnedTest { pinned, sync, prefs ->
    pinned.pin(A)
    pinned.pin(B)
    pinned.pin(C)
    sync.deleteWidget(A)

    pinned.ids.test { assertThatNextEmissionIsEqualTo(listOf(B, C)) }

    pinned.pin(D)
    assertThat(prefs[DbMetadataKey.HomeReports]).isEqualTo(listOf("b", "c", "d"))
  }

  @Test
  fun `Reports with no type don't take a slot`() = runPinnedTest { pinned, _, prefs ->
    dashboardQueries.withoutResult {
      insert(
        id = UNTYPED,
        type = null,
        width = 4,
        height = 2,
        x = 0,
        y = 0,
        meta = null,
        dashboard_page_id = PAGE_1.id,
      )
    }
    pinned.pin(A)
    pinned.pin(UNTYPED)
    pinned.pin(B)

    pinned.ids.test { assertThatNextEmissionIsEqualTo(listOf(A, B)) }

    pinned.pin(C)
    assertThat(prefs[DbMetadataKey.HomeReports]).isEqualTo(listOf("a", "b", "c"))
  }

  @Test
  fun `Pinned reports load their charts`() = runPinnedTest { pinned, _, _ ->
    pinned.pin(B)
    pinned.pin(A)

    pinned.observe().test {
      var reports = awaitItem()
      while (reports.any { it.data == null }) reports = awaitItem()
      assertThat(reports.map { it.summary.id to it.data })
        .containsExactly(B to TextData("b"), A to TextData("a"))
    }
  }

  @Test
  fun `Pinning another report keeps loaded charts`() = runPinnedTest { pinned, _, _ ->
    pinned.pin(A)

    pinned.observe().test {
      var reports = awaitItem()
      while (reports.any { it.data == null }) reports = awaitItem()

      pinned.pin(B)
      do {
        reports = awaitItem()
        assertThat(reports.first().data).isEqualTo(TextData("a"))
      } while (reports.size < 2 || reports[1].data == null)
    }
  }

  @Test
  fun `A chart that fails to load doesn't stop the others`() =
    runPinnedTest(currencyPreferences = BrokenCurrencyPreferences) { pinned, sync, _ ->
      val meta = buildJsonObject { put("formula", "=1") }
      sync.insertWidget(FORMULA, PAGE_2.id, Formula, x = 0, y = 1, meta = meta)
      pinned.pin(FORMULA)
      pinned.pin(A)

      pinned.observe().test {
        var reports = awaitItem()
        while (reports.size < 2 || reports[1].data == null) reports = awaitItem()
        assertThat(reports.map { it.summary.id to it.data })
          .containsExactly(FORMULA to null, A to TextData("a"))
        expectNoEvents()
      }
    }

  @Test
  fun `Choices list every report by page, as the dashboard orders them`() =
    runPinnedTest { pinned, _, _ ->
      pinned.observeChoices().test {
        assertThatNextEmissionIsEqualTo(
          persistentListOf(
            ReportPage(
              PAGE_1,
              persistentListOf(summary(C), summary(B), summary(A)),
            ),
            ReportPage(PAGE_2, persistentListOf(summary(D))),
          ),
        )
      }
    }

  private fun summary(id: WidgetId) = ReportSummary(id, Markdown, name = null)

  private fun runPinnedTest(
    currencyPreferences: CurrencyPreferences = TestCurrencyPreferences,
    action:
      suspend BudgetDatabase.(PinnedReports, DashboardSync, TestBudgetLocalPreferences) -> Unit,
  ) = runSyncedDatabaseTest { scope, controller ->
    dashboardPagesQueries.withoutResult {
      insert(Dashboard_pages(PAGE_1.id, PAGE_1.name, tombstone = false))
      insert(Dashboard_pages(PAGE_2.id, PAGE_2.name, tombstone = false))
    }
    val contexts = TestCoroutineContexts(StandardTestDispatcher(scope.testScheduler))
    val dao = DashboardDao(this, contexts)
    val sync = DashboardSync(dao, controller)
    listOf(A to PAGE_1, B to PAGE_1, C to PAGE_1, D to PAGE_2).forEachIndexed { y, (id, page) ->
      val meta = buildJsonObject { put("content", id.value) }
      sync.insertWidget(id, page.id, Markdown, x = 0, y = y.toLong(), meta = meta)
    }
    val prefs = TestBudgetLocalPreferences()
    val pinned =
      PinnedReportsImpl(
        prefs = prefs,
        dashboardDao = dao,
        customReportsDao = CustomReportsDao(this, contexts),
        pages = DashboardPages(dao = dao, sync = sync, prefs = prefs, uuidGenerator = { "unused" }),
        decoder = DashboardItemDecoder(),
        chartDataLoader =
          chartDataLoader(
            contexts = contexts,
            calendar = { LocalDate(2026, 1, 1) },
            currencyPreferences = currencyPreferences,
          ),
      )
    action(pinned, sync, prefs)
  }

  private companion object {
    val PAGE_1 = DashboardPage(DashboardPageId("page-1"), "One")
    val PAGE_2 = DashboardPage(DashboardPageId("page-2"), "Two")
    val A = WidgetId("a")
    val B = WidgetId("b")
    val C = WidgetId("c")
    val D = WidgetId("d")
    val UNTYPED = WidgetId("untyped")
    val FORMULA = WidgetId("formula")
  }

  private object BrokenCurrencyPreferences : CurrencyPreferences by TestCurrencyPreferences {
    override val currency =
      object : Preference<Currency> by TestCurrencyPreferences.currency {
        override fun asFlow(): Flow<Currency> = flow { error("Broken") }
      }
  }
}
