package aktual.prefs.vm.systemui

import aktual.prefs.SystemUiPreferencesImpl
import aktual.test.buildPreferences
import aktual.test.viewModelContext
import alakazam.test.unconfinedDispatcher
import app.cash.turbine.test
import assertk.all
import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import assertk.assertions.prop
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class SystemUiSettingsViewModelTest {
  @Test
  fun `Bottom bar preference changes`() = runTest {
    val preferences = SystemUiPreferencesImpl(buildPreferences(unconfinedDispatcher))
    val viewModel = SystemUiSettingsViewModel(preferences, viewModelContext = viewModelContext)

    viewModel.state.test {
      assertThat(awaitItem().showStatusBar.value).isTrue()

      preferences.showBottomBar.set(false)
      assertThat(awaitItem().showStatusBar.value).isFalse()

      expectNoEvents()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Effect settings follow the bar effect and dialog blur`() = runTest {
    val preferences = SystemUiPreferencesImpl(buildPreferences(unconfinedDispatcher))
    val viewModel = SystemUiSettingsViewModel(preferences, viewModelContext = viewModelContext)

    viewModel.state.test {
      assertThat(awaitItem()).all {
        prop(SystemUiSettingsState::showBlurSettings).isTrue()
        prop(SystemUiSettingsState::showGlassSettings).isFalse()
      }

      preferences.appBarEffect.set(Glass)
      assertThat(awaitItem()).all {
        // Dialog blur is still on
        prop(SystemUiSettingsState::showBlurSettings).isTrue()
        prop(SystemUiSettingsState::showGlassSettings).isTrue()
      }

      preferences.hazeDialogs.set(false)
      assertThat(awaitItem()).all {
        prop(SystemUiSettingsState::showBlurSettings).isFalse()
        prop(SystemUiSettingsState::showGlassSettings).isTrue()
      }

      preferences.appBarEffect.set(None)
      assertThat(awaitItem()).all {
        prop(SystemUiSettingsState::showBlurSettings).isFalse()
        prop(SystemUiSettingsState::showGlassSettings).isFalse()
      }

      expectNoEvents()
      cancelAndIgnoreRemainingEvents()
    }
  }
}
