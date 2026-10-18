package aktual.budget.reports.ui.charts

import aktual.budget.model.Amount
import aktual.budget.reports.vm.CustomData
import aktual.budget.reports.vm.CustomMetric
import aktual.budget.reports.vm.CustomTotals
import aktual.budget.reports.vm.metric
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.isInPreview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.columnSeries
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer.AreaFill
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer.LineFill
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer.LineProvider
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer.LineStroke
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.Fill
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.runBlocking

// packages/desktop-client/src/components/reports/graphs/LineGraph.tsx: a line per group across the
// intervals, with debts drawn above the axis like the stacked bars. Trend lines are dashed.
// Upstream
// curves the lines monotonically, Vico's cubic curves overshoot the points so these stay straight
@Composable
internal fun CustomLineChart(
  data: CustomData,
  legend: ImmutableList<CustomBar>,
  compact: Boolean,
  modifier: Modifier = Modifier,
) {
  val labels = intervalLabels(data)
  val stacks = remember(data, legend) { customStacks(data, legend) }
  val trends = remember(data, legend) { trendSeries(data, legend) }

  val modelProducer = remember { CartesianChartModelProducer() }
  if (isInPreview()) {
    runBlocking { modelProducer.populateLines(stacks, trends) }
  } else {
    LaunchedEffect(stacks, trends) { modelProducer.populateLines(stacks, trends) }
  }

  val lineProvider =
    remember(legend) { LineProvider.series(legend.map { groupLine(it.color, compact) }) }
  val trendProvider = remember(legend) { LineProvider.series(legend.map { trendLine(it.color) }) }
  val format = rememberAmountFormatter()
  val total = Strings.reportsCustomTotal
  val markerFormatter =
    remember(labels, stacks, format, total) {
      DefaultCartesianMarker.ValueFormatter { _, targets ->
        val index = targets.index()
        val stack = stacks.getOrNull(index) ?: return@ValueFormatter " "
        stackedMarkerText(labels[index], stack.sortedByDescending { it.amount }, total, format)
      }
    }

  CartesianChartHost(
    modifier = modifier,
    modelProducer = modelProducer,
    scrollState = rememberVicoScrollState(scrollEnabled = false),
    animationSpec = chartAnimationSpec(compact),
    chart =
      rememberCartesianChart(
        rememberLineCartesianLayer(lineProvider = lineProvider),
        // Left without a model when there are no trends, so Vico skips drawing it
        rememberLineCartesianLayer(lineProvider = trendProvider),
        startAxis = amountAxis(compact),
        bottomAxis = labelAxis(labels = labels, compact = compact, rotationDegrees = 0f),
        marker =
          if (compact) {
            null
          } else {
            rememberMarker(valueFormatter = markerFormatter, lineCount = markerLines(legend.size))
          },
        markerVisibilityListener = rememberMarkerHaptics(compact),
      ),
  )
}

// packages/desktop-client/src/components/reports/graphs/AreaGraph.tsx: the report's total for each
// interval, green above the axis and red below. Unlike the bar charts, debts stay negative
@Composable
internal fun CustomAreaChart(
  data: CustomData,
  compact: Boolean,
  modifier: Modifier = Modifier,
) {
  val metric = data.config.balanceType.metric
  val labels = intervalLabels(data)
  val values = remember(data, metric) { data.intervals.map { it.totals[metric].toDouble() } }

  val modelProducer = remember { CartesianChartModelProducer() }
  if (isInPreview()) {
    runBlocking { modelProducer.populateArea(values) }
  } else {
    LaunchedEffect(values) { modelProducer.populateArea(values) }
  }

  val rows = totalsRows(metric)
  val format = rememberAmountFormatter()
  val markerFormatter =
    remember(data, labels, rows, format) {
      DefaultCartesianMarker.ValueFormatter { _, targets ->
        val index = targets.index()
        val interval = data.intervals.getOrNull(index) ?: return@ValueFormatter " "
        totalsMarkerText(labels[index], interval.totals, rows, format)
      }
    }

  CartesianChartHost(
    modifier = modifier,
    modelProducer = modelProducer,
    scrollState = rememberVicoScrollState(scrollEnabled = false),
    animationSpec = chartAnimationSpec(compact),
    chart =
      rememberCartesianChart(
        rememberLineCartesianLayer(
          lineProvider =
            LineProvider.series(
              LineCartesianLayer.rememberLine(
                fill =
                  LineFill.double(
                    topFill = Fill(colors.reportsNumberPositive),
                    bottomFill = Fill(colors.reportsNumberNegative),
                  ),
                areaFill =
                  AreaFill.double(
                    topFill = Fill(colors.reportsNumberPositive.copy(alpha = AREA_ALPHA)),
                    bottomFill = Fill(colors.reportsNumberNegative.copy(alpha = AREA_ALPHA)),
                  ),
              ),
            ),
        ),
        startAxis = amountAxis(compact),
        bottomAxis = labelAxis(labels = labels, compact = compact, rotationDegrees = 0f),
        marker =
          if (compact) {
            null
          } else {
            rememberMarker(valueFormatter = markerFormatter, lineCount = rows.size + 1)
          },
        markerVisibilityListener = rememberMarkerHaptics(compact),
      ),
  )
}

// packages/desktop-client/src/components/reports/graphs/BarLineGraph.tsx draws bars and a line
// through the same values. Upstream reads them from a field the custom spreadsheet never fills in,
// so this plots each interval's total the way the bar chart does
@Composable
internal fun CustomBarLineChart(
  data: CustomData,
  compact: Boolean,
  modifier: Modifier = Modifier,
) {
  val metric = data.config.balanceType.metric
  val labels = intervalLabels(data)
  val theme = colors
  val bars = remember(data, labels, theme) { intervalBars(data, labels, theme) }
  val values = remember(bars, metric) { bars.map { it.amount.plotted(metric).toDouble() } }

  val modelProducer = remember { CartesianChartModelProducer() }
  if (isInPreview()) {
    runBlocking { modelProducer.populateBarLine(values) }
  } else {
    LaunchedEffect(values) { modelProducer.populateBarLine(values) }
  }

  val columnProvider = remember(bars) { IndexedColumns(bars.map { column(it.color) }) }
  val rows = totalsRows(Total)
  val format = rememberAmountFormatter()
  val markerFormatter =
    remember(data, labels, rows, format) {
      DefaultCartesianMarker.ValueFormatter { _, targets ->
        val index = targets.index()
        val interval = data.intervals.getOrNull(index) ?: return@ValueFormatter " "
        totalsMarkerText(labels[index], interval.totals, rows, format)
      }
    }

  CartesianChartHost(
    modifier = modifier,
    modelProducer = modelProducer,
    scrollState = rememberVicoScrollState(scrollEnabled = false),
    animationSpec = chartAnimationSpec(compact),
    chart =
      rememberCartesianChart(
        rememberColumnCartesianLayer(columnProvider = columnProvider),
        rememberLineCartesianLayer(
          lineProvider =
            LineProvider.series(
              LineCartesianLayer.rememberLine(
                fill = LineFill.single(Fill(colors.pageTextLight)),
                stroke = LineStroke.Continuous(thickness = LINE_THICKNESS),
              ),
            ),
        ),
        startAxis = amountAxis(compact),
        bottomAxis = labelAxis(labels = labels, compact = compact, rotationDegrees = 0f),
        marker =
          if (compact) {
            null
          } else {
            rememberMarker(valueFormatter = markerFormatter, lineCount = rows.size + 1)
          },
        markerVisibilityListener = rememberMarkerHaptics(compact),
      ),
  )
}

// Each group's trend line, as a value per interval. Empty unless the report shows trend lines
internal fun trendSeries(data: CustomData, legend: List<CustomBar>): List<List<Double>> {
  if (!data.config.showTrendLines || data.trends.isEmpty()) return emptyList()
  val last = data.intervals.lastIndex
  return data.groups.take(legend.size).map { group ->
    val trend = data.trends[group.group]
    val start = trend?.start?.toDouble() ?: 0.0
    val end = trend?.end?.toDouble() ?: 0.0
    data.intervals.indices.map { x -> start + (end - start) * x / last }
  }
}

/** A row of the area and bar-line markers: which of an interval's totals to show, and its name */
@Immutable internal data class TotalsRow(val name: String, val metric: CustomMetric)

// AreaGraph.tsx CustomTooltip: assets and debts, plus the net total for net reports
@Composable
private fun totalsRows(metric: CustomMetric): ImmutableList<TotalsRow> {
  val assets = TotalsRow(Strings.reportsCustomAssets, Assets)
  val debts = TotalsRow(Strings.reportsCustomDebts, Debts)
  val netAssets = TotalsRow(Strings.reportsCustomNetAssets, NetAssets)
  val netDebts = TotalsRow(Strings.reportsCustomNetDebts, NetDebts)
  val net = TotalsRow(Strings.reportsCustomNet, Total)
  return remember(metric, assets, debts, netAssets, netDebts, net) {
    totalsRows(metric, assets, debts, netAssets, netDebts, net)
  }
}

internal fun totalsRows(
  metric: CustomMetric,
  assets: TotalsRow,
  debts: TotalsRow,
  netAssets: TotalsRow,
  netDebts: TotalsRow,
  net: TotalsRow,
): ImmutableList<TotalsRow> =
  when (metric) {
    Assets -> persistentListOf(assets)
    Debts -> persistentListOf(debts)
    NetAssets -> persistentListOf(netAssets)
    NetDebts -> persistentListOf(netDebts)
    Total -> persistentListOf(assets, debts, net)
  }

// The net total is in bold, as upstream does
internal fun totalsMarkerText(
  label: String,
  totals: CustomTotals,
  rows: List<TotalsRow>,
  format: (Amount) -> AnnotatedString,
): AnnotatedString = buildAnnotatedString {
  withStyle(SpanStyle(fontWeight = Bold)) { append(label) }
  for (row in rows) {
    append('\n')
    val style = if (row.metric == Total) SpanStyle(fontWeight = Bold) else SpanStyle()
    withStyle(style) {
      append("${row.name}: ")
      append(format(totals[row.metric]))
    }
  }
}

private fun groupLine(color: Color, compact: Boolean) =
  LineCartesianLayer.Line(
    fill = LineFill.single(Fill(color)),
    stroke =
      LineStroke.Continuous(thickness = if (compact) COMPACT_LINE_THICKNESS else LINE_THICKNESS),
  )

private fun trendLine(color: Color) =
  LineCartesianLayer.Line(
    fill = LineFill.single(Fill(color.copy(alpha = TREND_ALPHA))),
    stroke = LineStroke.Dashed(thickness = 1.dp, dashLength = 4.dp, gapLength = 4.dp),
  )

private suspend fun CartesianChartModelProducer.populateLines(
  stacks: List<List<CustomBar>>,
  trends: List<List<Double>>,
) = runTransaction {
  val x = stacks.indices.toList()
  val series =
    (stacks.firstOrNull()?.indices ?: IntRange.EMPTY).map { group ->
      stacks.map { it[group].amount.toDouble() }
    }
  if (series.isNotEmpty()) {
    lineSeries { series.forEach { y -> series(x = x, y = y) } }
  }
  if (trends.isNotEmpty()) {
    lineSeries { trends.forEach { y -> series(x = x, y = y) } }
  }
}

private suspend fun CartesianChartModelProducer.populateArea(values: List<Double>) =
  runTransaction {
    if (values.isNotEmpty()) {
      lineSeries { series(x = values.indices.toList(), y = values) }
    }
  }

private suspend fun CartesianChartModelProducer.populateBarLine(values: List<Double>) =
  runTransaction {
    if (values.isNotEmpty()) {
      val x = values.indices.toList()
      columnSeries { series(x = x, y = values) }
      lineSeries { series(x = x, y = values) }
    }
  }

private val LINE_THICKNESS = 2.dp
private val COMPACT_LINE_THICKNESS = 1.5.dp
private const val AREA_ALPHA = 0.2f
private const val TREND_ALPHA = 0.6f
