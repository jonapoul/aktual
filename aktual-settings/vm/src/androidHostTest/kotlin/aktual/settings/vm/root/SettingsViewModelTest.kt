package aktual.settings.vm.root

import aktual.core.prefs.AppGlobalPreferences
import aktual.core.prefs.AppGlobalPreferencesImpl
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
  private lateinit var preferences: AppGlobalPreferences

  private fun TestScope.before() {
    val prefs = buildPreferences(standardDispatcher)
    preferences = AppGlobalPreferencesImpl(prefs)
    viewModel = SettingsViewModel(preferences)
  }

  @Test
  fun `Bottom bar preference changes`() = runTest {
    before()
    viewModel.state.test {
      assertThat(awaitItem()).showBottomBar().isTrue()

      preferences.showBottomBar.set(false)
      assertThat(awaitItem()).showBottomBar().isFalse()

      expectNoEvents()
      cancelAndIgnoreRemainingEvents()
    }
  }

  private fun Assert<SettingsScreenState>.showBottomBar(): Assert<Boolean> = transform { state ->
    state.systemUi.showStatusBar.value
  }
}
