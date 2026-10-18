package aktual.budget.home.vm

import aktual.budget.home.vm.ReportsCardState.Loaded
import aktual.budget.model.WidgetId
import aktual.budget.reports.vm.pinned.PinnedReport
import aktual.budget.reports.vm.pinned.ReportSummary
import aktual.test.TestCalendar
import aktual.test.runDatabaseTest
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.prop
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.resetMain
import kotlinx.datetime.LocalDate
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class HomeViewModelReportsTest {
  @AfterTest
  fun after() {
    Dispatchers.resetMain()
  }

  @Test
  fun `Pinned reports show on their card`() = runDatabaseTest { scope ->
    val pinned = FakePinnedReports(listOf(pinned("a"), pinned("b")))
    val viewModel = createHomeViewModel(scope, CALENDAR, pinnedReports = pinned)

    viewModel.state.test {
      assertThat(awaitSettled().reports)
        .isEqualTo(
          Loaded(persistentListOf(pinned("a"), pinned("b")), canPinMore = true),
        )
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `No more reports can be pinned once the card is full`() = runDatabaseTest { scope ->
    val pinned = FakePinnedReports(listOf(pinned("a"), pinned("b"), pinned("c")))
    val viewModel = createHomeViewModel(scope, CALENDAR, pinnedReports = pinned)

    viewModel.state.test {
      assertThat(awaitSettled().reports).isInstanceOf<Loaded>().prop(Loaded::canPinMore).isFalse()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `A new budget with a pinned report has something to show`() = runDatabaseTest { scope ->
    val pinned = FakePinnedReports(listOf(pinned("a")))
    val viewModel = createHomeViewModel(scope, CALENDAR, pinnedReports = pinned)

    viewModel.state.test { assertThat(awaitSettled().isEmpty).isFalse() }
  }

  @Test
  fun `Unpinning the last report empties the card`() = runDatabaseTest { scope ->
    val pinned = FakePinnedReports(listOf(pinned("a")))
    val viewModel = createHomeViewModel(scope, CALENDAR, pinnedReports = pinned)

    viewModel.state.test {
      awaitSettled()

      viewModel.unpinReport(WidgetId("a"))
      assertThat(awaitItem().reports).isEqualTo(Empty)
      assertThat(pinned.calls).containsExactly("unpin a")
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Pinning and replacing go to the pinned reports`() = runDatabaseTest { scope ->
    val pinned = FakePinnedReports()
    val viewModel = createHomeViewModel(scope, CALENDAR, pinnedReports = pinned)

    viewModel.state.test {
      awaitSettled()

      viewModel.pinReport(WidgetId("a"))
      viewModel.replaceReport(WidgetId("a"), WidgetId("b"))
      scope.testScheduler.advanceUntilIdle()
      assertThat(pinned.calls).containsExactly("pin a", "replace a b")
      cancelAndIgnoreRemainingEvents()
    }
  }

  private companion object {
    val CALENDAR = TestCalendar(LocalDate(2026, 4, 1))

    fun pinned(id: String) =
      PinnedReport(ReportSummary(WidgetId(id), NetWorth, name = id), data = null)
  }
}
