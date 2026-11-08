package aktual.prefs.vm.format

import aktual.budget.model.Currency
import aktual.prefs.CurrencyPreferences
import aktual.prefs.CurrencyPreferencesImpl
import aktual.prefs.FormatPreferences
import aktual.prefs.FormatPreferencesImpl
import aktual.prefs.vm.BooleanPreference
import aktual.prefs.vm.ListPreference
import aktual.test.buildPreferences
import aktual.test.viewModelContext
import alakazam.test.unconfinedDispatcher
import app.cash.turbine.test
import assertk.all
import assertk.assertThat
import assertk.assertions.doesNotContain
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import assertk.assertions.prop
import kotlin.test.Test
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest

class FormatSettingsViewModelTest {
  @Test
  fun `Hide fraction toggles the preference`() = runTest {
    val preferences = FormatPreferencesImpl(buildPreferences(unconfinedDispatcher))
    val viewModel = viewModel(preferences = preferences)

    viewModel.state.test {
      val initial = awaitItem()
      val hideFraction = initial.hideFraction.value

      initial.hideFraction.onChange(!hideFraction)
      assertThat(awaitItem().hideFraction.value).isEqualTo(!hideFraction)

      preferences.hideFraction.set(hideFraction)
      assertThat(awaitItem().hideFraction.value).isEqualTo(hideFraction)

      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Symbol settings are disabled without a currency`() = runTest {
    val currencies = CurrencyPreferencesImpl(buildPreferences(unconfinedDispatcher))
    val viewModel = viewModel(currencies = currencies)

    viewModel.state.test {
      awaitItem()

      currencies.currency.set(Euro)
      assertThat(awaitItem()).all {
        prop(FormatSettingsState::symbolPosition).prop(ListPreference<*>::enabled).isTrue()
        prop(FormatSettingsState::spaceBetweenAmountAndSymbol)
          .prop(BooleanPreference::enabled)
          .isTrue()
      }

      currencies.currency.set(None)
      assertThat(awaitItem()).all {
        prop(FormatSettingsState::symbolPosition).prop(ListPreference<*>::enabled).isFalse()
        prop(FormatSettingsState::spaceBetweenAmountAndSymbol)
          .prop(BooleanPreference::enabled)
          .isFalse()
      }

      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Currency options are sorted by code and exclude unknown`() = runTest {
    val viewModel = viewModel()

    viewModel.state.test {
      val options = awaitItem().currency.options
      assertThat(options).doesNotContain(Currency.Unknown)
      val codes = options.map(Currency::code)
      assertThat(codes).isEqualTo(codes.sorted())
      cancelAndIgnoreRemainingEvents()
    }
  }

  private fun TestScope.viewModel(
    preferences: FormatPreferences = FormatPreferencesImpl(buildPreferences(unconfinedDispatcher)),
    currencies: CurrencyPreferences =
      CurrencyPreferencesImpl(buildPreferences(unconfinedDispatcher)),
  ) = FormatSettingsViewModel(preferences, currencies, viewModelContext = viewModelContext)
}
