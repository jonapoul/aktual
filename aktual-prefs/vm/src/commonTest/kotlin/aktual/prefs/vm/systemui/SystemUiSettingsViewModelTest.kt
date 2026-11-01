package aktual.prefs.vm.systemui

import aktual.prefs.SystemUiPreferencesImpl
import aktual.prefs.vm.ListPreference
import aktual.prefs.vm.SliderPreference
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
  fun `Bar settings follow the bar effect`() = runTest {
    val preferences = SystemUiPreferencesImpl(buildPreferences(unconfinedDispatcher))
    val viewModel = SystemUiSettingsViewModel(preferences, viewModelContext = viewModelContext)

    viewModel.state.test {
      assertThat(awaitItem()).all {
        prop(SystemUiSettingsState::barBlurRadius).prop(SliderPreference::enabled).isTrue()
        prop(SystemUiSettingsState::glassStyle).prop(ListPreference<*>::enabled).isFalse()
      }

      preferences.appBarEffect.set(Glass)
      assertThat(awaitItem()).all {
        prop(SystemUiSettingsState::barBlurRadius).prop(SliderPreference::enabled).isFalse()
        prop(SystemUiSettingsState::glassStyle).prop(ListPreference<*>::enabled).isTrue()
      }

      preferences.appBarEffect.set(None)
      assertThat(awaitItem()).all {
        prop(SystemUiSettingsState::barBlurRadius).prop(SliderPreference::enabled).isFalse()
        prop(SystemUiSettingsState::glassStyle).prop(ListPreference<*>::enabled).isFalse()
      }

      expectNoEvents()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Dialog blur settings are disabled without dialog blur`() = runTest {
    val preferences = SystemUiPreferencesImpl(buildPreferences(unconfinedDispatcher))
    val viewModel = SystemUiSettingsViewModel(preferences, viewModelContext = viewModelContext)

    viewModel.state.test {
      assertThat(awaitItem().dialogBlurRadius.enabled).isTrue()

      preferences.hazeDialogs.set(false)
      assertThat(awaitItem().dialogBlurRadius.enabled).isFalse()

      expectNoEvents()
      cancelAndIgnoreRemainingEvents()
    }
  }
}
