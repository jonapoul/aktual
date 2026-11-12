package aktual.budget.reports.ui.report

import aktual.budget.model.WidgetId
import aktual.budget.reports.components.ChartTags
import aktual.budget.reports.components.PREVIEW_AGE_OF_MONEY_DATA
import aktual.budget.reports.components.PREVIEW_BALANCE_FORECAST_DATA
import aktual.budget.reports.components.PREVIEW_BUDGET_ANALYSIS_DATA
import aktual.budget.reports.components.PREVIEW_CASH_FLOW_DATA
import aktual.budget.reports.components.PREVIEW_CROSSOVER_DATA
import aktual.budget.reports.components.PREVIEW_NET_WORTH_DATA
import aktual.budget.reports.components.PREVIEW_SANKEY_DATA
import aktual.budget.reports.ui.Action
import aktual.budget.reports.ui.Tags
import aktual.budget.reports.vm.ALL_SANKEY_CATEGORIES
import aktual.budget.reports.vm.AgeOfMoneyReportMeta
import aktual.budget.reports.vm.BalanceForecastReportMeta
import aktual.budget.reports.vm.BudgetAnalysisReportMeta
import aktual.budget.reports.vm.CashFlowReportMeta
import aktual.budget.reports.vm.CrossoverReportMeta
import aktual.budget.reports.vm.NetWorthReportMeta
import aktual.budget.reports.vm.ReportMeta
import aktual.budget.reports.vm.SankeyLayer
import aktual.budget.reports.vm.SankeyReportMeta
import aktual.budget.reports.vm.SummaryData
import aktual.budget.reports.vm.SummaryReportMeta
import aktual.budget.reports.vm.TimeFrame
import aktual.budget.reports.vm.dashboard.DashboardItem
import aktual.budget.reports.vm.report.ReportState
import aktual.budget.reports.vm.summaryType
import aktual.budget.rules.components.ConditionTags
import aktual.budget.rules.domain.newFilterCondition
import aktual.core.ui.Tags as CoreTags
import aktual.test.setPreviewContent
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runComposeUiTest
import assertk.all
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
      down(0, center.copy(x = width * 0.4f))
      down(1, center.copy(x = width * 0.6f))
      moveTo(0, center.copy(x = width * 0.2f))
      moveTo(1, center.copy(x = width * 0.8f))
      up(0)
      up(1)
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
  fun `Settings sheet edits the net worth interval`() = runComposeUiTest {
    // given
    val actions = mutableListOf<Action>()
    setPreviewContent { ReportScaffold(state = STATE, onAction = { actions += it }) }

    // when
    onNodeWithTag(ChartTags.ChartSettings).performClick()
    onNodeWithText("Yearly").performClick()

    // then
    assertThat(actions.edited())
      .isInstanceOf<NetWorthReportMeta>()
      .prop(NetWorthReportMeta::interval)
      .isEqualTo(Yearly)
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
  fun `Done on the time frame picker goes back to the settings`() = runComposeUiTest {
    // given
    setPreviewContent {
      ReportScaffold(state = ANALYSIS, editorData = PREVIEW_QUERY_DATA, onAction = {})
    }
    onNodeWithTag(ChartTags.ChartSettings).performClick()
    onNodeWithTag(Tags.TimeFrameRow).performClick()
    onNodeWithText("Last year").assertExists()
    back().assertExists()

    // when
    onNodeWithTag(ConditionTags.QuerySheetDone).performSemanticsAction(SemanticsActions.OnClick)
    waitForIdle()

    // then
    onNodeWithText("Chart settings").assertExists()
    onNodeWithText("Last year").assertDoesNotExist()
    back().assertDoesNotExist()
  }

  @Test
  fun `Back goes from a condition's entity list to the condition, then the settings`() =
    runComposeUiTest {
      // given
      val state = ANALYSIS.copy(draft = ANALYSIS_META.copy(conditions = listOf(CATEGORY_CONDITION)))
      setPreviewContent {
        ReportScaffold(state = state, editorData = PREVIEW_QUERY_DATA, onAction = {})
      }
      onNodeWithTag(ChartTags.ChartSettings).performClick()
      onNodeWithTag(ConditionTags.ConditionRow).performScrollTo().performClick()
      onNodeWithText("Edit condition").assertExists()
      onNodeWithText("Nothing").performSemanticsAction(SemanticsActions.OnClick)
      waitForIdle()
      onNodeWithText("Food").assertExists()

      // when
      back().performSemanticsAction(SemanticsActions.OnClick)
      waitForIdle()

      // then
      onNodeWithText("Food").assertDoesNotExist()
      onNodeWithText("Edit condition").assertExists()

      // when
      back().performSemanticsAction(SemanticsActions.OnClick)
      waitForIdle()

      // then
      onNodeWithText("Edit condition").assertDoesNotExist()
      onNodeWithText("Chart settings").assertExists()
      back().assertDoesNotExist()
    }

  private fun ComposeUiTest.back() = onNodeWithTag(CoreTags.SheetBack)

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

  @Test
  fun `Settings sheet hides the cash flow balance`() = runComposeUiTest {
    // given
    val actions = mutableListOf<Action>()
    setPreviewContent { ReportScaffold(state = CASH_FLOW, onAction = { actions += it }) }

    // when
    onNodeWithTag(ChartTags.ChartSettings).performClick()
    onNodeWithText("Show balance").performScrollTo().performClick()

    // then
    assertThat(actions.edited(CASH_FLOW))
      .isInstanceOf<CashFlowReportMeta>()
      .prop(CashFlowReportMeta::showBalance)
      .isEqualTo(false)
  }

  @Test
  fun `Settings sheet turns on crossover custom growth at 6 percent`() = runComposeUiTest {
    // given
    val actions = mutableListOf<Action>()
    setPreviewContent { ReportScaffold(state = CROSSOVER, onAction = { actions += it }) }

    // when
    onNodeWithTag(ChartTags.ChartSettings).performClick()
    onNodeWithText("Use custom growth projections").performScrollTo().performClick()

    // then
    assertThat(actions.edited(CROSSOVER)).isInstanceOf<CrossoverReportMeta>().all {
      prop(CrossoverReportMeta::estimatedReturn).isEqualTo(0.06)
      prop(CrossoverReportMeta::expectedContribution).isEqualTo(0.0)
    }
  }

  @Test
  fun `Settings sheet picks a crossover projection type`() = runComposeUiTest {
    // given
    val actions = mutableListOf<Action>()
    setPreviewContent { ReportScaffold(state = CROSSOVER, onAction = { actions += it }) }

    // when
    onNodeWithTag(ChartTags.ChartSettings).performClick()
    onNodeWithText("Expense projection").performScrollTo().performClick()
    onNodeWithText("Mean").performClick()

    // then
    assertThat(actions.edited(CROSSOVER))
      .isInstanceOf<CrossoverReportMeta>()
      .prop(CrossoverReportMeta::projectionType)
      .isEqualTo(Mean)
  }

  @Test
  fun `Settings sheet switching sankey to budgeted moves the layers off payees`() =
    runComposeUiTest {
      // given
      val actions = mutableListOf<Action>()
      setPreviewContent { ReportScaffold(state = SANKEY, onAction = { actions += it }) }

      // when
      onNodeWithTag(ChartTags.ChartSettings).performClick()
      onNodeWithText("Budgeted").performScrollTo().performClick()

      // then
      assertThat(actions.edited(SANKEY)).isInstanceOf<SankeyReportMeta>().all {
        prop(SankeyReportMeta::mode).isEqualTo(Budgeted)
        prop(SankeyReportMeta::layerFrom).isEqualTo(SankeyLayer.IncomeCategory.key)
        prop(SankeyReportMeta::layerTo).isEqualTo(SankeyLayer.Category.key)
      }
    }

  @Test
  fun `Settings sheet picks a sankey category limit`() = runComposeUiTest {
    // given
    val actions = mutableListOf<Action>()
    setPreviewContent { ReportScaffold(state = SANKEY, onAction = { actions += it }) }

    // when
    onNodeWithTag(ChartTags.ChartSettings).performClick()
    onNodeWithText("Show up to").performScrollTo().performClick()
    onNodeWithText("All").performClick()

    // then
    assertThat(actions.edited(SANKEY))
      .isInstanceOf<SankeyReportMeta>()
      .prop(SankeyReportMeta::topNCategories)
      .isEqualTo(ALL_SANKEY_CATEGORIES)
  }

  @Test
  fun `Settings sheet picks a daily balance forecast`() = runComposeUiTest {
    // given
    val actions = mutableListOf<Action>()
    setPreviewContent { ReportScaffold(state = FORECAST, onAction = { actions += it }) }

    // when
    onNodeWithTag(ChartTags.ChartSettings).performClick()
    onNodeWithText("Daily").performScrollTo().performClick()

    // then
    assertThat(actions.edited(FORECAST))
      .isInstanceOf<BalanceForecastReportMeta>()
      .prop(BalanceForecastReportMeta::granularity)
      .isEqualTo(Daily)
  }

  @Test
  fun `Settings sheet picks an age of money granularity`() = runComposeUiTest {
    // given
    val actions = mutableListOf<Action>()
    setPreviewContent { ReportScaffold(state = AGE_OF_MONEY, onAction = { actions += it }) }

    // when
    onNodeWithTag(ChartTags.ChartSettings).performClick()
    onNodeWithText("Weekly").performClick()

    // then
    assertThat(actions.edited(AGE_OF_MONEY))
      .isInstanceOf<AgeOfMoneyReportMeta>()
      .prop(AgeOfMoneyReportMeta::granularity)
      .isEqualTo(Weekly)
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

    val CATEGORY_CONDITION = newFilterCondition(Category)

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

    val CASH_FLOW =
      ReportState.Loaded(
        type = CashFlow,
        item =
          DashboardItem(
            id = WidgetId("cash"),
            x = 0,
            y = 0,
            meta = CashFlowReportMeta(showBalance = true),
          ),
        data = PREVIEW_CASH_FLOW_DATA,
      )

    val AGE_OF_MONEY =
      ReportState.Loaded(
        type = AgeOfMoney,
        item =
          DashboardItem(
            id = WidgetId("age"),
            x = 0,
            y = 0,
            meta = AgeOfMoneyReportMeta(granularity = Monthly),
          ),
        data = PREVIEW_AGE_OF_MONEY_DATA,
      )

    val CROSSOVER =
      ReportState.Loaded(
        type = Crossover,
        item =
          DashboardItem(id = WidgetId("crossover"), x = 0, y = 0, meta = CrossoverReportMeta()),
        data = PREVIEW_CROSSOVER_DATA,
      )

    val FORECAST =
      ReportState.Loaded(
        type = BalanceForecast,
        item =
          DashboardItem(
            id = WidgetId("forecast"),
            x = 0,
            y = 0,
            meta = BalanceForecastReportMeta(),
          ),
        data = PREVIEW_BALANCE_FORECAST_DATA,
      )

    val SANKEY =
      ReportState.Loaded(
        type = Sankey,
        item = DashboardItem(id = WidgetId("sankey"), x = 0, y = 0, meta = SankeyReportMeta()),
        data = PREVIEW_SANKEY_DATA,
      )

    val ANALYSIS =
      ReportState.Loaded(
        type = BudgetAnalysis,
        item = DashboardItem(id = WidgetId("analysis"), x = 0, y = 0, meta = ANALYSIS_META),
        data = PREVIEW_BUDGET_ANALYSIS_DATA,
      )
  }
}
