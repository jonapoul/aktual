package aktual.budget.reports.ui.report

import aktual.budget.reports.ui.Tags
import aktual.budget.reports.ui.charts.PREVIEW_FORMULA_DATA
import aktual.budget.reports.vm.report.EditFormulaReportState
import aktual.budget.reports.vm.toEdits
import aktual.core.theme.DarkColors
import aktual.core.ui.PreviewWithColors
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isFalse
import kotlin.test.Test

class EditFormulaReportScaffoldTest {
  @Test
  fun `Save is disabled without changes`() = runComposeUiTest {
    // when
    setContent {
      PreviewWithColors(DarkColors) {
        EditFormulaReportScaffold(state = state(hasChanges = false), onAction = {})
      }
    }

    // then
    onNodeWithTag(Tags.SaveReport).assertIsNotEnabled()
  }

  @Test
  fun `Save and close send their actions`() = runComposeUiTest {
    // given
    val actions = mutableListOf<EditFormulaAction>()
    setContent {
      PreviewWithColors(DarkColors) {
        EditFormulaReportScaffold(state = state(hasChanges = true), onAction = { actions += it })
      }
    }

    // when
    onNodeWithTag(Tags.SaveReport).assertIsEnabled().performClick()
    onNodeWithTag(Tags.CloseEditor).performClick()

    // then
    assertThat(actions).containsExactly(EditFormulaAction.Save, EditFormulaAction.Close)
  }

  @Test
  fun `Tapping show title turns it off`() = runComposeUiTest {
    // given
    val actions = mutableListOf<EditFormulaAction>()
    setContent {
      PreviewWithColors(DarkColors) {
        EditFormulaReportScaffold(state = state(hasChanges = false), onAction = { actions += it })
      }
    }

    // when
    onNodeWithTag(Tags.ShowTitle).performClick()

    // then
    val edit = actions.single() as EditFormulaAction.Edit
    assertThat(edit.transform(DRAFT).showTitle).isFalse()
  }

  private fun state(hasChanges: Boolean) =
    EditFormulaReportState.Loaded(
      draft = DRAFT,
      preview = PREVIEW_FORMULA_DATA,
      hasChanges = hasChanges,
    )

  private companion object {
    val DRAFT = PREVIEW_FORMULA_META.toEdits()
  }
}
