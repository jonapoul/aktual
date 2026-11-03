package aktual.budget.reports.vm

import aktual.budget.BudgetSyncController
import aktual.budget.db.BudgetDatabase
import aktual.budget.db.dao.ReportsDao
import aktual.budget.model.Amount
import aktual.budget.model.Condition
import alakazam.test.TestCoroutineContexts
import app.cash.turbine.test
import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.prop
import kotlin.test.Test
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.serialization.json.JsonPrimitive

class CashFlowLoaderTest {
  @Test
  fun `Without conditions, totalling the transactions matches the SQL totals`() =
    runLoaderTest { _, sync, dao ->
      sync.insertReportBudget()

      val start = MAY.firstDay
      val end = MAY.lastDay
      val rows = dao.observeReportTransactions(ALL_TIME_START, end).first()
      val (balance, byMonth) = cashFlowTotals(rows, start)

      assertThat(balance).isEqualTo(dao.observeCashFlowStartingBalance(start).first())
      assertThat(byMonth)
        .containsExactlyInAnyOrder(*dao.observeCashFlowByMonth(start, end).first().toTypedArray())
    }

  @Test
  fun `Conditions filter the totals and the starting balance`() = runLoaderTest { loader, sync, _ ->
    sync.insertReportBudget()

    loader.cashFlow(meta(conditions = [FOOD])).test {
      val may = (awaitItem() as CashFlowData).items.getValue(MAY)
      assertThat(may).all {
        prop(CashFlowDatum::income).isEqualTo(Zero)
        prop(CashFlowDatum::expenses).isEqualTo(Amount(-300L))
        prop(CashFlowDatum::transfers).isEqualTo(Zero)
        prop(CashFlowDatum::balance).isEqualTo(Amount(-700L))
      }
    }
  }

  @Test
  fun `Hides the balance when asked`() = runLoaderTest { loader, sync, _ ->
    sync.insertReportBudget()

    loader.cashFlow(meta(showBalance = false)).test {
      assertThat(awaitItem()).isInstanceOf<CashFlowData>().prop(CashFlowData::showBalance).isFalse()
    }
  }

  private fun meta(conditions: List<Condition>? = null, showBalance: Boolean? = null) =
    CashFlowReportMeta(
      name = "Cash flow",
      conditions = conditions,
      timeFrame = TimeFrame(MAY, MAY, Static),
      showBalance = showBalance,
    )

  private fun runLoaderTest(
    action: suspend BudgetDatabase.(ChartDataLoader, BudgetSyncController, ReportsDao) -> Unit,
  ) = runSyncedDatabaseTest { scope, sync ->
    val contexts = TestCoroutineContexts(StandardTestDispatcher(scope.testScheduler))
    val loader = chartDataLoader(contexts, calendar = { TODAY })
    action(loader, sync, ReportsDao(this, contexts))
  }

  private companion object {
    val TODAY = LocalDate(2026, 6, 15)
    val MAY = YearMonth(2026, 5)
    val FOOD = Condition(field = Category, operator = Is, value = JsonPrimitive("food"))
  }
}
