package aktual.budget.reports.ui.charts

import aktual.budget.model.Amount
import aktual.budget.model.BalanceType
import aktual.budget.model.CategoryId
import aktual.budget.model.GraphType
import aktual.budget.model.GroupBy
import aktual.budget.reports.vm.CustomData
import aktual.budget.reports.vm.CustomGroup
import aktual.budget.reports.vm.CustomGroupIntervalTotals
import aktual.budget.reports.vm.CustomGroupTotals
import aktual.budget.reports.vm.CustomInterval
import aktual.budget.reports.vm.CustomIntervalTotals
import aktual.budget.reports.vm.CustomMetric
import aktual.budget.reports.vm.CustomReportConfig
import aktual.budget.reports.vm.CustomReportDates
import aktual.budget.reports.vm.CustomTotals
import aktual.budget.reports.vm.metric
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.CardShape
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.formattedText
import aktual.core.ui.verticalScrollWithBar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import kotlinx.datetime.number
import kotlinx.datetime.yearMonth

@Composable
internal fun CustomChart(
  data: CustomData,
  compact: Boolean,
  modifier: Modifier = Modifier,
  includeHeader: Boolean = true,
) =
  Column(modifier = modifier) {
    if (includeHeader) {
      if (compact) {
        CompactHeader(modifier = Modifier.padding(4.dp).fillMaxWidth(), data = data)
      } else {
        RegularHeader(modifier = Modifier.padding(4.dp).fillMaxWidth(), data = data)
      }
    }

    val bars = customBars(data)
    val chartModifier = Modifier.fillMaxWidth().weight(1f)
    val graphType = data.config.graphType
    val isIntervalStack = graphType == StackedBarGraph && data.config.groupBy == Interval
    when {
      graphType != BarGraph && graphType != StackedBarGraph || isIntervalStack ->
        Message(Strings.reportsCustomGraphUnsupported, colors.warningText, chartModifier)

      bars.isEmpty() || data.intervals.isEmpty() ->
        Message(Strings.reportsCustomEmpty, colors.pageTextSubdued, chartModifier)

      graphType == BarGraph -> CustomBarChart(data, bars, compact, chartModifier)

      else -> CustomStackedBarChart(data, bars, compact, chartModifier)
    }

    if (!compact && data.config.groupBy != Interval && bars.isNotEmpty()) {
      Legend(modifier = Modifier.padding(top = 8.dp).fillMaxWidth(), bars = bars)
    }
  }

@Composable
private fun Message(text: String, color: Color, modifier: Modifier = Modifier) =
  Box(modifier = modifier, contentAlignment = Center) {
    Text(text = text, style = typography.titleLarge, color = color, textAlign = Center)
  }

@Composable
private fun CompactHeader(
  data: CustomData,
  modifier: Modifier = Modifier,
) =
  Row(modifier = modifier.fillMaxWidth()) {
    Text(
      modifier = Modifier.weight(1f),
      text = data.title,
      color = colors.pageText,
      overflow = Ellipsis,
    )

    DateRangeText(dateRange(data.config.dates))
  }

@Composable
private fun RegularHeader(
  data: CustomData,
  modifier: Modifier = Modifier,
) =
  Row(modifier = modifier, verticalAlignment = CenterVertically) {
    DateRangeText(
      modifier = Modifier.weight(1f),
      text = dateRange(data.config.dates),
      color = colors.pageText,
    )

    Text(
      text = data.totals[data.config.balanceType.metric].formattedText(),
      style = typography.titleMedium,
      color = colors.pageText,
    )
  }

/** One bar of a bar chart, or one legend entry of a stacked bar chart */
@Immutable internal data class CustomBar(val name: String, val amount: Amount, val color: Color)

// packages/desktop-client/src/components/reports/ReportLegend.tsx
@Composable
private fun Legend(
  bars: ImmutableList<CustomBar>,
  modifier: Modifier = Modifier,
) =
  FlowRow(
    modifier = modifier.heightIn(max = LegendMaxHeight).verticalScrollWithBar(),
    horizontalArrangement = spacedBy(12.dp),
    verticalArrangement = spacedBy(2.dp),
  ) {
    bars.fastForEach { bar -> LegendItem(bar.name, bar.color) }
  }

@Composable
private fun LegendItem(text: String, color: Color, modifier: Modifier = Modifier) =
  Row(modifier = modifier, verticalAlignment = CenterVertically) {
    Box(modifier = Modifier.size(12.dp).background(color, CircleShape))
    Text(
      modifier = Modifier.padding(start = 4.dp),
      text = text,
      style = typography.bodySmall,
      color = colors.pageText,
    )
  }

private val LegendMaxHeight = 96.dp

@Composable
private fun dateRange(dates: CustomReportDates): String =
  when (dates) {
    is Live -> dates.type.string()
    is Static -> dateRange(dates.start.yearMonth, dates.end.yearMonth)
  }

@Preview
@Composable
private fun PreviewCustomChart(
  @PreviewParameter(CustomChartProvider::class) params: ColoredParams<CustomChartParams>,
) =
  PreviewWithColoredParams(params, isPrivacyEnabled = params.data.isPrivacyEnabled) {
    CustomChart(
      modifier =
        Modifier.background(colors.tableBackground, CardShape)
          .width(WIDTH.dp)
          .height(if (compact) 300.dp else 500.dp)
          .padding(5.dp),
      data = data,
      compact = compact,
    )
  }

private data class CustomChartParams(
  val data: CustomData,
  val compact: Boolean,
  val isPrivacyEnabled: Boolean = false,
)

private class CustomChartProvider :
  ColoredParameterProvider<CustomChartParams>(
    CustomChartParams(PREVIEW_CUSTOM_DATA, compact = false),
    CustomChartParams(PREVIEW_CUSTOM_DATA, compact = false, isPrivacyEnabled = true),
    CustomChartParams(PREVIEW_CUSTOM_DATA, compact = true),
    CustomChartParams(previewCustomData(BarGraph, Interval, Net), compact = false),
    CustomChartParams(previewCustomData(BarGraph, Interval, Net), compact = true),
    CustomChartParams(previewCustomData(StackedBarGraph, Category, Payment), compact = false),
    CustomChartParams(previewCustomData(StackedBarGraph, Category, Payment), compact = true),
    CustomChartParams(previewCustomData(StackedBarGraph, Category, Net), compact = false),
    CustomChartParams(previewCustomData(TableGraph, Category, Payment), compact = true),
  )

private val PREVIEW_START = LocalDate(2025, Month.JANUARY, 1)
private val PREVIEW_END = LocalDate(2025, Month.JUNE, 30)

// Monthly assets and debts per category, January to June 2025
private val PREVIEW_AMOUNTS =
  listOf(
    "Salary" to listOf(3200 to 0, 3200 to 0, 3300 to 0, 3300 to 0, 3300 to 0, 3500 to 0),
    "Groceries" to listOf(0 to -420, 0 to -380, 0 to -455, 0 to -410, 15 to -390, 0 to -470),
    "Rent" to listOf(0 to -1200, 0 to -1200, 0 to -1200, 0 to -1200, 0 to -1200, 0 to -1250),
    "Eating out" to listOf(0 to -130, 0 to -85, 0 to -210, 0 to -60, 0 to -145, 0 to -190),
    "Transport" to listOf(0 to -90, 0 to -95, 0 to -110, 0 to -90, 0 to -80, 0 to -120),
    "Holiday" to listOf(0 to 0, 0 to 0, 0 to -650, 0 to 0, 0 to 0, 0 to -2400),
  )

internal val PREVIEW_CUSTOM_DATA = previewCustomData(BarGraph, Category, Payment)

private fun previewCustomData(
  graphType: GraphType,
  groupBy: GroupBy,
  balanceType: BalanceType,
): CustomData {
  val metric = balanceType.metric
  val intervals =
    (PREVIEW_START.month.number..PREVIEW_END.month.number).map { month ->
      val yearMonth = YearMonth(PREVIEW_START.year, month)
      CustomInterval(start = yearMonth.firstDay, range = yearMonth.firstDay..yearMonth.lastDay)
    }

  val groups =
    PREVIEW_AMOUNTS.asSequence()
      .mapIndexed { index, (name, amounts) ->
        val totals = amounts.map { (assets, debts) -> previewTotals(assets, debts) }
        CustomGroupTotals(
          group = CustomGroup.Category(CategoryId("category-$index"), name),
          totals = totals.reduce(::plus),
          intervals =
            intervals
              .zip(totals) { interval, t -> CustomGroupIntervalTotals(interval, t, Zero) }
              .toImmutableList(),
          categories = persistentListOf(),
        )
      }
      .filter { it.totals[metric] != Zero }
      .sortedByDescending { it.totals[metric].plotted(metric) }
      .toList()

  val intervalTotals = intervals.mapIndexed { index, interval ->
    CustomIntervalTotals(
      interval = interval,
      totals = groups.map { it.intervals[index].totals }.reduce(::plus),
      stacked =
        groups
          .associate { it.group to it.intervals[index].totals.stacked(metric) }
          .toImmutableMap(),
    )
  }

  return CustomData(
    title = "My Custom Report",
    config =
      CustomReportConfig(
        mode = if (graphType == StackedBarGraph) Time else Total,
        groupBy = groupBy,
        balanceType = balanceType,
        graphType = graphType,
        interval = Monthly,
        dates = CustomReportDates.Static(start = PREVIEW_START, end = PREVIEW_END),
        showEmpty = false,
        showOffBudget = false,
        showHidden = false,
        showUncategorized = false,
        selectedCategories = persistentListOf(),
        conditions = persistentListOf(),
        conditionsOp = And,
        sortBy = Desc,
        trimIntervals = false,
        showTrendLines = false,
        colorScheme = null,
      ),
    range = PREVIEW_START..PREVIEW_END,
    intervals = intervalTotals.toImmutableList(),
    groups = groups.toImmutableList(),
    categoryGroups = persistentListOf(),
    totals = groups.map { it.totals }.reduce(::plus),
  )
}

private fun previewTotals(assets: Int, debts: Int): CustomTotals {
  val net = assets + debts
  return CustomTotals(
    assets = Amount(assets.toDouble()),
    debts = Amount(debts.toDouble()),
    netAssets = Amount(net.coerceAtLeast(0).toDouble()),
    netDebts = Amount(net.coerceAtMost(0).toDouble()),
    total = Amount(net.toDouble()),
  )
}

private fun plus(a: CustomTotals, b: CustomTotals) =
  CustomTotals(
    assets = a.assets + b.assets,
    debts = a.debts + b.debts,
    netAssets = a.netAssets + b.netAssets,
    netDebts = a.netDebts + b.netDebts,
    total = a.total + b.total,
  )

private fun CustomTotals.stacked(metric: CustomMetric): Amount =
  when (metric) {
    Debts -> -debts
    NetDebts -> -netDebts
    Assets,
    NetAssets,
    Total -> this[metric]
  }
