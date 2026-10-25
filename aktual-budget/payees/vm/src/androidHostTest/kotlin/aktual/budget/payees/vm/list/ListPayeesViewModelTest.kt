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
import aktual.budget.payees.vm.PayeesLoader
import aktual.budget.rules.domain.PayeeRuleCounter
import aktual.test.assertThatNextEmissionIsEqualTo
import aktual.test.runDatabaseTest
import alakazam.test.standardDispatcher
import app.cash.turbine.test
import kotlin.test.AfterTest
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ListPayeesViewModelTest {
  @AfterTest
  fun after() {
    Dispatchers.resetMain()
  }

  @Test
  fun `Loads payees with their transaction and rule counts`() = runDatabaseTest { scope ->
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
    val viewModel = createViewModel(scope)

    // then
    viewModel.state.test {
      assertThatNextEmissionIsEqualTo(Loading)
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
  fun `No payees gives the empty state`() = runDatabaseTest { scope ->
    createViewModel(scope).state.test {
      assertThatNextEmissionIsEqualTo(Loading)
      assertThatNextEmissionIsEqualTo(Empty)
    }
  }

  @Test
  fun `Updates when a payee is added`() = runDatabaseTest { scope ->
    // given
    val viewModel = createViewModel(scope)
    viewModel.state.test {
      assertThatNextEmissionIsEqualTo(Loading)
      assertThatNextEmissionIsEqualTo(Empty)

      // when
      PayeeDao(this@runDatabaseTest).insert(PayeeId("argos"), "Argos")

      // then
      assertThatNextEmissionIsEqualTo(Success(persistentListOf(ARGOS)))
    }
  }

  @Test
  fun `Updates rule counts when a rule is added`() = runDatabaseTest { scope ->
    // given
    PayeeDao(this).insert(PayeeId("argos"), "Argos")
    val viewModel = createViewModel(scope)
    viewModel.state.test {
      assertThatNextEmissionIsEqualTo(Loading)
      assertThatNextEmissionIsEqualTo(Success(persistentListOf(ARGOS)))

      // when
      insertPayeeRule("r1", payee = "argos")

      // then
      assertThatNextEmissionIsEqualTo(Success(persistentListOf(ARGOS.copy(numRules = 1))))
    }
  }

  private fun BudgetDatabase.createViewModel(scope: TestScope): ListPayeesViewModel {
    Dispatchers.setMain(scope.standardDispatcher)
    return ListPayeesViewModel(
      loader = PayeesLoader(PayeeDao(this), PayeeRuleCounter(RulesDao(this), RuleContextDao(this))),
    )
  }

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

    val ARGOS =
      PayeeItem(
        id = PayeeId("argos"),
        name = "Argos",
        isTransfer = false,
        isFavorite = false,
        numTransactions = 0,
        numRules = 0,
      )
  }
}
