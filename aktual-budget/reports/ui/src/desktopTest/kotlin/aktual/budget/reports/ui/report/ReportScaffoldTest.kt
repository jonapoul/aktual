package aktual.budget.reports.ui.report

import aktual.budget.model.WidgetId
import aktual.budget.reports.components.ChartTags
import aktual.budget.reports.components.PREVIEW_NET_WORTH_DATA
import aktual.budget.reports.ui.Action
import aktual.budget.reports.ui.Tags
import aktual.budget.reports.vm.NetWorthReportMeta
import aktual.budget.reports.vm.dashboard.DashboardItem
import aktual.budget.reports.vm.report.ReportState
import aktual.test.setPreviewContent
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runComposeUiTest
import assertk.assertThat
import assertk.assertions.containsExactly
import kotlin.test.Test

class ReportScaffoldTest {
  @Test
  fun `Reset zoom button shows in the top bar while zoomed`() = runComposeUiTest {
    // given
    setPreviewContent { ReportScaffold(state = STATE, onAction = {}) }
    onNodeWithTag(ChartTags.ResetZoom).assertDoesNotExist()

    // when zooming
    onNodeWithTag(ChartTags.ZoomableChart).performTouchInput {
      down(centerLeft.copy(x = width * 0.3f))
      advanceEventTime(LONG_PRESS_MS)
      moveTo(center.copy(x = width * 0.6f))
      up()
    }

    // then
    onNodeWithTag(ChartTags.ResetZoom).assertExists()

    // when resetting
    onNodeWithTag(ChartTags.ResetZoom).performClick()

    // then
    onNodeWithTag(ChartTags.ResetZoom).assertDoesNotExist()
  }

  @Test
  fun `Info button shows the chart explanation`() = runComposeUiTest {
    // given
    setPreviewContent { ReportScaffold(state = STATE, onAction = {}) }
    onNodeWithTag(ChartTags.ChartInfoSheet).assertDoesNotExist()

    // when
    onNodeWithTag(ChartTags.ChartInfo).performClick()

    // then
    onNodeWithTag(ChartTags.ChartInfoSheet).assertExists()
  }

  @Test
  fun `Menu renames the report`() = runComposeUiTest {
    // given
    val actions = mutableListOf<Action>()
    setPreviewContent { ReportScaffold(state = STATE, onAction = { actions += it }) }

    // when
    onNodeWithTag(Tags.ReportMenu).performClick()
    onNodeWithText("Rename").performClick()
    onNode(hasSetTextAction()).performTextReplacement("Wealth")
    onNodeWithText("Save").performClick()

    // then
    assertThat(actions).containsExactly(Action.Rename(STATE.item, "Wealth"))
  }

  private companion object {
    const val LONG_PRESS_MS = 1_000L

    val STATE =
      ReportState.Loaded(
        type = NetWorth,
        item = DashboardItem(id = WidgetId("test"), x = 0, y = 0, meta = NetWorthReportMeta()),
        data = PREVIEW_NET_WORTH_DATA,
      )
  }
}
