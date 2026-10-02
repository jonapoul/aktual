package aktual.prefs.vm.schedules

import aktual.prefs.SchedulePreferencesImpl
import aktual.test.buildPreferences
import alakazam.test.standardDispatcher
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ScheduleSettingsViewModelTest {
  @Test
  fun `Show completed toggles the preference`() = runTest {
    val preferences = SchedulePreferencesImpl(buildPreferences(standardDispatcher))
    val viewModel = ScheduleSettingsViewModel(preferences)

    viewModel.state.test {
      val initial = awaitItem()
      assertThat(initial.showCompleted.value).isFalse()

      initial.showCompleted.onChange(true)
      assertThat(awaitItem().showCompleted.value).isTrue()

      preferences.showCompleted.set(false)
      assertThat(awaitItem().showCompleted.value).isFalse()

      cancelAndIgnoreRemainingEvents()
    }
  }
}
