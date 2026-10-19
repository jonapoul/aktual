package aktual.budget.reports.ui.report

import aktual.budget.reports.ui.Tags
import aktual.budget.reports.ui.charts.PREVIEW_BUDGET_ANALYSIS_DATA
import aktual.budget.reports.vm.BudgetAnalysisSettings
import aktual.budget.reports.vm.TimeFrame
import aktual.budget.reports.vm.report.BudgetAnalysisState
import aktual.test.setPreviewContent
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.v2.runComposeUiTest
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.YearMonth

class BudgetAnalysisScaffoldTest {
  @Test
  fun `Viewing offers to edit, without settings`() = runComposeUiTest {
    // given
    val actions = mutableListOf<BudgetAnalysisAction>()
    setPreviewContent {
      BudgetAnalysisScaffold(state = state(isEditing = false), onAction = { actions += it })
    }

    // when
    onNodeWithTag(Tags.EditReport).performClick()

    // then
    onAllNodesWithTag(Tags.CustomSwitch).assertCountEquals(0)
    assertThat(actions).containsExactly(ReportEditorAction.StartEditing)
  }

  @Test
  fun `Picking a graph edits the draft`() = runComposeUiTest {
    // given
    val actions = mutableListOf<BudgetAnalysisAction>()
    setPreviewContent {
      BudgetAnalysisScaffold(state = state(isEditing = true), onAction = { actions += it })
    }

    // when
    onNodeWithText("Graph").performClick()
    onNodeWithText("Line").performClick()

    // then
    val edit = actions.single() as Edit
    assertThat(edit.transform(SETTINGS).graphType).isEqualTo(Line)
  }

  @Test
  fun `Switches toggle their option`() = runComposeUiTest {
    // given
    val actions = mutableListOf<BudgetAnalysisAction>()
    setPreviewContent {
      BudgetAnalysisScaffold(state = state(isEditing = true), onAction = { actions += it })
    }

    // when
    onNodeWithText("Show balance").performScrollTo().performClick()

    // then
    val edit = actions.single() as Edit
    assertThat(edit.transform(SETTINGS).showBalance).isFalse()
  }

  private fun state(isEditing: Boolean) =
    BudgetAnalysisState.Loaded(
      name = null,
      draft = SETTINGS,
      preview = PREVIEW_BUDGET_ANALYSIS_DATA,
      isEditing = isEditing,
      hasChanges = false,
    )

  private companion object {
    val SETTINGS =
      BudgetAnalysisSettings(
        timeFrame = TimeFrame(YearMonth(2025, 1), YearMonth(2025, 6), Static),
        conditions = persistentListOf(),
        conditionsOp = And,
        graphType = Bar,
        showBalance = true,
        balanceOnly = false,
        showHiddenCategories = false,
      )
  }
}
