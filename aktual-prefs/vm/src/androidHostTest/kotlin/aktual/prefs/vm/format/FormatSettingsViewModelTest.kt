package aktual.prefs.vm.format

import aktual.prefs.FormatPreferencesImpl
import aktual.test.buildPreferences
import alakazam.test.standardDispatcher
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FormatSettingsViewModelTest {
  @Test
  fun `Hide fraction toggles the preference`() = runTest {
    val preferences = FormatPreferencesImpl(buildPreferences(standardDispatcher))
    val viewModel = FormatSettingsViewModel(preferences)

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
}
