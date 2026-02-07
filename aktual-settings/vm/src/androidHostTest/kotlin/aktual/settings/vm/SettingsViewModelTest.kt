package aktual.settings.vm

import aktual.core.model.DarkColorSchemeType
import aktual.core.model.RegularColorSchemeType
import aktual.core.prefs.AppGlobalPreferencesImpl
import aktual.settings.vm.PreferenceValue.ShowBottomBar
import aktual.settings.vm.PreferenceValue.Theme
import aktual.test.buildPreferences
import alakazam.test.standardDispatcher
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.contains
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
class SettingsViewModelTest {
  private lateinit var viewModel: SettingsViewModel

  private fun TestScope.before() {
    val dispatcher = standardDispatcher
    val prefs = buildPreferences(dispatcher)

    viewModel = SettingsViewModel(
      preferences = AppGlobalPreferencesImpl(prefs),
    )
  }

  @Test
  fun `Color scheme changes`() = runTest {
    before()
    viewModel.prefValues.test {
      assertThat(awaitItem()).contains(theme(RegularColorSchemeType.System, DarkColorSchemeType.Dark))

      viewModel.set(theme(RegularColorSchemeType.Dark, DarkColorSchemeType.Dark))
      assertThat(awaitItem()).contains(theme(RegularColorSchemeType.Dark, DarkColorSchemeType.Dark))

      viewModel.set(theme(RegularColorSchemeType.Light, DarkColorSchemeType.Dark))
      assertThat(awaitItem()).contains(theme(RegularColorSchemeType.Light, DarkColorSchemeType.Dark))

      viewModel.set(theme(RegularColorSchemeType.Light, DarkColorSchemeType.Midnight))
      assertThat(awaitItem()).contains(theme(RegularColorSchemeType.Light, DarkColorSchemeType.Midnight))

      expectNoEvents()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Bottom bar preference changes`() = runTest {
    before()
    viewModel.prefValues.test {
      assertThat(awaitItem()).contains(ShowBottomBar(true))

      viewModel.set(ShowBottomBar(false))
      assertThat(awaitItem()).contains(ShowBottomBar(false))

      expectNoEvents()
      cancelAndIgnoreRemainingEvents()
    }
  }

  private fun theme(regular: RegularColorSchemeType, dark: DarkColorSchemeType) = Theme(ThemeConfig(regular, dark))
}
