package aktual.budget.reports.vm

import aktual.budget.BudgetSyncController
import aktual.budget.db.BudgetDatabase
import aktual.budget.db.dao.ReportsDao
import aktual.budget.model.Condition
import aktual.budget.model.ConditionOp
import alakazam.test.TestCoroutineContexts
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonPrimitive

class AgeOfMoneyLoaderTest {
  @Test
  fun `Without account conditions, matches the SQL transactions`() = runLoaderTest { sync, dao ->
    sync.insertReportBudget()

    val sql = dao.observeAgeOfMoneyTransactions(END).first().map { it.amount }
    assertThat(filtered(dao, conditions = emptyList())).isEqualTo(sql)
    assertThat(sql).containsExactly(2000L, -400L, 1000L, -300L, -50L)
  }

  @Test
  fun `Other conditions leave on-budget transfers out`() = runLoaderTest { sync, dao ->
    sync.insertReportBudget()

    assertThat(filtered(dao, conditions = listOf(NOT_FOOD))).containsExactly(2000L, 1000L, -50L)
  }

  @Test
  fun `Transfers to an account outside the filter count as spending`() =
    runLoaderTest { sync, dao ->
      sync.insertReportBudget()

      assertThat(filtered(dao, conditions = listOf(CHECKING)))
        .containsExactly(2000L, -400L, 1000L, -300L, -100L, -50L)
    }

  @Test
  fun `Account conditions upstream can't invert keep transfers out`() = runLoaderTest { sync, dao ->
    sync.insertReportBudget()

    val matches = CHECKING.copy(operator = Matches, value = JsonPrimitive("Check"))
    assertThat(filtered(dao, conditions = listOf(matches)))
      .containsExactly(2000L, -400L, 1000L, -300L, -50L)
  }

  private suspend fun filtered(
    dao: ReportsDao,
    conditions: List<Condition>,
    op: ConditionOp = And,
  ): List<Long> {
    val rows = dao.observeFilteredTransactions(reportFilter(conditions, op), ALL_TIME_START, END)
    return ageOfMoneyTransactions(rows.first(), conditions, op).map { it.amount }
  }

  private fun runLoaderTest(
    action: suspend BudgetDatabase.(BudgetSyncController, ReportsDao) -> Unit,
  ) = runSyncedDatabaseTest { scope, sync ->
    val contexts = TestCoroutineContexts(StandardTestDispatcher(scope.testScheduler))
    action(sync, ReportsDao(this, contexts))
  }

  private companion object {
    val END = LocalDate(2026, 5, 31)
    val CHECKING = Condition(field = Account, operator = Is, value = JsonPrimitive("checking"))
    val NOT_FOOD = Condition(field = Category, operator = IsNot, value = JsonPrimitive("food"))
  }
}
