package aktual.budget.reports.ui.report

import aktual.budget.reports.ui.Tags
import aktual.budget.reports.ui.charts.PREVIEW_CUSTOM_DATA
import aktual.budget.reports.vm.report.CustomReportState
import aktual.core.theme.DarkColors
import aktual.core.ui.PreviewWithColors
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.v2.runComposeUiTest
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import kotlin.test.Test

class CustomReportScaffoldTest {
  @Test
  fun `Viewing offers to edit, without settings`() = runComposeUiTest {
    // given
    val actions = mutableListOf<CustomReportAction>()
    setContent {
      PreviewWithColors(DarkColors) {
        CustomReportScaffold(state = state(isEditing = false), onAction = { actions += it })
      }
    }

    // when
    onNodeWithTag(Tags.EditReport).performClick()

    // then
    onAllNodesWithTag(Tags.CustomSetting).assertCountEquals(0)
    onNodeWithTag(Tags.RevertReport).assertDoesNotExist()
    assertThat(actions).containsExactly(CustomReportAction.StartEditing)
  }

  @Test
  fun `Revert and close send their actions`() = runComposeUiTest {
    // given
    val actions = mutableListOf<CustomReportAction>()
    setContent {
      PreviewWithColors(DarkColors) {
        CustomReportScaffold(
          state = state(isEditing = true, hasChanges = true),
          onAction = { actions += it },
        )
      }
    }

    // when
    onNodeWithTag(Tags.RevertReport).performClick()
    onNodeWithTag(Tags.CloseEditor).performClick()

    // then
    assertThat(actions).containsExactly(CustomReportAction.Revert, CustomReportAction.StopEditing)
  }

  @Test
  fun `Picking a graph applies its rules`() = runComposeUiTest {
    // given
    val actions = mutableListOf<CustomReportAction>()
    setContent {
      PreviewWithColors(DarkColors) {
        CustomReportScaffold(state = state(isEditing = true), onAction = { actions += it })
      }
    }

    // when
    onNodeWithText("Graph").performClick()
    onNodeWithText("Area").performClick()

    // then
    val edit = actions.single() as Edit
    val edited = edit.transform(PREVIEW_CUSTOM_DATA.config)
    assertThat(edited.graphType).isEqualTo(AreaGraph)
    assertThat(edited.groupBy).isEqualTo(Interval)
  }

  @Test
  fun `Total mode can't pick a line graph`() = runComposeUiTest {
    // given
    setContent {
      PreviewWithColors(DarkColors) {
        CustomReportScaffold(state = state(isEditing = true), onAction = {})
      }
    }

    // when
    onNodeWithText("Graph").performClick()

    // then
    onNodeWithText("Line").assertIsNotEnabled()
  }

  @Test
  fun `Switches toggle their option`() = runComposeUiTest {
    // given
    val actions = mutableListOf<CustomReportAction>()
    setContent {
      PreviewWithColors(DarkColors) {
        CustomReportScaffold(state = state(isEditing = true), onAction = { actions += it })
      }
    }

    // when
    onNodeWithText("Show empty rows").performScrollTo().performClick()

    // then
    val edit = actions.single() as Edit
    assertThat(edit.transform(PREVIEW_CUSTOM_DATA.config).showEmpty).isTrue()
  }

  private fun state(isEditing: Boolean, hasChanges: Boolean = false) =
    CustomReportState.Loaded(
      name = PREVIEW_CUSTOM_DATA.title,
      draft = PREVIEW_CUSTOM_DATA.config,
      preview = PREVIEW_CUSTOM_DATA,
      isEditing = isEditing,
      hasChanges = hasChanges,
    )
}
