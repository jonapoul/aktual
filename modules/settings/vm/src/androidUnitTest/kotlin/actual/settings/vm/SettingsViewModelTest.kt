package actual.settings.vm

import actual.core.model.DarkColorSchemeType
import actual.core.model.RegularColorSchemeType
import actual.prefs.AppGlobalPreferences
import actual.settings.vm.PreferenceValue.ShowBottomBar
import actual.settings.vm.PreferenceValue.Theme
import actual.test.buildPreferences
import alakazam.test.core.standardDispatcher
import app.cash.turbine.TurbineTestContext
import app.cash.turbine.test
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.assertContains

@RunWith(RobolectricTestRunner::class)
class SettingsViewModelTest {
  private lateinit var viewModel: SettingsViewModel

  private fun TestScope.before() {
    val dispatcher = standardDispatcher
    val prefs = buildPreferences(dispatcher)

    viewModel = SettingsViewModel(
      preferences = AppGlobalPreferences(prefs),
    )
  }

  @Test
  fun `Color scheme changes`() = runTest {
    before()
    viewModel.prefValues.test {
      assertValue(theme(RegularColorSchemeType.System, DarkColorSchemeType.Dark))

      viewModel.set(theme(RegularColorSchemeType.Dark, DarkColorSchemeType.Dark))
      assertValue(theme(RegularColorSchemeType.Dark, DarkColorSchemeType.Dark))

      viewModel.set(theme(RegularColorSchemeType.Light, DarkColorSchemeType.Dark))
      assertValue(theme(RegularColorSchemeType.Light, DarkColorSchemeType.Dark))

      viewModel.set(theme(RegularColorSchemeType.Light, DarkColorSchemeType.Midnight))
      assertValue(theme(RegularColorSchemeType.Light, DarkColorSchemeType.Midnight))

      expectNoEvents()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Bottom bar preference changes`() = runTest {
    before()
    viewModel.prefValues.test {
      assertValue(ShowBottomBar(true))

      viewModel.set(ShowBottomBar(false))
      assertValue(ShowBottomBar(false))

      expectNoEvents()
      cancelAndIgnoreRemainingEvents()
    }
  }

  private suspend fun TurbineTestContext<ImmutableList<PreferenceValue>>.assertValue(value: PreferenceValue) =
    assertContains(awaitItem(), value)

  private fun theme(regular: RegularColorSchemeType, dark: DarkColorSchemeType) = Theme(ThemeConfig(regular, dark))
}
