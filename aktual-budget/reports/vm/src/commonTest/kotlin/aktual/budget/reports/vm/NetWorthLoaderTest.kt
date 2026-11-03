package aktual.budget.reports.vm

import aktual.budget.BudgetSyncController
import aktual.budget.db.BudgetDatabase
import aktual.budget.db.dao.DatabaseTables.ACCOUNTS
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.Condition
import aktual.budget.model.Interval
import alakazam.test.TestCoroutineContexts
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.extracting
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.prop
import kotlin.test.Test
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.yearMonth
import kotlinx.serialization.json.JsonPrimitive

class NetWorthLoaderTest {
  @Test
  fun `Running balance per account, starting from the month before the range`() =
    runLoaderTest { loader, sync ->
      sync.insertAccounts()
      sync.transaction("old", 1000, account = "checking", date = 20260115)
      sync.transaction("feb", 500, account = "checking", date = 20260210)
      sync.transaction("mar", 200, account = "checking", date = 20260305)
      sync.transaction("card", -300, account = "card", date = 20260320)

      loader.netWorth(meta(mode = Stacked)).test {
        val data = awaitItem() as NetWorthData
        assertThat(data.mode).isEqualTo(Stacked)
        assertThat(data.items)
          .isEqualTo(persistentMapOf(JAN to Amount(1000), FEB to Amount(1500), MAR to Amount(1400)))
        assertThat(data.accounts)
          .containsExactly(
            account("card", "Card", JAN to 0, FEB to 0, MAR to -300),
            account("checking", "Checking", JAN to 1000, FEB to 1500, MAR to 1700),
          )
      }
    }

  @Test
  fun `Accounts at zero, deleted accounts and future transactions are left out`() =
    runLoaderTest { loader, sync ->
      sync.insertAccounts()
      sync.row(ACCOUNTS, "deleted", "name" to "Deleted", "tombstone" to true)
      sync.transaction("checking", 1000, account = "checking", date = 20260210)
      sync.transaction("in", 100, account = "card", date = 20260210)
      sync.transaction("out", -100, account = "card", date = 20260210)
      sync.transaction("deleted", 5000, account = "deleted", date = 20260210)
      sync.transaction("future", 7000, account = "checking", date = 20260410)

      loader.netWorth(meta(mode = Stacked)).test {
        val data = awaitItem() as NetWorthData
        assertThat(data.accounts).extracting(NetWorthAccount::name).containsExactly("Checking")
        assertThat(data.items.getValue(MAR)).isEqualTo(Amount(1000))
      }
    }

  @Test
  fun `Unknown mode is drawn as a trend`() = runLoaderTest { loader, sync ->
    sync.insertAccounts()
    sync.transaction("feb", 500, account = "checking", date = 20260210)

    loader.netWorth(meta(mode = Unknown)).test {
      assertThat(awaitItem()).isInstanceOf<NetWorthData>().prop(NetWorthData::mode).isEqualTo(Trend)
    }
  }

  @Test
  fun `Smallest total over the range is stacked first`() {
    val data =
      netWorthData(
        title = null,
        mode = Stacked,
        interval = Monthly,
        periods = listOf(JAN, FEB),
        rows =
          listOf(
            row("big", period = null, total = 100),
            row("small", period = JAN, total = 10),
            row("debt", period = FEB, total = -50),
          ),
      )

    assertThat(data.accounts)
      .extracting(NetWorthAccount::name)
      .containsExactly("debt", "small", "big")
  }

  @Test
  fun `Conditions filter the balances`() = runLoaderTest { loader, sync ->
    sync.insertAccounts()
    sync.transaction("old", 1000, account = "checking", date = 20260115)
    sync.transaction("card", -300, account = "card", date = 20260320)

    loader.netWorth(meta(conditions = listOf(CHECKING))).test {
      val data = awaitItem() as NetWorthData
      assertThat(data.items)
        .isEqualTo(persistentMapOf(JAN to Amount(1000), FEB to Amount(1000), MAR to Amount(1000)))
    }
  }

  @Test
  fun `Weekly periods start on the first day of the week and stop at today`() =
    runLoaderTest { loader, sync ->
      sync.insertAccounts()
      sync.transaction("old", 1000, account = "checking", date = 20260115)
      sync.transaction("apr", 200, account = "checking", date = 20260408)

      val april = TimeFrame(APR, APR, Static)
      loader.netWorth(meta(interval = Weekly, timeFrame = april)).test {
        val data = awaitItem() as NetWorthData
        assertThat(data.interval).isEqualTo(Weekly)
        // Sundays, from the week before April up to today
        assertThat(data.items)
          .isEqualTo(
            persistentMapOf(
              LocalDate(2026, 3, 29) to Amount(1000),
              LocalDate(2026, 4, 5) to Amount(1200),
              LocalDate(2026, 4, 12) to Amount(1200),
            ),
          )
      }
    }

  @Test
  fun `Weekly range after today still has a period`() = runLoaderTest { loader, sync ->
    sync.insertAccounts()
    sync.transaction("old", 1000, account = "checking", date = 20260115)

    val june = TimeFrame(YearMonth(2026, 6), YearMonth(2026, 6), Static)
    loader.netWorth(meta(interval = Weekly, timeFrame = june)).test {
      val data = awaitItem() as NetWorthData
      assertThat(data.items).isEqualTo(persistentMapOf(LocalDate(2026, 5, 31) to Amount(1000)))
    }
  }

  @Test
  fun `Yearly periods sum each year`() = runLoaderTest { loader, sync ->
    sync.insertAccounts()
    sync.transaction("old", 1000, account = "checking", date = 20250115)
    sync.transaction("new", 500, account = "checking", date = 20260210)

    val years = TimeFrame(YearMonth(2025, 6), YearMonth(2026, 3), Static)
    loader.netWorth(meta(interval = Yearly, timeFrame = years)).test {
      val data = awaitItem() as NetWorthData
      assertThat(data.items)
        .isEqualTo(
          persistentMapOf(LocalDate(2025, 1, 1) to Amount(1000), JAN to Amount(1500)),
        )
    }
  }

  private suspend fun BudgetSyncController.insertAccounts() {
    row(ACCOUNTS, "checking", "name" to "Checking", "offbudget" to false)
    row(ACCOUNTS, "card", "name" to "Card", "offbudget" to false)
  }

  private fun account(id: String, name: String, vararg balances: Pair<LocalDate, Long>) =
    NetWorthAccount(
      id = AccountId(id),
      name = name,
      balances = persistentMapOf(*balances.map { (m, a) -> m to Amount(a) }.toTypedArray()),
    )

  private fun row(account: String, period: LocalDate?, total: Long) =
    NetWorthRow(AccountId(account), name = account, period = period, total = total)

  private fun meta(
    mode: NetWorthMode = Trend,
    interval: Interval? = null,
    conditions: List<Condition>? = null,
    timeFrame: TimeFrame = TimeFrame(FEB.yearMonth, MAR.yearMonth, Static),
  ) =
    NetWorthReportMeta(
      name = "Net worth",
      conditions = conditions,
      conditionsOp = null,
      timeFrame = timeFrame,
      interval = interval,
      mode = mode,
    )

  private fun runLoaderTest(
    action: suspend BudgetDatabase.(ChartDataLoader, BudgetSyncController) -> Unit,
  ) = runSyncedDatabaseTest { scope, sync ->
    val contexts = TestCoroutineContexts(StandardTestDispatcher(scope.testScheduler))
    val loader = chartDataLoader(contexts, calendar = { TODAY })
    action(loader, sync)
  }

  private companion object {
    val TODAY = LocalDate(2026, 4, 15)
    val APR = YearMonth(2026, 4)
    val JAN = LocalDate(2026, 1, 1)
    val FEB = LocalDate(2026, 2, 1)
    val MAR = LocalDate(2026, 3, 1)
    val CHECKING = Condition(field = Account, operator = Is, value = JsonPrimitive("checking"))
  }
}
