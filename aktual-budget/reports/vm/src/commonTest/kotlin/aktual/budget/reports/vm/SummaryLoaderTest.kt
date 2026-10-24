package aktual.budget.reports.vm

import aktual.budget.BudgetSyncController
import aktual.budget.db.BudgetDatabase
import aktual.budget.db.dao.DatabaseTables.ACCOUNTS
import aktual.budget.db.dao.DatabaseTables.CATEGORIES
import aktual.budget.db.dao.DatabaseTables.CATEGORY_GROUPS
import aktual.budget.db.dao.DatabaseTables.CATEGORY_MAPPING
import aktual.budget.db.dao.DatabaseTables.PAYEES
import aktual.budget.db.dao.DatabaseTables.PAYEE_MAPPING
import aktual.budget.model.Amount
import aktual.budget.model.Condition
import aktual.core.model.percent
import aktual.test.assertThatNextEmissionIsEqualTo
import alakazam.test.TestCoroutineContexts
import app.cash.turbine.test
import kotlin.test.Test
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive

class SummaryLoaderTest {
  @Test
  fun `Missing content sums the transactions`() = runLoaderTest { loader, sync ->
    sync.insertSpending()

    loader.summary(meta(content = null)).test {
      assertThatNextEmissionIsEqualTo(SummaryData.Sum(TITLE, MAY_1, MAY_31, Amount(-4600)))
    }
  }

  @Test
  fun `Sum keeps the transactions matching the filters`() = runLoaderTest { loader, sync ->
    sync.insertSpending()

    loader.summary(meta(content = """{"type":"sum"}""", conditions = listOf(FOOD))).test {
      assertThatNextEmissionIsEqualTo(SummaryData.Sum(TITLE, MAY_1, MAY_31, Amount(-1900)))
    }
  }

  @Test
  fun `Average per month`() = runLoaderTest { loader, sync ->
    sync.insertSpending()

    loader.summary(meta("""{"type":"avgPerMonth"}""", start = YearMonth(2026, 4))).test {
      assertThatNextEmissionIsEqualTo(
        SummaryData.AveragePerMonth(
          title = TITLE,
          start = LocalDate(2026, 4, 1),
          end = MAY_31,
          numMonths = 2f,
          total = Amount(-4600),
          average = Amount(-2300),
        ),
      )
    }
  }

  @Test
  fun `Average per year`() = runLoaderTest { loader, sync ->
    sync.insertSpending()

    loader.summary(meta("""{"type":"avgPerYear"}""", start = YearMonth(2025, 12))).test {
      assertThatNextEmissionIsEqualTo(
        SummaryData.AveragePerYear(
          title = TITLE,
          start = LocalDate(2025, 12, 1),
          end = MAY_31,
          numYears = 0.5f,
          total = Amount(-4600),
          average = Amount(-9200),
        ),
      )
    }
  }

  @Test
  fun `Average per transaction`() = runLoaderTest { loader, sync ->
    sync.insertSpending()

    loader.summary(meta("""{"type":"avgPerTransact"}""")).test {
      assertThatNextEmissionIsEqualTo(
        SummaryData.AveragePerTransaction(
          title = TITLE,
          start = MAY_1,
          end = MAY_31,
          numTransactions = 6,
          total = Amount(-4600),
          average = Amount(-767),
        ),
      )
    }
  }

  @Test
  fun `Percentage of the transactions in the range`() = runLoaderTest { loader, sync ->
    sync.insertSpending()
    val content = """{"type":"percentage","divisorConditions":[],"divisorAllTimeDateRange":false}"""

    loader.summary(meta(content, conditions = listOf(FOOD))).test {
      assertThatNextEmissionIsEqualTo(
        SummaryData.Percentage(
          title = TITLE,
          start = MAY_1,
          end = MAY_31,
          numerator = Amount(-1900),
          denominator = Amount(-4600),
          percent = 41.3.percent,
          divisor = PercentageDivisor.Specific(MAY_1, MAY_31),
        ),
      )
    }
  }

  @Test
  fun `Percentage of all transactions`() = runLoaderTest { loader, sync ->
    sync.insertSpending()
    val content = """{"type":"percentage","divisorAllTimeDateRange":true}"""

    loader.summary(meta(content, conditions = listOf(FOOD))).test {
      assertThatNextEmissionIsEqualTo(
        SummaryData.Percentage(
          title = TITLE,
          start = MAY_1,
          end = MAY_31,
          numerator = Amount(-1900),
          denominator = Amount(-4609),
          percent = 41.22.percent,
          divisor = AllTime,
        ),
      )
    }
  }

  @Test
  fun `Percentage of nothing is zero`() = runLoaderTest { loader, _ ->
    loader.summary(meta("""{"type":"percentage"}""")).test {
      assertThatNextEmissionIsEqualTo(
        SummaryData.Percentage(
          title = TITLE,
          start = MAY_1,
          end = MAY_31,
          numerator = Zero,
          denominator = Zero,
          percent = 0.percent,
          divisor = PercentageDivisor.Specific(MAY_1, MAY_31),
        ),
      )
    }
  }

  private suspend fun BudgetSyncController.insertSpending() {
    row(ACCOUNTS, "checking", "name" to "Checking", "offbudget" to false)
    row(ACCOUNTS, "investing", "name" to "Investing", "offbudget" to true)
    row(CATEGORY_GROUPS, "spending", "name" to "Spending", "sort_order" to 1L)
    for ((id, name) in listOf("food" to "Food", "fuel" to "Fuel")) {
      row(CATEGORIES, id, "name" to name, "cat_group" to "spending", "sort_order" to 1L)
      row(CATEGORY_MAPPING, id, "transferId" to id)
    }
    row(PAYEES, "shop", "name" to "Shop")
    row(PAYEES, "to-investing", "transfer_acct" to "investing")
    for (id in listOf("shop", "to-investing")) row(PAYEE_MAPPING, id, "targetId" to id)

    transaction("plain", -1000, category = "food", payee = "shop")
    transaction("split", -3500, isParent = true)
    transaction("split-food", -1500, category = "food", parent = "split")
    transaction("split-fuel", -2000, category = "fuel", parent = "split")
    transaction("deleted", -5000, category = "food", isDeleted = true)
    transaction("transfer", -700, payee = "to-investing", transfer = "transfer-in")
    transaction("off-budget", -300, category = "food", account = "investing")
    transaction("income", 900, category = "food")
    transaction("next-month", -9, category = "food", date = 20260601)
  }

  private fun meta(
    content: String?,
    start: YearMonth = MAY,
    conditions: List<Condition>? = null,
  ) =
    SummaryReportMeta(
      name = TITLE,
      conditions = conditions,
      timeFrame = TimeFrame(start, MAY, Static),
      content = content,
    )

  private fun runLoaderTest(
    action: suspend BudgetDatabase.(ChartDataLoader, BudgetSyncController) -> Unit,
  ) = runSyncedDatabaseTest { scope, sync ->
    val contexts = TestCoroutineContexts(StandardTestDispatcher(scope.testScheduler))
    val loader = chartDataLoader(contexts, calendar = { TODAY })
    action(loader, sync)
  }

  private companion object {
    const val TITLE = "Summary"
    val TODAY = LocalDate(2026, 6, 15)
    val MAY = YearMonth(2026, 5)
    val MAY_1 = LocalDate(2026, 5, 1)
    val MAY_31 = LocalDate(2026, 5, 31)
    val FOOD =
      Condition(
        field = Category,
        operator = OneOf,
        value = JsonArray(listOf(JsonPrimitive("food"))),
      )
  }
}
