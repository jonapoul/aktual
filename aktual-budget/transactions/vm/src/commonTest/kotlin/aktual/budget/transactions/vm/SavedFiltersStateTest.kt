package aktual.budget.transactions.vm

import aktual.budget.model.AccountSpec.AllAccounts
import aktual.budget.model.Condition
import aktual.budget.model.ConditionSet
import app.cash.turbine.test
import assertk.all
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import assertk.assertions.prop
import kotlin.test.Test
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive

internal class SavedFiltersStateTest : TransactionsViewModelTestBase() {
  @Test
  fun `Saved filters are saved, modified, updated and deleted`() = runTest {
    // given
    buildViewModel(AllAccounts)
    viewModel.addCondition(PAYEE)

    // when
    viewModel.saveFilter("Groceries")

    // then
    val state = viewModel.savedFiltersState
    state
      .dropWhile { it.applied == null }
      .test {
        val saved = awaitItem()
        assertThat(saved).all {
          prop(SavedFiltersState::applied).isEqualTo(saved.filters.single())
          prop(SavedFiltersState::isModified).isFalse()
        }
        assertThat(saved.applied?.conditions).isEqualTo(ConditionSet(listOf(PAYEE)))
      }

    // when
    viewModel.addCondition(NOTES)

    // then
    state.dropWhile { !it.isModified }.test { assertThat(awaitItem().isModified).isTrue() }

    // when
    viewModel.updateSavedFilter()

    // then
    state
      .dropWhile { it.isModified }
      .test {
        assertThat(awaitItem().applied?.conditions).isEqualTo(ConditionSet(listOf(PAYEE, NOTES)))
      }

    // when
    viewModel.deleteSavedFilter()

    // then
    state.dropWhile { it.applied != null }.test { assertThat(awaitItem().filters).isEmpty() }
  }

  @Test
  fun `A taken name is reported`() = runTest {
    // given
    buildViewModel(AllAccounts)
    viewModel.addCondition(PAYEE)
    viewModel.saveFilter("Groceries")
    viewModel.savedFiltersState.dropWhile { it.applied == null }.test { awaitItem() }
    viewModel.addCondition(NOTES)

    viewModel.events.test {
      // when
      viewModel.saveFilter("Groceries")

      // then
      assertThat(awaitItem()).isEqualTo(TransactionsEvent.FilterNameTaken("Groceries"))
    }
  }

  private companion object {
    val PAYEE = Condition(Payee, Is, JsonPrimitive("a"))
    val NOTES = Condition(Notes, Contains, JsonPrimitive("lunch"))
  }
}
