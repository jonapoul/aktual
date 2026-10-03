package aktual.budget.reports.ui.montecarlo

import aktual.budget.reports.ui.charts.successColor
import aktual.budget.reports.vm.HISTORICAL_FIRST_YEAR
import aktual.budget.reports.vm.HISTORICAL_LAST_YEAR
import aktual.budget.reports.vm.ReturnModel
import aktual.budget.reports.vm.montecarlo.MonteCarloGraphView
import aktual.budget.reports.vm.montecarlo.MonteCarloResults
import aktual.budget.reports.vm.montecarlo.MonteCarloResultsView
import aktual.budget.reports.vm.montecarlo.MonteCarloState
import aktual.budget.reports.vm.montecarlo.RunPercentile
import aktual.core.l10n.Strings
import aktual.core.model.percent
import aktual.core.ui.AktualExposedDropDownMenu
import aktual.core.ui.AktualSlidingToggleButton
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.formattedString
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.toImmutableList

@Composable
internal fun MonteCarloResults(
  state: MonteCarloState.Loaded,
  onAction: MonteCarloActionHandler,
  modifier: Modifier = Modifier,
) =
  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
    val results = state.results
    if (results == null) {
      SectionCard {
        Row(
          verticalAlignment = CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          CircularProgressIndicator(color = colors.pageText)
          BodyText(Strings.monteCarloSimulating)
        }
      }
    } else {
      HeadlineStats(results)
      ResultsViews(state, results, onAction)
      DepletionCard(results)
    }

    HowItWorks(state.config.returnModel)
  }

@Composable
private fun HeadlineStats(results: MonteCarloResults, modifier: Modifier = Modifier) =
  SectionCard(modifier = modifier) {
    FlowRow(
      horizontalArrangement = Arrangement.spacedBy(32.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Stat(
        heading = Strings.monteCarloStatSuccess,
        value = results.successRate.toString(decimalPlaces = 1),
        color = successColor(results.successRate),
        isLarge = true,
      )
      Stat(Strings.monteCarloStatMedianEnding, results.medianEndingBalance.formattedString())
      Stat(Strings.monteCarloStatMedianWithdrawn, results.medianTotalWithdrawn.formattedString())
      Stat(
        Strings.monteCarloStatDepletionChance,
        results.depletionChance.toString(decimalPlaces = 1),
      )
      results.medianDepletionAge?.let { age ->
        Stat(Strings.monteCarloStatTypicalFailure, Strings.monteCarloAge(age))
      }
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
      GroupHeading(Strings.monteCarloSummaryTitle)
      BodyText(
        Strings.monteCarloSummary(
          results.successRate.toString(decimalPlaces = 1),
          results.simulationCount,
          results.endAge,
        )
      )
    }
  }

@Composable
private fun Stat(
  heading: String,
  value: String,
  modifier: Modifier = Modifier,
  color: Color = colors.pageText,
  isLarge: Boolean = false,
) =
  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
    GroupHeading(heading)
    Text(
      text = value,
      style = if (isLarge) typography.headlineMedium else typography.titleMedium,
      fontWeight = if (isLarge) FontWeight.Normal else FontWeight.Medium,
      color = color,
    )
  }

@Composable
private fun ResultsViews(
  state: MonteCarloState.Loaded,
  results: MonteCarloResults,
  onAction: MonteCarloActionHandler,
  modifier: Modifier = Modifier,
) =
  SectionCard(modifier = modifier) {
    SectionTitle(
      when (state.resultsView) {
        Chart -> Strings.monteCarloHeadingPerformance
        Cashflow -> Strings.monteCarloHeadingCashflow
        Runs -> Strings.monteCarloHeadingRuns
      }
    )

    AktualSlidingToggleButton(
      selected = state.resultsView,
      options = MonteCarloResultsView.entries.toImmutableList(),
      onSelect = { onAction(MonteCarloAction.SetResultsView(it)) },
      string = { it.string() },
    )

    when (state.resultsView) {
      Chart -> {
        PerformanceChart(state.graphView, results, onAction)
      }

      Cashflow -> {
        CashflowView(state, onAction)
      }

      Runs -> {
        val detail = state.runDetail
        val selected = state.selectedRun
        if (selected != null && detail != null && detail.index == selected) {
          RunDetailView(
            detail = detail,
            config = state.config,
            simulationCount = results.simulationCount,
            onBack = { onAction(MonteCarloAction.SelectRun(null)) },
          )
        } else {
          RunsTable(
            runs = results.runs,
            onSelectRun = { onAction(MonteCarloAction.SelectRun(it)) },
          )
        }
      }
    }
  }

@Composable
private fun PerformanceChart(
  view: MonteCarloGraphView,
  results: MonteCarloResults,
  onAction: MonteCarloActionHandler,
) {
  AktualExposedDropDownMenu(
    value = view,
    onValueChange = { onAction(MonteCarloAction.SetGraphView(it)) },
    options = MonteCarloGraphView.entries.toImmutableList(),
    string = { it.string() },
  )

  val description =
    when (view) {
      All -> null
      SingleWorst -> Strings.monteCarloGraphDescSingleWorst(results.simulationCount)
      WorstCase -> Strings.monteCarloGraphDescShare(SHARE_ABOVE_P5.percent.toString())
      Pessimistic -> Strings.monteCarloGraphDescShare(SHARE_ABOVE_P30.percent.toString())
      Median -> Strings.monteCarloGraphDescMedian
      Optimistic -> Strings.monteCarloGraphDescShare(SHARE_ABOVE_P70.percent.toString())
    }
  description?.let { BodyText(it) }

  FanChart(
    modifier = Modifier.fillMaxWidth().height(CHART_HEIGHT.dp),
    bands = results.bands,
    view = view,
  )
}

@Composable
internal fun RunPercentilePicker(
  value: RunPercentile,
  onValueChange: (RunPercentile) -> Unit,
  modifier: Modifier = Modifier,
) =
  AktualExposedDropDownMenu(
    modifier = modifier,
    value = value,
    onValueChange = onValueChange,
    options = RunPercentile.entries.toImmutableList(),
    string = { it.string() },
  )

@Composable
private fun DepletionCard(results: MonteCarloResults, modifier: Modifier = Modifier) =
  SectionCard(modifier = modifier) {
    SectionTitle(Strings.monteCarloHistogramTitle)

    if (results.failedCount == 0) {
      BodyText(Strings.monteCarloHistogramNone)
      return@SectionCard
    }

    BodyText(
      Strings.monteCarloHistogramFailures(
        results.failedCount,
        results.simulationCount,
        results.simulationCount - results.failedCount,
      )
    )

    var selectedAge by remember(results) { mutableStateOf<Int?>(null) }
    DepletionHistogram(
      modifier = Modifier.fillMaxWidth().height(HISTOGRAM_HEIGHT.dp),
      depletions = results.depletions,
      medianDepletionAge = results.medianDepletionAge,
      onSelectAge = { selectedAge = it },
    )

    val selected = selectedAge?.let { age -> results.depletions.firstOrNull { it.age == age } }
    Box(modifier = Modifier.fillMaxWidth()) {
      if (selected != null) {
        Text(
          text =
            Strings.monteCarloHistogramSelected(
              selected.count,
              results.simulationCount,
              selected.age,
            ),
          style = typography.bodyMedium,
          fontWeight = FontWeight.Medium,
          color = colors.pageText,
        )
      }
    }

    BodyText(
      Strings.monteCarloHistogramSummary(
        results.earliestDepletionAge ?: results.currentAge,
        results.medianDepletionAge ?: results.currentAge,
        results.latestDepletionAge ?: results.currentAge,
      )
    )
  }

@Composable
private fun HowItWorks(
  returnModel: ReturnModel,
  modifier: Modifier = Modifier,
) =
  SectionCard(modifier = modifier) {
    SectionTitle(Strings.reportsMonteCarloHowTitle)
    BodyText(Strings.monteCarloHow)
    BodyText(
      when (returnModel) {
        HistoricalBootstrap ->
          Strings.monteCarloHowBootstrap(HISTORICAL_FIRST_YEAR, HISTORICAL_LAST_YEAR)
        HistoricalSequence ->
          Strings.monteCarloHowSequence(HISTORICAL_FIRST_YEAR, HISTORICAL_LAST_YEAR)
        Normal,
        Unknown -> Strings.monteCarloHowNormal
      }
    )
  }

private const val CHART_HEIGHT = 360
private const val HISTOGRAM_HEIGHT = 200
private const val SHARE_ABOVE_P5 = 95
private const val SHARE_ABOVE_P30 = 70
private const val SHARE_ABOVE_P70 = 30
