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
import aktual.budget.db.dao.DatabaseTables.TRANSACTIONS
import aktual.budget.db.dao.PreferencesDao
import aktual.budget.db.dao.ReportsDao
import aktual.budget.db.withoutResult
import aktual.budget.model.Amount
import aktual.budget.model.CategoryId
import aktual.budget.model.CustomReportId
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
  fun `Budgeted report`() = runLoaderTest { loader, _ ->
    insert(emptyCustomReport(ID).copy(name = "Budget", balance_type = Budgeted))

    loader.custom(CustomReportMeta(ID)).test {
      assertThatNextEmissionIsEqualTo(UnsupportedData(CustomBudgeted, Custom, "Budget"))
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
          groups = persistentListOf(),
          categoryGroups = persistentListOf(),
          totals = Zero,
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
    sync.row(ACCOUNTS, "checking", "name" to "Checking", "offbudget" to false)
    sync.row(ACCOUNTS, "investing", "name" to "Investing", "offbudget" to true)
    sync.row(CATEGORY_GROUPS, "spending", "name" to "Spending", "sort_order" to 1L)
    for ((id, name) in listOf("food" to "Food", "fuel" to "Fuel")) {
      sync.row(CATEGORIES, id, "name" to name, "cat_group" to "spending", "sort_order" to 1L)
      sync.row(CATEGORY_MAPPING, id, "transferId" to id)
    }
    sync.row(PAYEES, "shop", "name" to "Shop")
    sync.row(PAYEES, "to-investing", "transfer_acct" to "investing")
    for (id in listOf("shop", "to-investing")) sync.row(PAYEE_MAPPING, id, "targetId" to id)

    sync.transaction("plain", -1000, category = "food", payee = "shop")
    sync.transaction("split", -3500, isParent = true)
    sync.transaction("split-food", -1500, category = "food", parent = "split")
    sync.transaction("split-fuel", -2000, category = "fuel", parent = "split")
    sync.transaction("deleted", -5000, category = "food", isDeleted = true)
    sync.transaction("transfer", -700, payee = "to-investing")
    sync.transaction("off-budget", -300, category = "food", account = "investing")
    sync.transaction("income", 900, category = "food")
    sync.transaction("next-month", -9, category = "food", date = 20260601)

    val report =
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
    insert(report)

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

  private suspend fun BudgetSyncController.transaction(
    id: String,
    amount: Long,
    category: String? = null,
    payee: String? = null,
    account: String = "checking",
    date: Long = 20260510,
    isParent: Boolean = false,
    parent: String? = null,
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
