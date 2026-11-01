package aktual.budget.reports.ui.report

import aktual.budget.model.WidgetId
import aktual.budget.reports.components.ChartTags
import aktual.budget.reports.components.PREVIEW_BUDGET_ANALYSIS_DATA
import aktual.budget.reports.components.PREVIEW_NET_WORTH_DATA
import aktual.budget.reports.ui.Action
import aktual.budget.reports.ui.Tags
import aktual.budget.reports.vm.BudgetAnalysisReportMeta
import aktual.budget.reports.vm.NetWorthReportMeta
import aktual.budget.reports.vm.ReportMeta
import aktual.budget.reports.vm.SummaryData
import aktual.budget.reports.vm.SummaryReportMeta
import aktual.budget.reports.vm.TimeFrame
import aktual.budget.reports.vm.dashboard.DashboardItem
import aktual.budget.reports.vm.report.ReportState
import aktual.budget.reports.vm.summaryType
import aktual.test.setPreviewContent
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runComposeUiTest
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.prop
import kotlin.test.Test
import kotlinx.datetime.LocalDate
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

  @Test
  fun `Settings sheet picks a summary type`() = runComposeUiTest {
    // given
    val actions = mutableListOf<Action>()
    setPreviewContent { ReportScaffold(state = SUMMARY, onAction = { actions += it }) }

    // when
    onNodeWithTag(ChartTags.ChartSettings).performClick()
    onNodeWithText("Show as").performClick()
    onNodeWithText("Percentage").performClick()

    // then
    assertThat(actions.edited(SUMMARY))
      .isInstanceOf<SummaryReportMeta>()
      .prop(SummaryReportMeta::summaryType)
      .isEqualTo(Percentage)
  }

  @Test
  fun `Settings sheet picks a budget analysis graph`() = runComposeUiTest {
    // given
    val actions = mutableListOf<Action>()
    setPreviewContent { ReportScaffold(state = ANALYSIS, onAction = { actions += it }) }

    // when
    onNodeWithTag(ChartTags.ChartSettings).performClick()
    onNodeWithText("Graph").performClick()
    onNodeWithText("Line").performClick()

    // then
    assertThat(actions.edited(ANALYSIS))
      .isInstanceOf<BudgetAnalysisReportMeta>()
      .prop(BudgetAnalysisReportMeta::graphType)
      .isEqualTo(Line)
  }

  @Test
  fun `Settings sheet toggles budget analysis switches`() = runComposeUiTest {
    // given
    val actions = mutableListOf<Action>()
    setPreviewContent { ReportScaffold(state = ANALYSIS, onAction = { actions += it }) }

    // when
    onNodeWithTag(ChartTags.ChartSettings).performClick()
    onNodeWithText("Show balance").performScrollTo().performClick()

    // then
    assertThat(actions.edited(ANALYSIS))
      .isInstanceOf<BudgetAnalysisReportMeta>()
      .prop(BudgetAnalysisReportMeta::showBalance)
      .isEqualTo(false)
  }

  // Applies each edit the scaffold sent to the draft
  private fun List<Action>.edited(state: ReportState.Loaded = STATE): ReportMeta =
    filterIsInstance<Action.EditSettings>().fold(state.draft) { meta, edit -> edit.transform(meta) }

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

    val SUMMARY =
      ReportState.Loaded(
        type = Summary,
        item = DashboardItem(id = WidgetId("summary"), x = 0, y = 0, meta = SummaryReportMeta()),
        data =
          SummaryData.Sum(
            title = "Summary",
            start = LocalDate(2025, 1, 1),
            end = LocalDate(2025, 6, 30),
            value = Zero,
          ),
      )

    // As the view model makes it, with upstream's defaults filled in
    val ANALYSIS_META =
      BudgetAnalysisReportMeta(
        timeFrame = TimeFrame(YearMonth(2025, 1), YearMonth(2025, 6), Static),
        conditions = emptyList(),
        conditionsOp = And,
        graphType = Bar,
        showBalance = true,
        balanceOnly = false,
        showHiddenCategories = false,
      )

    val ANALYSIS =
      ReportState.Loaded(
        type = BudgetAnalysis,
        item = DashboardItem(id = WidgetId("analysis"), x = 0, y = 0, meta = ANALYSIS_META),
        data = PREVIEW_BUDGET_ANALYSIS_DATA,
      )
  }
}
