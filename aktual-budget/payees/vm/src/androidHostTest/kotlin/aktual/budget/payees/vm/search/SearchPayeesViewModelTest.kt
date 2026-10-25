package aktual.budget.payees.vm.search

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.dao.PayeeDao
import aktual.budget.db.dao.RuleContextDao
import aktual.budget.db.dao.RulesDao
import aktual.budget.model.PayeeId
import aktual.budget.payees.vm.PayeesLoader
import aktual.budget.payees.vm.list.PayeeItem
import aktual.budget.payees.vm.search.SearchPayeesState.Results
import aktual.budget.rules.domain.PayeeRuleCounter
import aktual.test.assertThatNextEmissionIsEqualTo
import aktual.test.runDatabaseTest
import aktual.test.viewModelContext
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.extracting
import assertk.assertions.isEqualTo
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SearchPayeesViewModelTest {
  @Test
  fun `Blank query prompts for input`() = runDatabaseTest { scope ->
    PayeeDao(this).insert(PayeeId("argos"), "Argos")
    val viewModel = createViewModel(scope)

    viewModel.state.test {
      assertThatNextEmissionIsEqualTo(NoQuery)
      viewModel.setQuery("   ")
      scope.advanceUntilIdle()
      expectNoEvents()
    }
  }

  @Test
  fun `Matches names ignoring case and accents`() = runDatabaseTest { scope ->
    val payees = PayeeDao(this)
    payees.insert(PayeeId("cafe"), "Café Nero")
    payees.insert(PayeeId("cafes"), "CAFES R US")
    payees.insert(PayeeId("tesco"), "Tesco")
    val viewModel = createViewModel(scope)

    viewModel.setQuery(" cafe ")

    viewModel.state.test {
      assertThat(awaitResults())
        .extracting(PayeeItem::name)
        .containsExactly("CAFES R US", "Café Nero")
    }
  }

  @Test
  fun `No matches shows no results`() = runDatabaseTest { scope ->
    PayeeDao(this).insert(PayeeId("argos"), "Argos")
    val viewModel = createViewModel(scope)

    viewModel.setQuery("xyz")

    viewModel.state.test {
      var state = awaitItem()
      while (state == NoQuery || state == Loading) state = awaitItem()
      assertThat(state).isEqualTo(NoResults)
    }
  }

  @Test
  fun `Updates results when a payee is added`() = runDatabaseTest { scope ->
    val payees = PayeeDao(this)
    payees.insert(PayeeId("argos"), "Argos")
    val viewModel = createViewModel(scope)
    viewModel.setQuery("ar")

    viewModel.state.test {
      assertThat(awaitResults()).extracting(PayeeItem::name).containsExactly("Argos")

      payees.insert(PayeeId("arcade"), "Arcade")

      val state = awaitItem() as Results
      assertThat(state.payees).extracting(PayeeItem::name).containsExactly("Arcade", "Argos")
    }
  }

  private suspend fun ReceiveTurbine<SearchPayeesState>.awaitResults(): List<PayeeItem> {
    var state = awaitItem()
    while (state !is Results) state = awaitItem()
    return state.payees
  }

  private fun BudgetDatabase.createViewModel(scope: TestScope): SearchPayeesViewModel {
    return SearchPayeesViewModel(
      savedState = SavedStateHandle(),
      loader = PayeesLoader(PayeeDao(this), PayeeRuleCounter(RulesDao(this), RuleContextDao(this))),
      viewModelContext = scope.viewModelContext,
    )
  }
}
