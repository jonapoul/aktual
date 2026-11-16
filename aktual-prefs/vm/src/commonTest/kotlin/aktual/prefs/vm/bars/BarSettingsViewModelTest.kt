package aktual.prefs.vm.bars

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

class BarSettingsViewModelTest {
  @Test
  fun `Bar settings follow the bar effect`() = runTest {
    val preferences = SystemUiPreferencesImpl(buildPreferences(unconfinedDispatcher))
    val viewModel = BarSettingsViewModel(preferences, viewModelContext = viewModelContext)

    viewModel.state.test {
      assertThat(awaitItem()).all {
        prop(BarSettingsState::blurRadius).prop(SliderPreference::enabled).isTrue()
        prop(BarSettingsState::glassStyle).prop(ListPreference<*>::enabled).isFalse()
      }

      preferences.appBarEffect.set(Glass)
      assertThat(awaitItem()).all {
        prop(BarSettingsState::blurRadius).prop(SliderPreference::enabled).isFalse()
        prop(BarSettingsState::glassStyle).prop(ListPreference<*>::enabled).isTrue()
      }

      preferences.appBarEffect.set(None)
      assertThat(awaitItem()).all {
        prop(BarSettingsState::blurRadius).prop(SliderPreference::enabled).isFalse()
        prop(BarSettingsState::glassStyle).prop(ListPreference<*>::enabled).isFalse()
      }

      expectNoEvents()
      cancelAndIgnoreRemainingEvents()
    }
  }
}
