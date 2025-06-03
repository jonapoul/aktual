package actual.settings.vm

import actual.core.model.ColorSchemeType
import actual.prefs.AppLocalPreferences
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
      preferences = AppLocalPreferences(prefs),
    )
  }

  @Test
  fun `Color scheme changes`() = runTest {
    before()
    viewModel.prefValues.test {
      assertValue(Theme(ColorSchemeType.System))

      viewModel.set(Theme(ColorSchemeType.Midnight))
      assertValue(Theme(ColorSchemeType.Midnight))

      viewModel.set(Theme(ColorSchemeType.Dark))
      assertValue(Theme(ColorSchemeType.Dark))

      viewModel.set(Theme(ColorSchemeType.Light))
      assertValue(Theme(ColorSchemeType.Light))

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
}
