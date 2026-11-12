package aktual.budget.payees.vm.locations

import aktual.budget.db.BudgetDatabase
import aktual.budget.model.PayeeLocationId
import aktual.budget.payees.vm.locations.SearchPayeeLocationsState.Results
import aktual.test.TestSyncController
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

class SearchPayeeLocationsViewModelTest {
  @Test
  fun `Blank query prompts for input`() = runDatabaseTest { scope ->
    val viewModel = createViewModel(scope)

    viewModel.state.test {
      assertThatNextEmissionIsEqualTo(NoQuery)
      viewModel.setQuery("   ")
      scope.advanceUntilIdle()
      expectNoEvents()
    }
  }

  @Test
  fun `Matches payee names ignoring case and accents`() = runDatabaseTest { scope ->
    // given
    insertPayee("cafe", "Café Nero")
    insertPayee("tesco", "Tesco")
    insertLocation("cafe", "cafe")
    insertLocation("tesco", "tesco")
    val viewModel = createViewModel(scope)

    // when
    viewModel.setQuery(" CAFE ")

    // then
    viewModel.state.test {
      assertThat(awaitResults())
        .extracting(PayeeLocationItem::id)
        .containsExactly(PayeeLocationId("cafe"))
    }
  }

  @Test
  fun `A recreated screen keeps its query`() = runDatabaseTest { scope ->
    // given
    insertPayee("cafe", "Café Nero")
    insertPayee("tesco", "Tesco")
    insertLocation("cafe", "cafe")
    insertLocation("tesco", "tesco")
    val savedState = SavedStateHandle()
    createViewModel(scope, savedState).setQuery("tes")

    // then
    createViewModel(scope, savedState).state.test {
      assertThat(awaitResults())
        .extracting(PayeeLocationItem::id)
        .containsExactly(PayeeLocationId("tesco"))
    }
  }

  @Test
  fun `No matches shows no results`() = runDatabaseTest { scope ->
    // given
    insertPayee("tesco", "Tesco")
    insertLocation("tesco", "tesco")
    val viewModel = createViewModel(scope)

    // when
    viewModel.setQuery("xyz")

    // then
    viewModel.state.test {
      var state = awaitItem()
      while (state == NoQuery || state == Loading) state = awaitItem()
      assertThat(state).isEqualTo(NoResults)
    }
  }

  private suspend fun ReceiveTurbine<SearchPayeeLocationsState>.awaitResults():
    List<PayeeLocationItem> {
    var state = awaitItem()
    while (state !is Results) state = awaitItem()
    return state.locations
  }

  private fun BudgetDatabase.createViewModel(
    scope: TestScope,
    savedState: SavedStateHandle = SavedStateHandle(),
  ): SearchPayeeLocationsViewModel {
    val sync = TestSyncController()
    return SearchPayeeLocationsViewModel(
      savedState = savedState,
      loader = loader(scope, sync),
      deleter = deleter(scope, sync),
      viewModelContext = scope.viewModelContext,
    )
  }
}
