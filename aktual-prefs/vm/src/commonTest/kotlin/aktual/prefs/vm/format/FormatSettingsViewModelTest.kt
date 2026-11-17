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
  fun `Distance unit changes the preference`() = runTest {
    val preferences = FormatPreferencesImpl(buildPreferences(unconfinedDispatcher))
    val viewModel = viewModel(preferences)

    viewModel.state.test {
      awaitItem().distanceUnit.onChange(Metric)
      assertThat(awaitItem().distanceUnit.value).isEqualTo(Metric)
      assertThat(preferences.distanceUnit.get()).isEqualTo(Metric)
      cancelAndIgnoreRemainingEvents()
    }
  }

  private fun TestScope.viewModel(preferences: FormatPreferences) =
    FormatSettingsViewModel(preferences, viewModelContext = viewModelContext)
}
