package aktual.budget.reports.vm

import aktual.budget.BudgetSyncController
import aktual.budget.db.BudgetDatabase
import aktual.budget.db.dao.DatabaseTables.ACCOUNTS
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
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
        months = listOf(JAN, FEB),
        rows =
          listOf(
            row("big", month = null, total = 100),
            row("small", month = JAN, total = 10),
            row("debt", month = FEB, total = -50),
          ),
      )

    assertThat(data.accounts)
      .extracting(NetWorthAccount::name)
      .containsExactly("debt", "small", "big")
  }

  private suspend fun BudgetSyncController.insertAccounts() {
    row(ACCOUNTS, "checking", "name" to "Checking", "offbudget" to false)
    row(ACCOUNTS, "card", "name" to "Card", "offbudget" to false)
  }

  private fun account(id: String, name: String, vararg balances: Pair<YearMonth, Long>) =
    NetWorthAccount(
      id = AccountId(id),
      name = name,
      balances = persistentMapOf(*balances.map { (m, a) -> m to Amount(a) }.toTypedArray()),
    )

  private fun row(account: String, month: YearMonth?, total: Long) =
    NetWorthRow(AccountId(account), name = account, month = month, total = total)

  private fun meta(mode: NetWorthMode) =
    NetWorthReportMeta(
      name = "Net worth",
      conditions = null,
      conditionsOp = null,
      timeFrame = TimeFrame(FEB, MAR, Static),
      interval = null,
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
    val JAN = YearMonth(2026, 1)
    val FEB = YearMonth(2026, 2)
    val MAR = YearMonth(2026, 3)
  }
}
