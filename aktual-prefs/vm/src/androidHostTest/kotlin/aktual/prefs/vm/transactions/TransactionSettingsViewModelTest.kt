package aktual.prefs.vm.transactions

import aktual.prefs.TransactionPreferencesImpl
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
class TransactionSettingsViewModelTest {
  @Test
  fun `Alternate row colours toggles the preference`() = runTest {
    val preferences = TransactionPreferencesImpl(buildPreferences(standardDispatcher))
    val viewModel = TransactionSettingsViewModel(preferences)

    viewModel.state.test {
      val initial = awaitItem()
      assertThat(initial.alternateRowColours.value).isFalse()

      initial.alternateRowColours.onChange(true)
      assertThat(awaitItem().alternateRowColours.value).isTrue()

      preferences.alternateRowColours.set(false)
      assertThat(awaitItem().alternateRowColours.value).isFalse()

      cancelAndIgnoreRemainingEvents()
    }
  }
}
