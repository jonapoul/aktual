package aktual.budget.payees.vm.list

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.Rules
import aktual.budget.db.dao.PayeeDao
import aktual.budget.db.dao.RuleContextDao
import aktual.budget.db.dao.RulesDao
import aktual.budget.db.dao.TransactionDao
import aktual.budget.db.withoutResult
import aktual.budget.model.AccountId
import aktual.budget.model.Condition
import aktual.budget.model.PayeeId
import aktual.budget.model.RuleId
import aktual.budget.rules.domain.PayeeRuleCounter
import aktual.test.assertThatNextEmissionIsEqualTo
import aktual.test.runDatabaseTest
import app.cash.turbine.test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ListPayeesViewModelTest {
  @Test
  fun `Loads payees with their transaction and rule counts`() = runDatabaseTest {
    // given
    insertAccount(ACCOUNT, name = "Current")
    val payees = PayeeDao(this)
    payees.insert(PayeeId("argos"), "Argos")
    payees.insert(PayeeId("tesco"), "Tesco")
    payees.insert(PayeeId("transfer"), null, transferAccount = AccountId(ACCOUNT))
    val transactions = TransactionDao(this)
    transactions.insert("t1", ACCOUNT, null, "tesco", DATE)
    transactions.insert("t2", ACCOUNT, null, "tesco", DATE)
    insertPayeeRule("r1", payee = "tesco")
    insertPayeeRule("r2", payee = "transfer")

    // when
    val viewModel = createViewModel()

    // then
    viewModel.state.test {
      assertThatNextEmissionIsEqualTo(
        Success(
          persistentListOf(
            PayeeItem(
              id = PayeeId("argos"),
              name = "Argos",
              isTransfer = false,
              isFavorite = false,
              numTransactions = 0,
              numRules = 0,
            ),
            PayeeItem(
              id = PayeeId("transfer"),
              name = "Current",
              isTransfer = true,
              isFavorite = false,
              numTransactions = 0,
              numRules = 1,
            ),
            PayeeItem(
              id = PayeeId("tesco"),
              name = "Tesco",
              isTransfer = false,
              isFavorite = false,
              numTransactions = 2,
              numRules = 1,
            ),
          ),
        ),
      )
    }
  }

  @Test
  fun `No payees gives the empty state`() = runDatabaseTest {
    createViewModel().state.test { assertThatNextEmissionIsEqualTo(Empty) }
  }

  @Test
  fun `Reload picks up new payees`() = runDatabaseTest {
    // given
    val viewModel = createViewModel()
    viewModel.state.test {
      assertThatNextEmissionIsEqualTo(Empty)

      // when
      PayeeDao(this@runDatabaseTest).insert(PayeeId("argos"), "Argos")
      viewModel.reload(showLoading = false)

      // then
      assertThatNextEmissionIsEqualTo(
        Success(
          persistentListOf(
            PayeeItem(
              id = PayeeId("argos"),
              name = "Argos",
              isTransfer = false,
              isFavorite = false,
              numTransactions = 0,
              numRules = 0,
            ),
          ),
        ),
      )
    }
  }

  private fun BudgetDatabase.createViewModel() =
    ListPayeesViewModel(
      payeeDao = PayeeDao(this),
      ruleCounter = PayeeRuleCounter(RulesDao(this), RuleContextDao(this)),
    )

  private suspend fun BudgetDatabase.insertAccount(id: String, name: String) =
    accountsQueries.withoutResult {
      insert(
        id = AccountId(id),
        account_id = null,
        name = name,
        official_name = null,
        bank = null,
        offbudget = false,
        account_sync_source = null,
      )
    }

  private suspend fun BudgetDatabase.insertPayeeRule(id: String, payee: String) =
    RulesDao(this)
      .insert(
        Rules(
          id = RuleId(id),
          stage = null,
          conditions =
            listOf(Condition(field = Payee, operator = Is, value = JsonPrimitive(payee))),
          actions = emptyList(),
          tombstone = false,
          conditions_op = And,
        ),
      )

  private companion object {
    const val ACCOUNT = "account"
    val DATE = LocalDate(2026, 10, 1)
  }
}
