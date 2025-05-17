package actual.settings.vm

import actual.core.colorscheme.ColorSchemePreferences
import actual.core.colorscheme.ColorSchemeType
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
      colorSchemePrefs = ColorSchemePreferences(prefs),
    )
  }

  @Test
  fun `Color scheme changes`() = runTest {
    before()
    viewModel.prefValues.test {
      assertValue(PreferenceValue.Theme(ColorSchemeType.System))

      viewModel.set(PreferenceValue.Theme(ColorSchemeType.Midnight))
      assertValue(PreferenceValue.Theme(ColorSchemeType.Midnight))

      viewModel.set(PreferenceValue.Theme(ColorSchemeType.Dark))
      assertValue(PreferenceValue.Theme(ColorSchemeType.Dark))

      viewModel.set(PreferenceValue.Theme(ColorSchemeType.Light))
      assertValue(PreferenceValue.Theme(ColorSchemeType.Light))

      expectNoEvents()
      cancelAndIgnoreRemainingEvents()
    }
  }

  private suspend fun TurbineTestContext<ImmutableList<PreferenceValue>>.assertValue(value: PreferenceValue) =
    assertContains(awaitItem(), value)
}
