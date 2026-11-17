package aktual.prefs.vm.dates

import aktual.prefs.vm.FakeBudgetFormatPreferences
import aktual.test.TestBudgetLocalPreferences
import aktual.test.TestCalendar
import aktual.test.viewModelContext
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
    val preferences = FakeBudgetFormatPreferences()
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
    val preferences = FakeBudgetFormatPreferences()
    val viewModel = viewModel(preferences)

    viewModel.state.test {
      awaitItem().firstDayOfWeek.onChange(Monday)
      assertThat(awaitItem().firstDayOfWeek.value).isEqualTo(Monday)
      assertThat(preferences.firstDayOfWeek.get()).isEqualTo(Monday)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Today comes from the calendar`() = runTest {
    val preferences = FakeBudgetFormatPreferences()
    val viewModel = viewModel(preferences)

    viewModel.state.test {
      assertThat(awaitItem().today).isEqualTo(Today)
      cancelAndIgnoreRemainingEvents()
    }
  }

  private fun TestScope.viewModel(preferences: FakeBudgetFormatPreferences) =
    DateSettingsViewModel(
      preferences = preferences,
      localPreferences = TestBudgetLocalPreferences(),
      calendar = TestCalendar(Today),
      viewModelContext = viewModelContext,
    )

  private companion object {
    val Today = LocalDate(year = 2026, month = 2, day = 3)
  }
}
