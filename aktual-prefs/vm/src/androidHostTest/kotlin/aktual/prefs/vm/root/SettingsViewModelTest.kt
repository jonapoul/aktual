package aktual.prefs.vm.root

import aktual.core.prefs.CurrencyPreferences
import aktual.core.prefs.CurrencyPreferencesImpl
import aktual.core.prefs.FormatPreferences
import aktual.core.prefs.FormatPreferencesImpl
import aktual.core.prefs.SystemUiPreferences
import aktual.core.prefs.SystemUiPreferencesImpl
import aktual.test.buildPreferences
import alakazam.test.standardDispatcher
import app.cash.turbine.test
import assertk.Assert
import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlin.test.Test
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SettingsViewModelTest {
  private lateinit var viewModel: SettingsViewModel
  private lateinit var uiPreferences: SystemUiPreferences
  private lateinit var formatPreferences: FormatPreferences
  private lateinit var currencyPreferences: CurrencyPreferences

  private fun TestScope.before() {
    val dataStore = buildPreferences(standardDispatcher)
    uiPreferences = SystemUiPreferencesImpl(dataStore)
    formatPreferences = FormatPreferencesImpl(dataStore)
    currencyPreferences = CurrencyPreferencesImpl(dataStore)
    viewModel = SettingsViewModel(uiPreferences, formatPreferences, currencyPreferences)
  }

  @Test
  fun `Bottom bar preference changes`() = runTest {
    before()
    viewModel.state.test {
      assertThat(awaitItem()).showBottomBar().isTrue()

      uiPreferences.showBottomBar.set(false)
      assertThat(awaitItem()).showBottomBar().isFalse()

      expectNoEvents()
      cancelAndIgnoreRemainingEvents()
    }
  }

  private fun Assert<SettingsScreenState>.showBottomBar(): Assert<Boolean> = transform { state ->
    state.systemUi.showStatusBar.value
  }
}
