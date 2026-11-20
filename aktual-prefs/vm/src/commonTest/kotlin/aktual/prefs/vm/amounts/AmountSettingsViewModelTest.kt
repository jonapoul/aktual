package aktual.prefs.vm.amounts

import aktual.budget.model.Currency
import aktual.budget.model.DbMetadata
import aktual.budget.model.DbMetadataKey.Companion.BudgetName
import aktual.prefs.vm.BooleanPreference
import aktual.prefs.vm.FakeBudgetFormatPreferences
import aktual.prefs.vm.ListPreference
import aktual.test.FakeBudgetFeatureFlags
import aktual.test.TestBudgetLocalPreferences
import aktual.test.viewModelContext
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

class AmountSettingsViewModelTest {
  @Test
  fun `Hide fraction toggles the preference`() = runTest {
    val preferences = FakeBudgetFormatPreferences()
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
  fun `Choosing a currency changes it in the budget`() = runTest {
    val preferences = FakeBudgetFormatPreferences()
    val viewModel = viewModel(preferences = preferences)

    viewModel.state.test {
      awaitItem().currency.onChange(Euro)
      assertThat(awaitItem().currency.value).isEqualTo(Euro)
      assertThat(preferences.currency.get()).isEqualTo(Euro)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Symbol settings are disabled without a currency`() = runTest {
    val preferences = FakeBudgetFormatPreferences()
    val viewModel = viewModel(preferences = preferences)

    viewModel.state.test {
      awaitItem()

      preferences.currency.set(Euro)
      assertThat(awaitItem()).all {
        prop(AmountSettingsState::symbolPosition).prop(ListPreference<*>::enabled).isTrue()
        prop(AmountSettingsState::spaceBetweenAmountAndSymbol)
          .prop(BooleanPreference::enabled)
          .isTrue()
      }

      preferences.currency.set(None)
      assertThat(awaitItem()).all {
        prop(AmountSettingsState::symbolPosition).prop(ListPreference<*>::enabled).isFalse()
        prop(AmountSettingsState::spaceBetweenAmountAndSymbol)
          .prop(BooleanPreference::enabled)
          .isFalse()
      }

      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Currency settings are hidden while the flag is off`() = runTest {
    val flags = FakeBudgetFeatureFlags()
    val viewModel = viewModel(flags = flags)

    viewModel.state.test {
      assertThat(awaitItem()).all {
        prop(AmountSettingsState::currency).prop(ListPreference<*>::visible).isFalse()
        prop(AmountSettingsState::symbolPosition).prop(ListPreference<*>::visible).isFalse()
        prop(AmountSettingsState::spaceBetweenAmountAndSymbol)
          .prop(BooleanPreference::visible)
          .isFalse()
      }

      flags[CurrencySupport].set(true)
      assertThat(awaitItem()).all {
        prop(AmountSettingsState::currency).prop(ListPreference<*>::visible).isTrue()
        prop(AmountSettingsState::symbolPosition).prop(ListPreference<*>::visible).isTrue()
        prop(AmountSettingsState::spaceBetweenAmountAndSymbol)
          .prop(BooleanPreference::visible)
          .isTrue()
      }

      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Budget name comes from the open budget`() = runTest {
    val viewModel = viewModel()

    viewModel.state.test {
      assertThat(awaitItem().budgetName).isEqualTo(BUDGET_NAME)
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
    preferences: FakeBudgetFormatPreferences = FakeBudgetFormatPreferences(),
    flags: FakeBudgetFeatureFlags = FakeBudgetFeatureFlags(),
  ) =
    AmountSettingsViewModel(
      preferences = preferences,
      localPreferences = TestBudgetLocalPreferences(DbMetadata().set(BudgetName, BUDGET_NAME)),
      flags = flags,
      viewModelContext = viewModelContext,
    )

  private companion object {
    const val BUDGET_NAME = "My Budget"
  }
}
