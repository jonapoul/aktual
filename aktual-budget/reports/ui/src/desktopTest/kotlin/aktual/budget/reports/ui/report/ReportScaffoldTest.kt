package aktual.budget.reports.ui.report

import aktual.budget.model.WidgetId
import aktual.budget.reports.ui.Action
import aktual.budget.reports.ui.Tags
import aktual.budget.reports.ui.charts.PREVIEW_FORMULA_DATA
import aktual.budget.reports.ui.charts.PREVIEW_NET_WORTH_DATA
import aktual.budget.reports.vm.NetWorthReportMeta
import aktual.budget.reports.vm.dashboard.DashboardItem
import aktual.budget.reports.vm.report.ReportState
import aktual.core.theme.DarkColors
import aktual.core.ui.PreviewWithColors
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runComposeUiTest
import assertk.assertThat
import assertk.assertions.containsExactly
import kotlin.test.Test

class ReportScaffoldTest {
  @Test
  fun `Reset zoom button shows in the top bar while zoomed`() = runComposeUiTest {
    // given
    setContent { PreviewWithColors(DarkColors) { ReportScaffold(state = STATE, onAction = {}) } }
    onNodeWithTag(Tags.ResetZoom).assertDoesNotExist()

    // when zooming
    onNodeWithTag(Tags.ZoomableChart).performTouchInput {
      down(centerLeft.copy(x = width * 0.3f))
      advanceEventTime(LONG_PRESS_MS)
      moveTo(center.copy(x = width * 0.6f))
      up()
    }

    // then
    onNodeWithTag(Tags.ResetZoom).assertExists()

    // when resetting
    onNodeWithTag(Tags.ResetZoom).performClick()

    // then
    onNodeWithTag(Tags.ResetZoom).assertDoesNotExist()
  }

  @Test
  fun `Info button shows the chart explanation`() = runComposeUiTest {
    // given
    setContent { PreviewWithColors(DarkColors) { ReportScaffold(state = STATE, onAction = {}) } }
    onNodeWithTag(Tags.ChartInfoSheet).assertDoesNotExist()

    // when
    onNodeWithTag(Tags.ChartInfo).performClick()

    // then
    onNodeWithTag(Tags.ChartInfoSheet).assertExists()
  }

  @Test
  fun `Edit button on a formula card sends the edit action`() = runComposeUiTest {
    // given
    val actions = mutableListOf<Action>()
    setContent {
      PreviewWithColors(DarkColors) {
        ReportScaffold(state = FORMULA_STATE, onAction = { actions += it })
      }
    }

    // when
    onNodeWithTag(Tags.EditReport).performClick()

    // then
    assertThat(actions).containsExactly(Action.EditReport)
  }

  @Test
  fun `Edit button only shows for formula cards`() = runComposeUiTest {
    // when
    setContent { PreviewWithColors(DarkColors) { ReportScaffold(state = STATE, onAction = {}) } }

    // then
    onNodeWithTag(Tags.EditReport).assertDoesNotExist()
  }

  private companion object {
    const val LONG_PRESS_MS = 1_000L

    val STATE =
      ReportState.Loaded(
        type = NetWorth,
        item = DashboardItem(id = WidgetId("test"), x = 0, y = 0, meta = NetWorthReportMeta()),
        data = PREVIEW_NET_WORTH_DATA,
      )

    val FORMULA_STATE =
      ReportState.Loaded(
        type = Formula,
        item = DashboardItem(id = WidgetId("test"), x = 0, y = 0, meta = PREVIEW_FORMULA_META),
        data = PREVIEW_FORMULA_DATA,
      )
  }
}
