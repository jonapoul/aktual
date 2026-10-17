package aktual.budget.reports.ui.report

import aktual.budget.reports.ui.Tags
import aktual.budget.reports.ui.charts.PREVIEW_FORMULA_DATA
import aktual.budget.reports.vm.report.FormulaReportState
import aktual.budget.reports.vm.toEdits
import aktual.test.setPreviewContent
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.v2.runComposeUiTest
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isFalse
import kotlin.test.Test

class FormulaReportScaffoldTest {
  @Test
  fun `Save is disabled without changes`() = runComposeUiTest {
    // when
    setPreviewContent {
      FormulaReportScaffold(state = state(hasChanges = false), onAction = {})
    }

    // then
    onNodeWithTag(Tags.SaveReport).assertIsNotEnabled()
  }

  @Test
  fun `Save and close send their actions while editing`() = runComposeUiTest {
    // given
    val actions = mutableListOf<FormulaReportAction>()
    setPreviewContent {
      FormulaReportScaffold(state = state(hasChanges = true), onAction = { actions += it })
    }

    // when
    onNodeWithTag(Tags.SaveReport).assertIsEnabled().performClick()
    onNodeWithTag(Tags.CloseEditor).performClick()

    // then
    assertThat(actions).containsExactly(FormulaReportAction.Save, FormulaReportAction.StopEditing)
  }

  @Test
  fun `Tapping show title turns it off`() = runComposeUiTest {
    // given
    val actions = mutableListOf<FormulaReportAction>()
    setPreviewContent {
      FormulaReportScaffold(state = state(hasChanges = false), onAction = { actions += it })
    }

    // when
    onNodeWithTag(Tags.ShowTitle).performClick()

    // then
    val edit = actions.single() as FormulaReportAction.Edit
    assertThat(edit.transform(DRAFT).showTitle).isFalse()
  }

  @Test
  fun `Setting rows open their editors`() = runComposeUiTest {
    // given
    val actions = mutableListOf<FormulaReportAction>()
    setPreviewContent {
      FormulaReportScaffold(state = state(hasChanges = false), onAction = { actions += it })
    }

    // when
    onNodeWithText("Name").performClick()
    onNodeWithText("Formula").performClick()
    onNodeWithText("rule", substring = true).performClick()
    onNodeWithText("Font size").performClick()

    // then
    assertThat(actions)
      .containsExactly(
        FormulaReportAction.EditName,
        FormulaReportAction.EditFormula,
        FormulaReportAction.EditColorRule,
        FormulaReportAction.EditFontSize,
      )
  }

  @Test
  fun `Editing shows the name row and query counts`() = runComposeUiTest {
    // when
    setPreviewContent {
      FormulaReportScaffold(state = state(hasChanges = false), onAction = {})
    }

    // then
    onNodeWithText("Name").assertExists()
    onNodeWithText("3 conditions").assertExists()
    onNodeWithTag(Tags.EditReport).assertDoesNotExist()
  }

  @Test
  fun `Query rows open their query while editing`() = runComposeUiTest {
    // given
    val actions = mutableListOf<FormulaReportAction>()
    setPreviewContent {
      FormulaReportScaffold(state = state(hasChanges = false), onAction = { actions += it })
    }

    // when
    onNodeWithText("income").performScrollTo().performClick()

    // then
    assertThat(actions).containsExactly(FormulaReportAction.EditQuery("income"))
  }

  @Test
  fun `Add query waits for the editor data`() = runComposeUiTest {
    // when
    setPreviewContent {
      FormulaReportScaffold(state = state(hasChanges = false), onAction = {})
    }

    // then
    onNodeWithTag(Tags.AddQuery).performScrollTo().assertIsNotEnabled()
  }

  @Test
  fun `Add query sends its action while editing`() = runComposeUiTest {
    // given
    val actions = mutableListOf<FormulaReportAction>()
    setPreviewContent {
      FormulaReportScaffold(
        state = state(hasChanges = false),
        queryEditorData = PREVIEW_QUERY_DATA,
        onAction = { actions += it },
      )
    }

    // when
    onNodeWithTag(Tags.AddQuery).performScrollTo().performClick()

    // then
    assertThat(actions).containsExactly(FormulaReportAction.AddQuery)
  }

  private fun state(hasChanges: Boolean) =
    FormulaReportState.Loaded(
      draft = DRAFT,
      preview = PREVIEW_FORMULA_DATA,
      isEditing = true,
      hasChanges = hasChanges,
    )

  private companion object {
    val DRAFT = PREVIEW_FORMULA_META.toEdits()
  }
}
