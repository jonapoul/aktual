package aktual.budget.reports.vm

import aktual.budget.BudgetSyncController
import aktual.budget.db.BudgetDatabase
import aktual.budget.db.dao.DatabaseTables.ACCOUNTS
import aktual.budget.db.dao.DatabaseTables.CATEGORIES
import aktual.budget.db.dao.DatabaseTables.CATEGORY_GROUPS
import aktual.budget.db.dao.DatabaseTables.CATEGORY_MAPPING
import aktual.budget.db.dao.DatabaseTables.PAYEES
import aktual.budget.db.dao.DatabaseTables.PAYEE_MAPPING
import aktual.budget.db.dao.ReportsDao
import aktual.budget.model.Condition
import aktual.budget.model.ConditionOp
import alakazam.test.TestCoroutineContexts
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.doesNotContain
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEmpty
import kotlin.test.Test
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive

class SankeyLoaderTest {
  @Test
  fun `Without conditions, totalling the transactions matches the SQL totals`() =
    runLoaderTest { _, sync, dao ->
      sync.insertBudget()

      val start = MAY.firstDay
      val end = MAY.lastDay
      val rows = dao.observeReportTransactions(start, end).first()
      val categories = dao.observeSankeyCategoryTotals(start, end).first()
      val transfers = dao.observeSankeyTransfers(start, end).first()

      assertThat(categories).isNotEmpty()
      assertThat(transfers).isNotEmpty()
      assertThat(sankeyCategoryTotals(rows)).containsExactlyInAnyOrder(*categories.toTypedArray())
      assertThat(sankeyTransfers(rows)).containsExactlyInAnyOrder(*transfers.toTypedArray())
    }

  @Test
  fun `Unfiltered report has every category`() = runLoaderTest { loader, sync, _ ->
    sync.insertBudget()

    loader.sankey(meta()).test {
      assertThat(awaitItem().nodeKeys())
        .containsExactlyInAnyOrder("employer", "salary", "checking", "bills", "food", "fuel")
    }
  }

  @Test
  fun `Keeps the transactions matching the conditions`() = runLoaderTest { loader, sync, _ ->
    sync.insertBudget()

    loader.sankey(meta(conditions = [EMPLOYER, FOOD], op = Or)).test {
      val data = awaitItem() as SankeyData
      assertThat(data.nodeKeys())
        .containsExactlyInAnyOrder("employer", "salary", "checking", "bills", "food")
      assertThat(data.nodes.first { it.key == "food" }.value.toLong()).isEqualTo(300)
    }
  }

  @Test
  fun `All conditions have to match by default`() = runLoaderTest { loader, sync, _ ->
    sync.insertBudget()

    loader.sankey(meta(conditions = [EMPLOYER, FOOD])).test {
      assertThat(awaitItem().nodeKeys()).isEmpty()
    }
  }

  @Test
  fun `Transfers are only shown when both sides match`() = runLoaderTest { loader, sync, _ ->
    sync.insertBudget()

    loader.sankey(meta(showTransfers = true)).test {
      assertThat(awaitItem().nodeKeys()).contains("savings")
    }
    loader.sankey(meta(conditions = [CHECKING], showTransfers = true)).test {
      assertThat(awaitItem().nodeKeys()).doesNotContain("savings")
    }
  }

  @Test
  fun `Saved filter references are ignored`() = runLoaderTest { loader, sync, _ ->
    sync.insertBudget()

    loader.sankey(meta(conditions = [FOOD.copy(customName = "Saved")])).test {
      assertThat(awaitItem().nodeKeys())
        .containsExactlyInAnyOrder("employer", "salary", "checking", "bills", "food", "fuel")
    }
  }

  private fun ChartData.nodeKeys() = (this as SankeyData).nodes.map { it.key }

  private suspend fun BudgetSyncController.insertBudget() {
    row(ACCOUNTS, "checking", "name" to "Checking", "offbudget" to false)
    row(ACCOUNTS, "savings", "name" to "Savings", "offbudget" to false)
    row(CATEGORY_GROUPS, "income", "name" to "Income", "is_income" to true, "sort_order" to 1L)
    row(CATEGORY_GROUPS, "bills", "name" to "Bills", "sort_order" to 2L)
    row(CATEGORIES, "salary", "name" to "Salary", "cat_group" to "income", "is_income" to true)
    row(CATEGORIES, "food", "name" to "Food", "cat_group" to "bills", "sort_order" to 1L)
    row(CATEGORIES, "fuel", "name" to "Fuel", "cat_group" to "bills", "sort_order" to 2L)
    row(CATEGORIES, "old", "name" to "Old", "cat_group" to "bills", "tombstone" to true)
    for (id in ["salary", "food", "fuel", "old"]) row(
      CATEGORY_MAPPING,
      id,
      "transferId" to id,
    )
    row(PAYEES, "employer", "name" to "Employer")
    row(PAYEES, "shop", "name" to "Shop")
    row(PAYEES, "gone", "name" to "Gone", "tombstone" to true)
    row(PAYEES, "to-savings", "transfer_acct" to "savings")
    row(PAYEES, "to-checking", "transfer_acct" to "checking")
    for (id in ["employer", "shop", "gone", "to-savings", "to-checking"]) {
      row(PAYEE_MAPPING, id, "targetId" to id)
    }

    transaction("pay", 1000, category = "salary", payee = "employer")
    transaction("food", -300, category = "food", payee = "shop")
    transaction("fuel", -200, category = "fuel", payee = "gone")
    transaction("old", -50, category = "old")
    transaction("out", -100, payee = "to-savings", transfer = "in")
    transaction("in", 100, account = "savings", payee = "to-checking", transfer = "out")
    transaction("next-month", -9, category = "food", date = 20260601)
  }

  private fun meta(
    conditions: List<Condition>? = null,
    op: ConditionOp? = null,
    showTransfers: Boolean = false,
  ) =
    SankeyReportMeta(
      name = "Sankey",
      conditions = conditions,
      conditionsOp = op,
      timeFrame = TimeFrame(MAY, MAY, Static),
      showTransfers = showTransfers,
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
    val EMPLOYER = Condition(field = Payee, operator = Is, value = JsonPrimitive("employer"))
    val CHECKING = Condition(field = Account, operator = Is, value = JsonPrimitive("checking"))
    val FOOD =
      Condition(
        field = Category,
        operator = OneOf,
        value = JsonArray([JsonPrimitive("food")]),
      )
  }
}
