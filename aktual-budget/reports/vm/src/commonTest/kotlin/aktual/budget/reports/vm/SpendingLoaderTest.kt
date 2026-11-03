package aktual.budget.reports.vm

import aktual.budget.BudgetSyncController
import aktual.budget.db.BudgetDatabase
import aktual.budget.db.dao.DatabaseTables.ACCOUNTS
import aktual.budget.db.dao.DatabaseTables.CATEGORIES
import aktual.budget.db.dao.DatabaseTables.CATEGORY_GROUPS
import aktual.budget.db.dao.DatabaseTables.CATEGORY_MAPPING
import aktual.budget.db.dao.DatabaseTables.PREFERENCES
import aktual.budget.db.dao.DatabaseTables.REFLECT_BUDGETS
import aktual.budget.db.dao.DatabaseTables.ZERO_BUDGETS
import aktual.budget.model.Amount
import aktual.budget.model.Condition
import alakazam.test.TestCoroutineContexts
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive

class SpendingLoaderTest {
  @Test
  fun `Single month compares this month with last month`() = runLoaderTest { loader, sync ->
    sync.insertSpending()

    loader.spending(SpendingReportMeta(name = TITLE)).test {
      val data = awaitItem() as SpendingData
      assertThat(data.title).isEqualTo(TITLE)
      assertThat(data.mode).isEqualTo(Live)
      assertThat(data.targetMonth).isEqualTo(JUNE)
      assertThat(data.comparison).isEqualTo(SpendingComparison.SingleMonth(MAY))
      assertThat(data.day(1)).isEqualTo(400L to 0L)
      assertThat(data.day(2)).isEqualTo(400L to 1000L)
      assertThat(data.day(3)).isEqualTo(400L to 950L)
      assertThat(data.day(10)).isEqualTo(700L to 1450L)
      assertThat(data.day(15)).isEqualTo(700L to 1450L)
      // May's 15th less June's
      assertThat(data.difference).isEqualTo(Amount(-750))
    }
  }

  @Test
  fun `Leaves out income categories and off-budget accounts`() = runLoaderTest { loader, sync ->
    sync.insertSpending()

    loader.spending(SpendingReportMeta()).test {
      // The salary and off-budget spending on the 1st and 3rd of May aren't counted
      val data = awaitItem() as SpendingData
      assertThat(data.comparisonOn(1)).isEqualTo(0L)
      assertThat(data.comparisonOn(3)).isEqualTo(950L)
    }
  }

  @Test
  fun `Groups the 28th onwards into one day`() = runLoaderTest { loader, sync ->
    sync.insertSpending()

    loader.spending(SpendingReportMeta()).test {
      val data = awaitItem() as SpendingData
      assertThat(data.days.map { it.number })
        .containsExactly(
          *(1..27).map(SpendingDayNumber::Specific).toTypedArray(),
          SpendingDayNumber.End,
        )
      assertThat(data.comparisonOn(27)).isEqualTo(1450L)
      assertThat(data.comparisonOn(END)).isEqualTo(1750L)
    }
  }

  @Test
  fun `Leaves the target month empty after today`() = runLoaderTest { loader, sync ->
    sync.insertSpending()

    loader.spending(SpendingReportMeta()).test {
      val data = awaitItem() as SpendingData
      assertThat(data.targetOn(15)).isEqualTo(700L)
      assertThat(data.days[15].target).isNull()
      assertThat(data.days.last().target).isNull()
    }
  }

  @Test
  fun `Static months compare the saved months`() = runLoaderTest { loader, sync ->
    sync.insertSpending()

    val meta = SpendingReportMeta(compare = "2026-05", compareTo = "2026-03", isLive = false)
    loader.spending(meta).test {
      val data = awaitItem() as SpendingData
      assertThat(data.mode).isEqualTo(Static)
      assertThat(data.targetMonth).isEqualTo(MAY)
      assertThat(data.comparison).isEqualTo(SpendingComparison.SingleMonth(YearMonth(2026, 3)))
      assertThat(data.day(END)).isEqualTo(1750L to 300L)
      // Not this month, so compared on the last day
      assertThat(data.difference).isEqualTo(Amount(1450))
    }
  }

  @Test
  fun `Live months slide to this month`() = runLoaderTest { loader, sync ->
    sync.insertSpending()

    loader.spending(SpendingReportMeta(compare = "2026-03", compareTo = "2026-02")).test {
      val data = awaitItem() as SpendingData
      assertThat(data.targetMonth).isEqualTo(JUNE)
      assertThat(data.comparison).isEqualTo(SpendingComparison.SingleMonth(MAY))
    }
  }

  @Test
  fun `Budget mode spreads the month's budget over its days`() = runLoaderTest { loader, sync ->
    sync.insertSpending()
    sync.insertBudgets()

    loader.spending(SpendingReportMeta(mode = Budget, compare = "2026-01")).test {
      // Live, so the saved month is ignored
      val data = awaitItem() as SpendingData
      assertThat(data.targetMonth).isEqualTo(JUNE)
      assertThat(data.comparison).isEqualTo(Budgeted)
      assertThat(data.comparisonOn(1)).isEqualTo(150L)
      assertThat(data.comparisonOn(15)).isEqualTo(2250L)
      assertThat(data.comparisonOn(END)).isEqualTo(4500L)
      assertThat(data.difference).isEqualTo(Amount(-1550))
    }
  }

  @Test
  fun `Budget mode reads the tracking budget`() = runLoaderTest { loader, sync ->
    sync.insertSpending()
    sync.insertBudgets()
    sync.row(PREFERENCES, "budgetType", "value" to "tracking")

    loader.spending(SpendingReportMeta(mode = Budget)).test {
      assertThat((awaitItem() as SpendingData).comparisonOn(END)).isEqualTo(600L)
    }
  }

  @Test
  fun `Budget mode only counts the categories the filters keep`() = runLoaderTest { loader, sync ->
    sync.insertSpending()
    sync.insertBudgets()

    loader.spending(SpendingReportMeta(mode = Budget, conditions = listOf(FOOD))).test {
      val data = awaitItem() as SpendingData
      assertThat(data.day(15)).isEqualTo(400L to 1500L)
      assertThat(data.difference).isEqualTo(Amount(-1100))
    }
  }

  @Test
  fun `Average mode averages the last 3 months`() = runLoaderTest { loader, sync ->
    sync.insertSpending()

    loader.spending(SpendingReportMeta(mode = Average)).test {
      // March, April and May
      val data = awaitItem() as SpendingData
      assertThat(data.comparison).isEqualTo(Average)
      assertThat(data.comparisonOn(1)).isEqualTo(0L)
      assertThat(data.comparisonOn(5)).isEqualTo(417L)
      assertThat(data.comparisonOn(15)).isEqualTo(783L)
      assertThat(data.comparisonOn(END)).isEqualTo(883L)
      assertThat(data.difference).isEqualTo(Amount(-83))
    }
  }

  @Test
  fun `Average mode averages every month since the earliest transaction`() =
    runLoaderTest { loader, sync ->
      sync.insertSpending()
      sync.transaction("january", -1200, category = "food", date = 20260115)

      val meta = SpendingReportMeta(mode = Average, averageRange = AllTime)
      loader.spending(meta).test {
        // January to May, with nothing in February
        assertThat((awaitItem() as SpendingData).comparisonOn(END)).isEqualTo(770L)
      }
    }

  @Test
  fun `Keeps the transactions matching the filters`() = runLoaderTest { loader, sync ->
    sync.insertSpending()

    loader.spending(SpendingReportMeta(conditions = listOf(FOOD))).test {
      val data = awaitItem() as SpendingData
      assertThat(data.day(15)).isEqualTo(400L to 950L)
      assertThat(data.day(END)).isEqualTo(null to 1150L)
    }
  }

  @Test
  fun `Unknown mode compares single months`() = runLoaderTest { loader, sync ->
    sync.insertSpending()

    loader.spending(SpendingReportMeta(mode = Unknown)).test {
      val data = awaitItem() as SpendingData
      assertThat(data.comparison).isEqualTo(SpendingComparison.SingleMonth(MAY))
    }
  }

  private fun SpendingData.dayAt(day: Int) = days[if (day == END) days.lastIndex else day - 1]

  private fun SpendingData.targetOn(day: Int) = dayAt(day).target?.toLong()

  private fun SpendingData.comparisonOn(day: Int) = dayAt(day).comparison?.toLong()

  private fun SpendingData.day(day: Int) = targetOn(day) to comparisonOn(day)

  private suspend fun BudgetSyncController.insertSpending() {
    row(ACCOUNTS, "checking", "name" to "Checking", "offbudget" to false)
    row(ACCOUNTS, "invest", "name" to "Invest", "offbudget" to true)
    row(CATEGORY_GROUPS, "income", "name" to "Income", "is_income" to true)
    row(CATEGORY_GROUPS, "bills", "name" to "Bills")
    row(CATEGORIES, "salary", "name" to "Salary", "cat_group" to "income", "is_income" to true)
    row(CATEGORIES, "food", "name" to "Food", "cat_group" to "bills")
    row(CATEGORIES, "fuel", "name" to "Fuel", "cat_group" to "bills")
    for (id in listOf("salary", "food", "fuel")) row(CATEGORY_MAPPING, id, "transferId" to id)

    transaction("march", -300, category = "food", date = 20260305)
    transaction("april", -600, category = "food", date = 20260410)
    transaction("salary", 5000, category = "salary", date = 20260501)
    transaction("may-food", -1000, category = "food", date = 20260502)
    transaction("offbudget", -999, account = "invest", date = 20260503)
    transaction("refund", 50, category = "food", date = 20260503)
    transaction("may-fuel", -500, category = "fuel", date = 20260510)
    transaction("may-28th", -200, category = "food", date = 20260528)
    transaction("may-31st", -100, category = "fuel", date = 20260531)
    transaction("june-food", -400, category = "food", date = 20260601)
    transaction("june-fuel", -300, category = "fuel", date = 20260610)
    transaction("june-later", -700, category = "food", date = 20260620)
  }

  private suspend fun BudgetSyncController.insertBudgets() {
    budget(ZERO_BUDGETS, 202606, "food", 3000)
    budget(ZERO_BUDGETS, 202606, "fuel", 1500)
    budget(ZERO_BUDGETS, 202606, "salary", 9000)
    budget(ZERO_BUDGETS, 202605, "food", 7000)
    budget(REFLECT_BUDGETS, 202606, "food", 600)
  }

  private suspend fun BudgetSyncController.budget(
    dataset: String,
    month: Long,
    category: String,
    amount: Long,
  ) = row(dataset, "$month-$category", "month" to month, "category" to category, "amount" to amount)

  private fun runLoaderTest(
    action: suspend BudgetDatabase.(ChartDataLoader, BudgetSyncController) -> Unit,
  ) = runSyncedDatabaseTest { scope, sync ->
    val contexts = TestCoroutineContexts(StandardTestDispatcher(scope.testScheduler))
    action(chartDataLoader(contexts, calendar = { TODAY }), sync)
  }

  private companion object {
    const val TITLE = "Spending"
    const val END = 28
    val TODAY = LocalDate(2026, 6, 15)
    val JUNE = YearMonth(2026, 6)
    val MAY = YearMonth(2026, 5)
    val FOOD =
      Condition(
        field = Category,
        operator = OneOf,
        value = JsonArray(listOf(JsonPrimitive("food"))),
      )
  }
}
