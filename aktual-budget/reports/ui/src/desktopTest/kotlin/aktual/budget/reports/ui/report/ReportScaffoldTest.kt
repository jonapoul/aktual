package aktual.budget.reports.ui.report

import aktual.budget.model.WidgetId
import aktual.budget.reports.components.ChartTags
import aktual.budget.reports.components.PREVIEW_NET_WORTH_DATA
import aktual.budget.reports.ui.Action
import aktual.budget.reports.ui.Tags
import aktual.budget.reports.vm.NetWorthReportMeta
import aktual.budget.reports.vm.ReportMeta
import aktual.budget.reports.vm.TimeFrame
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
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.datetime.YearMonth

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
    onNodeWithTag(Tags.ReportMenu).performClick()
    onNodeWithTag(ChartTags.ChartInfo).performClick()

    // then
    onNodeWithTag(ChartTags.ChartInfoSheet).assertExists()
  }

  @Test
  fun `Settings sheet edits the net worth mode`() = runComposeUiTest {
    // given
    val actions = mutableListOf<Action>()
    setPreviewContent { ReportScaffold(state = STATE, onAction = { actions += it }) }

    // when
    onNodeWithTag(ChartTags.ChartSettings).performClick()
    onNodeWithText("Stacked").performClick()

    // then
    assertThat(actions.edited()).isEqualTo(NetWorthReportMeta(mode = Stacked))
  }

  @Test
  fun `Settings sheet edits the net worth time frame`() = runComposeUiTest {
    // given
    val actions = mutableListOf<Action>()
    setPreviewContent {
      ReportScaffold(state = STATE, editorData = PREVIEW_QUERY_DATA, onAction = { actions += it })
    }

    // when
    onNodeWithTag(ChartTags.ChartSettings).performClick()
    onNodeWithTag(Tags.TimeFrameRow).performClick()
    onNodeWithText("Last year").performClick()

    // then
    assertThat(actions.edited())
      .isEqualTo(
        NetWorthReportMeta(timeFrame = TimeFrame(YearMonth(2025, 6), YearMonth(2025, 6), LastYear)),
      )
  }

  @Test
  fun `Unsaved changes swap back for discard and show save`() = runComposeUiTest {
    // given
    val actions = mutableListOf<Action>()
    setPreviewContent { ReportScaffold(state = CHANGED, onAction = { actions += it }) }
    onNodeWithText("Unsaved changes").assertExists()

    // when
    onNodeWithTag(Tags.DiscardReport).performClick()
    onNodeWithTag(Tags.SaveReport).performClick()

    // then
    assertThat(actions).containsExactly(Action.NavBack, Action.SaveSettings)
  }

  @Test
  fun `No discard or save without changes`() = runComposeUiTest {
    // when
    setPreviewContent { ReportScaffold(state = STATE, onAction = {}) }

    // then
    onNodeWithTag(Tags.DiscardReport).assertDoesNotExist()
    onNodeWithTag(Tags.SaveReport).assertDoesNotExist()
  }

  // Applies each edit the scaffold sent to the saved meta
  private fun List<Action>.edited(): ReportMeta =
    filterIsInstance<Action.EditSettings>().fold(STATE.item.meta) { meta, edit ->
      edit.transform(meta)
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

    val CHANGED = STATE.copy(draft = NetWorthReportMeta(mode = Stacked), hasChanges = true)
  }
}
