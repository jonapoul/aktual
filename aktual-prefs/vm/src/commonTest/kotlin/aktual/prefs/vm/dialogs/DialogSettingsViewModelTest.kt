package aktual.prefs.vm.dialogs

import aktual.prefs.SystemUiPreferencesImpl
import aktual.test.buildPreferences
import aktual.test.viewModelContext
import alakazam.test.unconfinedDispatcher
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class DialogSettingsViewModelTest {
  @Test
  fun `Blur settings are disabled without dialog blur`() = runTest {
    val preferences = SystemUiPreferencesImpl(buildPreferences(unconfinedDispatcher))
    val viewModel = DialogSettingsViewModel(preferences, viewModelContext = viewModelContext)

    viewModel.state.test {
      val initial = awaitItem()
      assertThat(initial.blurRadius.enabled).isTrue()
      assertThat(initial.blurAlpha.enabled).isTrue()

      preferences.hazeDialogs.set(false)
      val disabled = awaitItem()
      assertThat(disabled.blurRadius.enabled).isFalse()
      assertThat(disabled.blurAlpha.enabled).isFalse()

      expectNoEvents()
      cancelAndIgnoreRemainingEvents()
    }
  }
}
