package aktual.budget.reports.vm

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.dao.DatabaseTables.ACCOUNTS
import aktual.budget.db.dao.DatabaseTables.PAYEES
import aktual.budget.db.dao.DatabaseTables.PAYEE_MAPPING
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.utils.BudgetSyncController
import aktual.test.insertSchedule
import alakazam.test.TestCoroutineContexts
import app.cash.turbine.test
import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import assertk.assertions.isZero
import assertk.assertions.key
import assertk.assertions.prop
import kotlin.test.Test
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

class BalanceForecastLoaderTest {
  @Test
  fun `Transfer posted from an unselected account isn't forecast again`() =
    runLoaderTest { loader, sync ->
      insertSchedule(
        id = "s",
        name = "To savings",
        payee = "Savings",
        account = "Checking",
        nextDate = TODAY,
      )
      sync.row(ACCOUNTS, "savings", "name" to "Savings", "offbudget" to false)
      sync.row(PAYEES, "s-payee", "transfer_acct" to "savings")
      sync.row(PAYEES, "to-checking", "transfer_acct" to "s-account")
      sync.row(PAYEE_MAPPING, "to-checking", "targetId" to "to-checking")
      sync.transaction(
        "out",
        -1000,
        payee = "s-payee",
        account = "s-account",
        transfer = "in",
        date = 20260501,
        schedule = "s",
      )
      sync.transaction(
        "in",
        1000,
        payee = "to-checking",
        account = "savings",
        transfer = "out",
        date = 20260501,
      )

      loader.balanceForecast(meta(accounts = listOf(AccountId("savings")))).test {
        assertThat(awaitItem()).isInstanceOf<BalanceForecastData>().all {
          prop(BalanceForecastData::items).key(MAY.firstDay).isEqualTo(Amount(1000L))
          prop(BalanceForecastData::scheduledCount).isZero()
        }
      }
    }

  @Test
  fun `Starting today replaces the saved range`() = runLoaderTest { loader, sync ->
    sync.row(ACCOUNTS, "checking", "name" to "Checking", "offbudget" to false)
    val meta =
      meta(accounts = listOf(AccountId("checking")))
        .copy(granularity = Daily, monthsFromToday = 2, yAxisBase = ZeroBased)

    loader.balanceForecast(meta).test {
      assertThat(awaitItem()).isInstanceOf<BalanceForecastData>().all {
        prop(BalanceForecastData::end).isEqualTo(YearMonth(2026, 6))
        prop(BalanceForecastData::items).transform { it.keys.first() }.isEqualTo(TODAY)
        prop(BalanceForecastData::items)
          .transform { it.keys.last() }
          .isEqualTo(LocalDate(2026, 6, 30))
        prop(BalanceForecastData::zeroBased).isTrue()
      }
    }
  }

  private fun meta(accounts: List<AccountId>) =
    BalanceForecastReportMeta(
      name = "Forecast",
      accounts = accounts,
      timeFrame = TimeFrame(MAY, MAY, Static),
      granularity = Monthly,
    )

  private fun runLoaderTest(
    action: suspend BudgetDatabase.(ChartDataLoader, BudgetSyncController) -> Unit,
  ) = runSyncedDatabaseTest { scope, sync ->
    val contexts = TestCoroutineContexts(StandardTestDispatcher(scope.testScheduler))
    action(chartDataLoader(contexts, calendar = { TODAY }), sync)
  }

  private companion object {
    val TODAY = LocalDate(2026, 5, 1)
    val MAY = YearMonth(2026, 5)
  }
}
