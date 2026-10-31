package aktual.prefs.vm.currency

import aktual.prefs.CurrencyPreferencesImpl
import aktual.prefs.vm.BooleanPreference
import aktual.prefs.vm.ListPreference
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

class CurrencySettingsViewModelTest {
  @Test
  fun `Symbol settings are disabled without a currency`() = runTest {
    val preferences = CurrencyPreferencesImpl(buildPreferences(unconfinedDispatcher))
    val viewModel = CurrencySettingsViewModel(preferences, viewModelContext = viewModelContext)

    viewModel.state.test {
      awaitItem()

      preferences.currency.set(Euro)
      assertThat(awaitItem()).all {
        prop(CurrencySettingsState::symbolPosition).prop(ListPreference<*>::enabled).isTrue()
        prop(CurrencySettingsState::spaceBetweenAmountAndSymbol)
          .prop(BooleanPreference::enabled)
          .isTrue()
      }

      preferences.currency.set(None)
      assertThat(awaitItem()).all {
        prop(CurrencySettingsState::symbolPosition).prop(ListPreference<*>::enabled).isFalse()
        prop(CurrencySettingsState::spaceBetweenAmountAndSymbol)
          .prop(BooleanPreference::enabled)
          .isFalse()
      }

      cancelAndIgnoreRemainingEvents()
    }
  }
}
