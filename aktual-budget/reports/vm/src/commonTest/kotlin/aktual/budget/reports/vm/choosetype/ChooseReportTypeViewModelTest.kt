package aktual.budget.reports.vm.choosetype

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.dao.DashboardDao
import aktual.budget.model.DashboardPageId
import aktual.budget.model.WidgetId
import aktual.budget.reports.vm.DashboardSync
import aktual.budget.reports.vm.runSyncedDatabaseTest
import aktual.test.assertThatNextEmissionIsEqualTo
import alakazam.test.TestCoroutineContexts
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

class ChooseReportTypeViewModelTest {
  @Test
  fun `Creating a report puts a widget for it on the page`() = runChooseTest { viewModel, dao ->
    viewModel.shouldNavigateEvent.test {
      viewModel.createReport(NetWorth)
      assertThatNextEmissionIsEqualTo(ChooseReportTypeViewModel.ShouldNavigateEvent(WIDGET))
    }

    val widget = dao.observeByPage(PAGE).first().single()
    assertThat(widget.id).isEqualTo(WIDGET)
    assertThat(widget.type).isEqualTo(NetWorth)
  }

  @Test
  fun `Custom reports aren't created here`() = runChooseTest { viewModel, dao ->
    viewModel.createReport(Custom)

    assertThat(dao.observeByPage(PAGE).first()).isEmpty()
  }

  private fun runChooseTest(
    action: suspend BudgetDatabase.(ChooseReportTypeViewModel, DashboardDao) -> Unit,
  ) = runSyncedDatabaseTest { scope, controller ->
    val dispatcher = StandardTestDispatcher(scope.testScheduler)
    // Keep viewModelScope on the test scheduler, so its queries can't outlive the database
    Dispatchers.setMain(dispatcher)
    val dao = DashboardDao(this, TestCoroutineContexts(dispatcher))
    val viewModel =
      ChooseReportTypeViewModel(
        page = PAGE,
        uuidGenerator = { WIDGET.value },
        sync = DashboardSync(dao, controller),
      )
    try {
      action(viewModel, dao)
      scope.advanceUntilIdle()
    } finally {
      Dispatchers.resetMain()
    }
  }

  private companion object {
    val PAGE = DashboardPageId("page")
    val WIDGET = WidgetId("widget")
  }
}
