package aktual.prefs.vm.transactions

import aktual.prefs.TransactionPreferences
import aktual.prefs.TransactionPreferencesImpl
import aktual.test.buildPreferences
import aktual.test.viewModelContext
import alakazam.test.unconfinedDispatcher
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isTrue
import kotlin.test.Test
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest

class TransactionSettingsViewModelTest {
  @Test
  fun `Alternate row colours changes the preference`() = runTest {
    val preferences = TransactionPreferencesImpl(buildPreferences(unconfinedDispatcher))
    val viewModel = viewModel(preferences)

    viewModel.state.test {
      awaitItem().alternateRowColours.onChange(true)
      assertThat(awaitItem().alternateRowColours.value).isTrue()
      assertThat(preferences.alternateRowColours.get()).isTrue()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Expand splits changes the preference`() = runTest {
    val preferences = TransactionPreferencesImpl(buildPreferences(unconfinedDispatcher))
    val viewModel = viewModel(preferences)

    viewModel.state.test {
      awaitItem().expandSplits.onChange(true)
      assertThat(awaitItem().expandSplits.value).isTrue()
      assertThat(preferences.expandSplits.get()).isTrue()
      cancelAndIgnoreRemainingEvents()
    }
  }

  private fun TestScope.viewModel(preferences: TransactionPreferences) =
    TransactionSettingsViewModel(preferences, viewModelContext = viewModelContext)
}
