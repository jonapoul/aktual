package aktual.prefs.vm.transactions

import aktual.prefs.TransactionPreferencesImpl
import aktual.test.buildPreferences
import aktual.test.viewModelContext
import alakazam.test.unconfinedDispatcher
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class TransactionSettingsViewModelTest {
  @Test
  fun `Alternate row colours toggles the preference`() = runTest {
    val preferences = TransactionPreferencesImpl(buildPreferences(unconfinedDispatcher))
    val viewModel = TransactionSettingsViewModel(preferences, viewModelContext = viewModelContext)

    viewModel.state.test {
      val initial = awaitItem()
      assertThat(initial.alternateRowColours.value).isFalse()

      initial.alternateRowColours.onChange(true)
      assertThat(awaitItem().alternateRowColours.value).isTrue()

      preferences.alternateRowColours.set(false)
      assertThat(awaitItem().alternateRowColours.value).isFalse()

      cancelAndIgnoreRemainingEvents()
    }
  }
}
