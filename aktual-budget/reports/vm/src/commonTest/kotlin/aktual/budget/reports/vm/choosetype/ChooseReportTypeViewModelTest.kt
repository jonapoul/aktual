package aktual.budget.reports.vm.choosetype

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.dao.CustomReportsDao
import aktual.budget.db.dao.DashboardDao
import aktual.budget.db.withoutResult
import aktual.budget.model.CustomReportId
import aktual.budget.model.DashboardPageId
import aktual.budget.model.WidgetId
import aktual.budget.reports.vm.DashboardSync
import aktual.budget.reports.vm.emptyCustomReport
import aktual.budget.reports.vm.runSyncedDatabaseTest
import aktual.test.assertThatNextEmissionIsEqualTo
import alakazam.test.TestCoroutineContexts
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class ChooseReportTypeViewModelTest {
  @Test
  fun `Saved reports are listed by name`() = runChooseTest { viewModel, _ ->
    customReportsQueries.withoutResult {
      insert(emptyCustomReport(CustomReportId("b")).copy(name = "groceries", tombstone = false))
      insert(emptyCustomReport(CustomReportId("a")).copy(name = "Bills", tombstone = false))
      insert(emptyCustomReport(CustomReportId("c")).copy(name = "Deleted", tombstone = true))
    }

    viewModel.savedReports.test {
      assertThatNextEmissionIsEqualTo(persistentListOf())
      assertThatNextEmissionIsEqualTo(
        persistentListOf(
          SavedCustomReport(CustomReportId("a"), "Bills"),
          SavedCustomReport(CustomReportId("b"), "groceries"),
        ),
      )
    }
  }

  @Test
  fun `Adding a saved report puts a widget for it on the page`() = runChooseTest { viewModel, dao ->
    viewModel.shouldNavigateEvent.test {
      viewModel.addSavedReport(REPORT)
      assertThatNextEmissionIsEqualTo(ChooseReportTypeViewModel.ShouldNavigateEvent(WIDGET))
    }

    val widget = dao.observeByPage(PAGE).first().single()
    assertThat(widget.type).isEqualTo(Custom)
    assertThat(widget.meta).isEqualTo(JsonObject(mapOf("id" to JsonPrimitive(REPORT.value))))
  }

  private fun runChooseTest(
    action: suspend BudgetDatabase.(ChooseReportTypeViewModel, DashboardDao) -> Unit,
  ) = runSyncedDatabaseTest { scope, controller ->
    val dispatcher = StandardTestDispatcher(scope.testScheduler)
    // Keep viewModelScope on the test scheduler, so its queries can't outlive the database
    Dispatchers.setMain(dispatcher)
    val contexts = TestCoroutineContexts(dispatcher)
    val dao = DashboardDao(this, contexts)
    val viewModel =
      ChooseReportTypeViewModel(
        page = PAGE,
        uuidGenerator = { WIDGET.value },
        sync = DashboardSync(dao, controller),
        customReportsDao = CustomReportsDao(this, contexts),
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
    val REPORT = CustomReportId("report")
  }
}
