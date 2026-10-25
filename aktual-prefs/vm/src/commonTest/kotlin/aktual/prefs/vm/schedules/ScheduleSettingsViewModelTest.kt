package aktual.prefs.vm.schedules

import aktual.prefs.SchedulePreferencesImpl
import aktual.test.buildPreferences
import aktual.test.viewModelContext
import alakazam.test.unconfinedDispatcher
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class ScheduleSettingsViewModelTest {
  @Test
  fun `Show completed toggles the preference`() = runTest {
    val preferences = SchedulePreferencesImpl(buildPreferences(unconfinedDispatcher))
    val viewModel = ScheduleSettingsViewModel(preferences, viewModelContext = viewModelContext)

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
