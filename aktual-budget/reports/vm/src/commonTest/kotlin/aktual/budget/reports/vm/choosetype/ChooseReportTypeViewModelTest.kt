package aktual.budget.reports.vm.choosetype

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.DbJson
import aktual.budget.db.dao.DashboardDao
import aktual.budget.model.DashboardPageId
import aktual.budget.model.SyncedPrefKey.FeatureFlag
import aktual.budget.model.WidgetId
import aktual.budget.model.WidgetType
import aktual.budget.reports.vm.DashboardSync
import aktual.budget.reports.vm.MarkdownReportMeta
import aktual.budget.reports.vm.runSyncedDatabaseTest
import aktual.test.FakeBudgetFeatureFlags
import aktual.test.assertThatNextEmissionIsEqualTo
import aktual.test.viewModelContext
import alakazam.test.TestCoroutineContexts
import app.cash.turbine.test
import assertk.all
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.containsAtLeast
import assertk.assertions.doesNotContain
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle

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
  fun `Markdown reports are created with starting content`() = runChooseTest { viewModel, dao ->
    viewModel.shouldNavigateEvent.test {
      viewModel.createReport(Markdown)
      assertThatNextEmissionIsEqualTo(ChooseReportTypeViewModel.ShouldNavigateEvent(WIDGET))
    }

    val meta = checkNotNull(dao.meta(WIDGET))
    assertThat(DbJson.decodeFromJsonElement(MarkdownReportMeta.serializer(), meta))
      .isEqualTo(MarkdownReportMeta(content = "reports_text_default"))
  }

  @Test
  fun `Custom reports aren't created here`() = runChooseTest { viewModel, dao ->
    viewModel.createReport(Custom)

    assertThat(dao.observeByPage(PAGE).first()).isEmpty()
  }

  @Test
  fun `Types behind an experimental flag are hidden while it's off`() {
    val flags = FakeBudgetFeatureFlags()
    runChooseTest(flags) { viewModel, _ ->
      viewModel.types.test {
        assertThat(awaitItem()).all {
          doesNotContain(WidgetType.Sankey)
          doesNotContain(WidgetType.Formula)
          containsAtLeast(WidgetType.NetWorth, WidgetType.Markdown, WidgetType.Custom)
        }

        flags[SankeyReport].set(true)
        assertThat(awaitItem()).all {
          contains(WidgetType.Sankey)
          doesNotContain(WidgetType.Formula)
        }
      }
    }
  }

  @Test
  fun `Every type shows with every flag on`() =
    runChooseTest(FakeBudgetFeatureFlags(FeatureFlag.entries.toSet())) { viewModel, _ ->
      viewModel.types.test {
        var types = awaitItem()
        while (types.size < WidgetType.known.size) types = awaitItem()
        assertThat(types).isEqualTo(WidgetType.known)
      }
    }

  private fun runChooseTest(
    flags: FakeBudgetFeatureFlags = FakeBudgetFeatureFlags(),
    action: suspend BudgetDatabase.(ChooseReportTypeViewModel, DashboardDao) -> Unit,
  ) = runSyncedDatabaseTest { scope, controller ->
    val dispatcher = StandardTestDispatcher(scope.testScheduler)
    // Keep viewModelScope on the test scheduler, so its queries can't outlive the database
    val dao = DashboardDao(this, TestCoroutineContexts(dispatcher))
    val viewModel =
      ChooseReportTypeViewModel(
        page = PAGE,
        uuidGenerator = { WIDGET.value },
        sync = DashboardSync(dao, controller),
        strings = { it.key },
        flags = flags,
        viewModelContext = scope.viewModelContext,
      )
    action(viewModel, dao)
    scope.advanceUntilIdle()
  }

  private companion object {
    val PAGE = DashboardPageId("page")
    val WIDGET = WidgetId("widget")
  }
}
