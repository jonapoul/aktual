package aktual.budget.reports.vm

import aktual.budget.BudgetSyncController
import aktual.budget.db.BudgetDatabase
import aktual.budget.db.CustomReports
import aktual.budget.db.dao.CustomReportsDao
import aktual.budget.db.dao.DatabaseTables.ACCOUNTS
import aktual.budget.db.dao.DatabaseTables.CATEGORIES
import aktual.budget.db.dao.DatabaseTables.CATEGORY_GROUPS
import aktual.budget.db.dao.DatabaseTables.CATEGORY_MAPPING
import aktual.budget.db.dao.DatabaseTables.CUSTOM_REPORTS
import aktual.budget.db.dao.DatabaseTables.PAYEES
import aktual.budget.db.dao.DatabaseTables.PAYEE_MAPPING
import aktual.budget.db.dao.DatabaseTables.PREFERENCES
import aktual.budget.db.dao.DatabaseTables.REFLECT_BUDGETS
import aktual.budget.db.dao.DatabaseTables.TRANSACTIONS
import aktual.budget.db.dao.DatabaseTables.ZERO_BUDGETS
import aktual.budget.db.dao.PreferencesDao
import aktual.budget.db.dao.ReportsDao
import aktual.budget.db.withoutResult
import aktual.budget.model.Amount
import aktual.budget.model.CategoryId
import aktual.budget.model.Condition
import aktual.budget.model.CustomReportId
import aktual.budget.model.Field
import aktual.budget.model.Operator
import aktual.budget.model.ReportDate
import aktual.budget.model.localChange
import aktual.budget.model.tombstone
import aktual.test.assertThatNextEmission
import aktual.test.assertThatNextEmissionIsEqualTo
import alakazam.test.TestCoroutineContexts
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

class CustomReportLoaderTest {
  @Test
  fun `Missing report`() = runLoaderTest { loader, _ ->
    loader.custom(CustomReportMeta(ID)).test {
      assertThatNextEmissionIsEqualTo(MISSING)
    }
  }

  @Test
  fun `Deleted report`() = runLoaderTest { loader, _ ->
    insert(emptyCustomReport(ID).copy(name = "Deleted", tombstone = true))

    loader.custom(CustomReportMeta(ID)).test {
      assertThatNextEmissionIsEqualTo(MISSING)
    }
  }

  @Test
  fun `Unreadable report`() = runLoaderTest { loader, _ ->
    insert(emptyCustomReport(ID).copy(name = "Unreadable", graph_type = Unknown))

    loader.custom(CustomReportMeta(ID)).test {
      assertThatNextEmissionIsEqualTo(UnsupportedData(InvalidMeta, Custom, "Unreadable"))
    }
  }

  @Test
  fun `Budgeted report can't be grouped by payee`() = runLoaderTest { loader, _ ->
    insert(budgetedReport().copy(group_by = Payee))

    loader.custom(CustomReportMeta(ID)).test {
      assertThatNextEmissionIsEqualTo(UnsupportedData(CustomBudgeted, Custom, "Budget"))
    }
  }

  @Test
  fun `Budgeted report can't be weekly`() = runLoaderTest { loader, _ ->
    insert(budgetedReport().copy(interval = Weekly))

    loader.custom(CustomReportMeta(ID)).test {
      assertThatNextEmissionIsEqualTo(UnsupportedData(CustomBudgeted, Custom, "Budget"))
    }
  }

  @Test
  fun `Budgeted report reads the envelope budget`() = runLoaderTest { loader, sync ->
    sync.insertSpending()
    sync.insertBudgets()
    insert(budgetedReport())

    loader.custom(CustomReportMeta(ID)).test {
      val data = awaitItem() as CustomData
      assertThat(data.groups.map { it.group to it.totals.total })
        .containsExactly(
          CustomGroup.Category(CategoryId("food"), "Food") to Amount(5000),
          CustomGroup.Category(CategoryId("fuel"), "Fuel") to Amount(-3000),
        )
      assertThat(data.totals.assets).isEqualTo(Amount(5000))
      assertThat(data.totals.debts).isEqualTo(Amount(-3000))
    }
  }

  @Test
  fun `Budgeted report reads the tracking budget`() = runLoaderTest { loader, sync ->
    sync.insertSpending()
    sync.insertBudgets()
    sync.row(PREFERENCES, "budgetType", "value" to "tracking")
    insert(budgetedReport())

    loader.custom(CustomReportMeta(ID)).test {
      val data = awaitItem() as CustomData
      assertThat(data.groups.map { it.group to it.totals.total })
        .containsExactly(CustomGroup.Category(CategoryId("food"), "Food") to Amount(1111))
    }
  }

  @Test
  fun `Budgeted report sums yearly`() = runLoaderTest { loader, sync ->
    sync.insertSpending()
    sync.insertBudgets()
    insert(budgetedReport().copy(interval = Yearly))

    loader.custom(CustomReportMeta(ID)).test {
      val data = awaitItem() as CustomData
      assertThat(data.intervals.map { it.interval.start to it.totals.total })
        .containsExactly(LocalDate(2026, 1, 1) to Amount(2000))
    }
  }

  @Test
  fun `Budgeted report only filters by category`() = runLoaderTest { loader, sync ->
    sync.insertSpending()
    sync.insertBudgets()
    val conditions =
      listOf(
        condition(Category, Is, JsonPrimitive("fuel")),
        condition(Payee, Is, JsonPrimitive("shop")),
      )
    insert(budgetedReport().copy(conditions = conditions, conditions_op = And))

    loader.custom(CustomReportMeta(ID)).test {
      val data = awaitItem() as CustomData
      assertThat(data.groups.map { it.group to it.totals.total })
        .containsExactly(CustomGroup.Category(CategoryId("fuel"), "Fuel") to Amount(-3000))
    }
  }

  @Test
  fun `Live report without transactions covers today`() = runLoaderTest { loader, _ ->
    val report = emptyCustomReport(ID).copy(name = "Spending", date_range = AllTime)
    insert(report)

    loader.custom(CustomReportMeta(ID)).test {
      assertThatNextEmissionIsEqualTo(
        CustomData(
          title = "Spending",
          config = checkNotNull(report.toConfig()),
          range = TODAY..TODAY,
          intervals =
            persistentListOf(
              CustomIntervalTotals(
                interval = CustomInterval(LocalDate(2026, 5, 1), TODAY..TODAY),
                totals = Zero,
                stacked =
                  persistentMapOf(
                    CustomGroup.Uncategorized to Amount.Zero,
                    CustomGroup.OffBudget to Amount.Zero,
                    CustomGroup.Transfers to Amount.Zero,
                  ),
              ),
            ),
          intervalCount = 1,
          groups = persistentListOf(),
          categoryGroups = persistentListOf(),
          totals = Zero,
          trends = persistentMapOf(),
        ),
      )
    }
  }

  @Test
  fun `Synced changes to the report are picked up`() = runLoaderTest { loader, sync ->
    insert(emptyCustomReport(ID).copy(name = "Before"))

    loader.custom(CustomReportMeta(ID)).test {
      assertThatNextEmission().transform { (it as CustomData).title }.isEqualTo("Before")

      sync.syncChanges(localChange(CUSTOM_REPORTS, ID.value, "name", "After"))
      assertThatNextEmission().transform { (it as CustomData).title }.isEqualTo("After")

      sync.syncChanges(tombstone(CUSTOM_REPORTS, ID.value))
      assertThatNextEmissionIsEqualTo(MISSING)
    }
  }

  @Test
  fun `Amounts are read from the database`() = runLoaderTest { loader, sync ->
    sync.insertSpending()
    insert(spendingReport())

    loader.custom(CustomReportMeta(ID)).test {
      val data = awaitItem() as CustomData
      assertThat(data.groups.map { it.group to it.totals.debts })
        .containsExactly(
          CustomGroup.Category(CategoryId("food"), "Food") to Amount(-2500),
          CustomGroup.Category(CategoryId("fuel"), "Fuel") to Amount(-2000),
          CustomGroup.Transfers to Amount(-700),
          CustomGroup.OffBudget to Amount(-300),
        )
      assertThat(data.totals.assets).isEqualTo(Amount(900))
    }
  }

  @Test
  fun `Filters keep the transactions matching all conditions`() = runLoaderTest { loader, sync ->
    sync.insertSpending()
    val conditions =
      listOf(
        condition(Category, OneOf, JsonArray(listOf(JsonPrimitive("food")))),
        condition(Field.Amount, LessThan, JsonPrimitive(-500)),
      )
    insert(spendingReport().copy(conditions = conditions, conditions_op = And))

    loader.custom(CustomReportMeta(ID)).test {
      val data = awaitItem() as CustomData
      assertThat(data.groups.map { it.group to it.totals.debts })
        .containsExactly(CustomGroup.Category(CategoryId("food"), "Food") to Amount(-2500))
      assertThat(data.totals.assets).isEqualTo(Zero)
    }
  }

  @Test
  fun `Filters keep the transactions matching any condition`() = runLoaderTest { loader, sync ->
    sync.insertSpending()
    val conditions =
      listOf(
        condition(Payee, Contains, JsonPrimitive("sho")),
        condition(Transfer, Is, JsonPrimitive(true)),
      )
    insert(spendingReport().copy(conditions = conditions, conditions_op = Or))

    loader.custom(CustomReportMeta(ID)).test {
      val data = awaitItem() as CustomData
      assertThat(data.groups.map { it.group to it.totals.debts })
        .containsExactly(
          CustomGroup.Category(CategoryId("food"), "Food") to Amount(-1000),
          CustomGroup.Transfers to Amount(-700),
        )
    }
  }

  @Test
  fun `Saved filter references are ignored`() = runLoaderTest { loader, sync ->
    sync.insertSpending()
    val saved = condition(Notes, Is, JsonPrimitive("x")).copy(customName = "Saved")
    insert(spendingReport().copy(conditions = listOf(saved)))

    loader.custom(CustomReportMeta(ID)).test {
      val data = awaitItem() as CustomData
      assertThat(data.groups.map { it.group to it.totals.debts })
        .containsExactly(
          CustomGroup.Category(CategoryId("food"), "Food") to Amount(-2500),
          CustomGroup.Category(CategoryId("fuel"), "Fuel") to Amount(-2000),
          CustomGroup.Transfers to Amount(-700),
          CustomGroup.OffBudget to Amount(-300),
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

  // A negative budget is unusual, but upstream counts it as debts
  private suspend fun BudgetSyncController.insertBudgets() {
    row(CATEGORY_GROUPS, "income", "name" to "Income", "is_income" to true, "sort_order" to 2L)
    row(CATEGORIES, "salary", "name" to "Salary", "cat_group" to "income", "is_income" to true)
    row(CATEGORY_MAPPING, "salary", "transferId" to "salary")
    budget(ZERO_BUDGETS, 202605, "food", 5000)
    budget(ZERO_BUDGETS, 202605, "fuel", -3000)
    budget(ZERO_BUDGETS, 202605, "salary", 9000)
    budget(ZERO_BUDGETS, 202606, "food", 7000)
    budget(REFLECT_BUDGETS, 202605, "food", 1111)
  }

  private suspend fun BudgetSyncController.budget(
    dataset: String,
    month: Long,
    category: String,
    amount: Long,
  ) = row(dataset, "$month-$category", "month" to month, "category" to category, "amount" to amount)

  private fun budgetedReport() =
    spendingReport().copy(name = "Budget", balance_type = Budgeted, show_uncategorized = false)

  private fun spendingReport() =
    emptyCustomReport(ID)
      .copy(
        name = "Spending",
        date_static = true,
        start_date = ReportDate.Date(LocalDate(2026, 5, 1)),
        end_date = ReportDate.Date(LocalDate(2026, 5, 31)),
        show_offbudget = true,
        show_uncategorized = true,
        show_hidden = true,
      )

  private fun condition(field: Field, operator: Operator, value: JsonElement) =
    Condition(field = field, operator = operator, value = value)

  private suspend fun BudgetSyncController.transaction(
    id: String,
    amount: Long,
    category: String? = null,
    payee: String? = null,
    account: String = "checking",
    date: Long = 20260510,
    isParent: Boolean = false,
    parent: String? = null,
    transfer: String? = null,
    isDeleted: Boolean = false,
  ) =
    row(
      TRANSACTIONS,
      id,
      "acct" to account,
      "amount" to amount,
      "date" to date,
      "category" to category,
      "description" to payee,
      "isParent" to isParent,
      "isChild" to (parent != null),
      "parent_id" to parent,
      "transferred_id" to transfer,
      "tombstone" to isDeleted,
    )

  private suspend fun BudgetSyncController.row(
    dataset: String,
    id: String,
    vararg values: Pair<String, Any?>,
  ) =
    syncChanges(
      values.map { (column, value) ->
        when (value) {
          is Long -> localChange(dataset, id, column, value)
          is Boolean -> localChange(dataset, id, column, value)
          else -> localChange(dataset, id, column, value?.toString())
        }
      },
    )

  private suspend fun BudgetDatabase.insert(report: CustomReports) =
    customReportsQueries.withoutResult {
      insert(report)
    }

  private fun runLoaderTest(
    action: suspend BudgetDatabase.(ChartDataLoader, BudgetSyncController) -> Unit,
  ) = runSyncedDatabaseTest { scope, sync ->
    val contexts = TestCoroutineContexts(StandardTestDispatcher(scope.testScheduler))
    val loader =
      ChartDataLoader(
        ReportsDao(this, contexts),
        CustomReportsDao(this, contexts),
        PreferencesDao(this, contexts),
        calendar = { TODAY },
        contexts = contexts,
      )
    action(loader, sync)
  }

  private companion object {
    val ID = CustomReportId("report-1")
    val TODAY = LocalDate(2026, 5, 15)
    val MISSING = UnsupportedData(MissingReport, Custom, name = null)
  }
}
