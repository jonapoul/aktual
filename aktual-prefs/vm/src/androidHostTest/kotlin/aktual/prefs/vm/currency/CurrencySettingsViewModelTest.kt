package aktual.prefs.vm.currency

import aktual.prefs.CurrencyPreferencesImpl
import aktual.test.buildPreferences
import alakazam.test.standardDispatcher
import app.cash.turbine.test
import assertk.all
import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import assertk.assertions.prop
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CurrencySettingsViewModelTest {
  @Test
  fun `Symbol settings are disabled without a currency`() = runTest {
    val preferences = CurrencyPreferencesImpl(buildPreferences(standardDispatcher))
    val viewModel = CurrencySettingsViewModel(preferences)

    viewModel.state.test {
      awaitItem()

      preferences.currency.set(Euro)
      assertThat(awaitItem()).all {
        prop("symbolPosition") { it.symbolPosition.enabled }.isTrue()
        prop("spaceBetweenAmountAndSymbol") { it.spaceBetweenAmountAndSymbol.enabled }.isTrue()
      }

      preferences.currency.set(None)
      assertThat(awaitItem()).all {
        prop("symbolPosition") { it.symbolPosition.enabled }.isFalse()
        prop("spaceBetweenAmountAndSymbol") { it.spaceBetweenAmountAndSymbol.enabled }.isFalse()
      }

      cancelAndIgnoreRemainingEvents()
    }
  }
}
