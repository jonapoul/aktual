package aktual.budget.reports.ui.charts

import aktual.budget.reports.ui.Tags
import aktual.budget.reports.vm.AgeOfMoneyData
import aktual.budget.reports.vm.BalanceForecastData
import aktual.budget.reports.vm.CalendarData
import aktual.budget.reports.vm.CashFlowData
import aktual.budget.reports.vm.ChartData
import aktual.budget.reports.vm.CrossoverData
import aktual.budget.reports.vm.CustomData
import aktual.budget.reports.vm.MonteCarloData
import aktual.budget.reports.vm.NetWorthData
import aktual.budget.reports.vm.SankeyData
import aktual.budget.reports.vm.SpendingData
import aktual.budget.reports.vm.SummaryData
import aktual.budget.reports.vm.TextData
import aktual.budget.reports.vm.UnsupportedData
import aktual.core.l10n.Strings
import aktual.core.ui.AktualModalBottomSheet
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BottomSpacing
import aktual.core.ui.verticalScrollWithBar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable internal data class ChartInfo(val title: String, val text: String)

// Empty if the chart has nothing to explain
@Composable
internal fun ChartData.info(): ImmutableList<ChartInfo> =
  when (this) {
    is AgeOfMoneyData ->
      persistentListOf(
        ChartInfo(Strings.reportsAgeOfMoneyWhatTitle, Strings.reportsAgeOfMoneyWhat),
        ChartInfo(Strings.reportsAgeOfMoneyHowTitle, Strings.reportsAgeOfMoneyHow),
      )

    is BalanceForecastData ->
      persistentListOf(
        ChartInfo(
          title = Strings.reportsBalanceForecastHowTitle,
          text =
            if (source == TrackingBudget) {
              Strings.reportsBalanceForecastTrackingHow
            } else {
              Strings.reportsBalanceForecastHow
            },
        ),
      )

    is CashFlowData ->
      persistentListOf(
        ChartInfo(Strings.reportsCashFlowFooterTitle, Strings.reportsCashFlowFooter),
      )

    is CrossoverData ->
      persistentListOf(ChartInfo(Strings.reportsCrossoverWhatTitle, Strings.reportsCrossoverWhat))

    is MonteCarloData ->
      persistentListOf(ChartInfo(Strings.reportsMonteCarloHowTitle, Strings.reportsMonteCarloHow))

    is NetWorthData ->
      persistentListOf(
        ChartInfo(Strings.reportsNetWorthFooterTitle, Strings.reportsNetWorthFooter),
      )

    is SpendingData ->
      persistentListOf(
        ChartInfo(Strings.reportsSpendingFooterTitle, Strings.reportsSpendingFooter),
      )

    is CalendarData,
    is CustomData,
    is SankeyData,
    is SummaryData,
    is TextData,
    is UnsupportedData -> persistentListOf()
  }

@Composable
internal fun ChartInfoSheet(
  info: ImmutableList<ChartInfo>,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) =
  AktualModalBottomSheet(
    modifier = modifier,
    onDismissRequest = onDismiss,
  ) {
    Column(
      modifier =
        Modifier.testTag(Tags.ChartInfoSheet)
          .fillMaxWidth()
          .verticalScrollWithBar()
          .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      info.fastForEach { item ->
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(text = item.title, fontWeight = Bold, style = typography.bodyLarge)
          Text(text = item.text, style = typography.bodyMedium)
        }
      }

      BottomSpacing()
    }
  }
