package actual.settings.vm

import actual.core.model.ColorSchemeType
import actual.core.model.ColorSchemeType.Dark
import actual.core.model.ColorSchemeType.Light
import actual.core.model.ColorSchemeType.Midnight
import actual.core.model.ColorSchemeType.System
import actual.core.model.DarkColorSchemeType
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
      assertValue(theme(System, Dark))

      viewModel.set(theme(Midnight, Dark))
      assertValue(theme(Midnight, Dark))

      viewModel.set(theme(Dark, Dark))
      assertValue(theme(Dark, Dark))

      viewModel.set(theme(Light, Dark))
      assertValue(theme(Light, Dark))

      viewModel.set(theme(Light, Midnight))
      assertValue(theme(Light, Midnight))

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

  private fun theme(regular: ColorSchemeType, dark: DarkColorSchemeType) = Theme(ThemeConfig(regular, dark))
}
