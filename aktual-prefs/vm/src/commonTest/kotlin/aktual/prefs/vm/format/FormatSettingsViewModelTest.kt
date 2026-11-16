package aktual.prefs.vm.format

import aktual.prefs.FormatPreferences
import aktual.prefs.FormatPreferencesImpl
import aktual.test.buildPreferences
import aktual.test.viewModelContext
import alakazam.test.unconfinedDispatcher
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest

class FormatSettingsViewModelTest {
  @Test
  fun `Date format changes the preference`() = runTest {
    val preferences = FormatPreferencesImpl(buildPreferences(unconfinedDispatcher))
    val viewModel = viewModel(preferences)

    viewModel.state.test {
      awaitItem().dateFormat.onChange(DdMmYyyyDot)
      assertThat(awaitItem().dateFormat.value).isEqualTo(DdMmYyyyDot)
      assertThat(preferences.dateFormat.get()).isEqualTo(DdMmYyyyDot)
      cancelAndIgnoreRemainingEvents()
    }
  }

  private fun TestScope.viewModel(preferences: FormatPreferences) =
    FormatSettingsViewModel(preferences, viewModelContext = viewModelContext)
}
