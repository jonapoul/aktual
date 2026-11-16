package aktual.prefs.vm.dates

import aktual.core.utils.Calendar
import aktual.prefs.FormatPreferences
import aktual.prefs.FormatPreferencesImpl
import aktual.test.buildPreferences
import aktual.test.viewModelContext
import alakazam.test.unconfinedDispatcher
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate

class DateSettingsViewModelTest {
  @Test
  fun `Date format changes the preference`() = runTest {
    val preferences = FormatPreferencesImpl(buildPreferences(unconfinedDispatcher))
    val viewModel = viewModel(preferences)

    viewModel.state.test {
      awaitItem().dateFormat.onChange(DdMmYyyyDot)
      assertThat(awaitItem().dateFormat.value).isEqualTo(DdMmYyyyDot)
      assertThat(preferences.dateFormat.get()).isEqualTo(DdMmYyyyDot)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `First day of week changes the preference`() = runTest {
    val preferences = FormatPreferencesImpl(buildPreferences(unconfinedDispatcher))
    val viewModel = viewModel(preferences)

    viewModel.state.test {
      awaitItem().firstDayOfWeek.onChange(Sunday)
      assertThat(awaitItem().firstDayOfWeek.value).isEqualTo(Sunday)
      assertThat(preferences.firstDayOfWeek.get()).isEqualTo(Sunday)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Today comes from the calendar`() = runTest {
    val preferences = FormatPreferencesImpl(buildPreferences(unconfinedDispatcher))
    val viewModel = viewModel(preferences)

    viewModel.state.test {
      assertThat(awaitItem().today).isEqualTo(Today)
      cancelAndIgnoreRemainingEvents()
    }
  }

  private fun TestScope.viewModel(preferences: FormatPreferences) =
    DateSettingsViewModel(preferences, Calendar { Today }, viewModelContext = viewModelContext)

  private companion object {
    val Today = LocalDate(year = 2026, month = 2, day = 3)
  }
}
