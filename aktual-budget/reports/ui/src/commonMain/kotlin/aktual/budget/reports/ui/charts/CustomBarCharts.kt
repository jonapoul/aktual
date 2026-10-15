package aktual.budget.reports.ui.charts

import aktual.budget.model.Amount
import aktual.budget.model.GroupBy
import aktual.budget.model.Interval
import aktual.budget.reports.vm.CustomData
import aktual.budget.reports.vm.CustomGroup
import aktual.budget.reports.vm.CustomInterval
import aktual.budget.reports.vm.CustomMetric
import aktual.budget.reports.vm.metric
import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.LocalCurrencyConfig
import aktual.core.ui.LocalNumberFormatConfig
import aktual.core.ui.LocalPrivacyEnabled
import aktual.core.ui.formatted
import aktual.core.ui.isInPreview
import aktual.core.ui.redacted
import aktual.core.ui.redactedFontFamily
import aktual.core.ui.stringShort
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastMap
import com.patrykandpatrick.vico.multiplatform.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.multiplatform.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.multiplatform.cartesian.data.columnSeries
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.multiplatform.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.multiplatform.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.multiplatform.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.multiplatform.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.multiplatform.common.Fill
import com.patrykandpatrick.vico.multiplatform.common.component.LineComponent
import com.patrykandpatrick.vico.multiplatform.common.data.ExtraStore
import kotlin.math.roundToInt
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.runBlocking

// packages/desktop-client/src/components/reports/graphs/BarGraph.tsx: one bar per group, or per
// interval when grouped by interval. Debts are drawn above the axis, as upstream's getVal() does
@Composable
internal fun CustomBarChart(
  data: CustomData,
  bars: ImmutableList<CustomBar>,
  compact: Boolean,
  modifier: Modifier = Modifier,
) {
  val metric = data.config.balanceType.metric
  val modelProducer = remember { CartesianChartModelProducer() }
  if (isInPreview()) {
    runBlocking { modelProducer.populateBars(bars, metric) }
  } else {
    LaunchedEffect(bars, metric) { modelProducer.populateBars(bars, metric) }
  }

  val columnProvider = remember(bars) { IndexedColumns(bars.map { column(it.color) }) }
  val format = rememberAmountFormatter()
  val markerFormatter =
    remember(bars, format) {
      DefaultCartesianMarker.ValueFormatter { _, targets ->
        val bar = bars.getOrNull(targets.index()) ?: return@ValueFormatter " "
        buildAnnotatedString {
          withStyle(SpanStyle(fontWeight = Bold)) { append(bar.name) }
          append('\n')
          append(format(bar.amount))
        }
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
        startAxis = amountAxis(compact),
        bottomAxis =
          labelAxis(
            labels = bars.map { it.name }.toImmutableList(),
            compact = compact,
            rotationDegrees = BAR_LABEL_ROTATION,
          ),
        marker =
          if (compact) null else rememberMarker(valueFormatter = markerFormatter, lineCount = 2),
        markerVisibilityListener = rememberMarkerHaptics(compact),
      ),
  )
}

// packages/desktop-client/src/components/reports/graphs/StackedBarGraph.tsx: one bar per interval,
// split into each group's share. Upstream draws the legend in reverse, so the first group is on top
@Composable
internal fun CustomStackedBarChart(
  data: CustomData,
  legend: ImmutableList<CustomBar>,
  compact: Boolean,
  modifier: Modifier = Modifier,
) {
  val labels = data.intervals.map { it.interval.label(data.config.interval) }.toImmutableList()
  val stacks = remember(data, legend) { customStacks(data, legend) }

  val modelProducer = remember { CartesianChartModelProducer() }
  if (isInPreview()) {
    runBlocking { modelProducer.populateStacks(stacks) }
  } else {
    LaunchedEffect(stacks) { modelProducer.populateStacks(stacks) }
  }

  val columnProvider =
    remember(legend) {
      ColumnCartesianLayer.ColumnProvider.series(legend.asReversed().map { column(it.color) })
    }
  val format = rememberAmountFormatter()
  val total = Strings.reportsCustomTotal
  val markerFormatter =
    remember(labels, stacks, format, total) {
      DefaultCartesianMarker.ValueFormatter { _, targets ->
        val index = targets.index()
        val stack = stacks.getOrNull(index) ?: return@ValueFormatter " "
        stackedMarkerText(labels[index], stack, total, format)
      }
    }

  CartesianChartHost(
    modifier = modifier,
    modelProducer = modelProducer,
    scrollState = rememberVicoScrollState(scrollEnabled = false),
    animationSpec = chartAnimationSpec(compact),
    chart =
      rememberCartesianChart(
        rememberColumnCartesianLayer(
          columnProvider = columnProvider,
          mergeMode = { Stacked },
        ),
        startAxis = amountAxis(compact),
        bottomAxis = labelAxis(labels = labels, compact = compact, rotationDegrees = 0f),
        marker =
          if (compact) {
            null
          } else {
            rememberMarker(
              valueFormatter = markerFormatter,
              lineCount = MAX_MARKER_ROWS + MARKER_EXTRA_LINES,
            )
          },
        markerVisibilityListener = rememberMarkerHaptics(compact),
      ),
  )
}

@Composable
internal fun customBars(data: CustomData): ImmutableList<CustomBar> {
  val names =
    if (data.config.groupBy == GroupBy.Interval) {
      data.intervals.fastMap { it.interval.label(data.config.interval) }
    } else {
      data.groups.fastMap { it.group.label() }
    }
  return customBars(data, names, colors)
}

// packages/desktop-client/src/components/reports/spreadsheets/calculateLegend.ts: groups take the
// qualitative colours in order, intervals are coloured by whether they're money in or out
internal fun customBars(
  data: CustomData,
  names: List<String>,
  theme: Colors,
): ImmutableList<CustomBar> {
  val metric = data.config.balanceType.metric
  return if (data.config.groupBy == GroupBy.Interval) {
      data.intervals.mapIndexed { index, item ->
        val amount = item.totals[metric]
        CustomBar(names[index], amount, theme.intervalColor(metric, amount))
      }
    } else {
      data.groups.mapIndexed { index, item ->
        CustomBar(names[index], item.totals[metric], theme.qualitative(index))
      }
    }
    .toImmutableList()
}

// Each interval's stack, with a bar per legend entry
internal fun customStacks(data: CustomData, legend: List<CustomBar>): List<List<CustomBar>> =
  data.intervals.map { interval ->
    data.groups.zip(legend) { group, bar ->
      bar.copy(amount = interval.stacked[group.group] ?: Zero)
    }
  }

// Each group's amounts across the intervals. Stacked bottom up, so the first group goes last
internal fun stackSeries(stacks: List<List<CustomBar>>): List<List<Double>> {
  val groups = stacks.firstOrNull()?.size ?: 0
  return (groups - 1 downTo 0).map { group -> stacks.map { it[group].amount.toDouble() } }
}

@Composable
internal fun CustomGroup.label(): String =
  when (this) {
    is Category -> name
    is Group -> name
    is Payee -> name
    is Account -> name
    Uncategorized -> Strings.reportsCustomUncategorised
    Transfers -> Strings.reportsCustomTransfers
    OffBudget -> Strings.reportsCustomOffBudget
    UncategorizedAndOffBudget -> Strings.reportsCustomUncategorisedAndOffBudget
  }

// ReportOptions.ts getIntervalFormat(): days and weeks follow the date format preference
@Composable
private fun CustomInterval.label(interval: Interval): String =
  when (interval) {
    Monthly -> "${start.month.stringShort()} ${start.year.toString().takeLast(2)}"
    Yearly -> start.year.toString()
    Daily,
    Weekly,
    Unknown -> start.formatted()
  }

private fun Colors.intervalColor(metric: CustomMetric, amount: Amount): Color =
  when (metric) {
    Debts,
    NetDebts -> reportsNumberNegative
    Assets,
    NetAssets -> reportsNumberPositive
    Total -> if (amount.isPositive()) reportsNumberPositive else reportsNumberNegative
  }

internal fun Amount.plotted(metric: CustomMetric): Amount =
  if (metric == Debts || metric == NetDebts) -this else this

private fun List<CartesianMarker.Target>.index(): Int = firstOrNull()?.x?.roundToInt() ?: -1

// Upstream's tooltip: each group with money in the interval, then the interval's total
internal fun stackedMarkerText(
  label: String,
  stack: List<CustomBar>,
  totalLabel: String,
  format: (Amount) -> AnnotatedString,
): AnnotatedString {
  val rows = stack.filter { it.amount != Zero }
  val total = stack.fold(Amount.Zero) { sum, bar -> sum + bar.amount }
  return buildAnnotatedString {
    withStyle(SpanStyle(fontWeight = Bold)) { append(label) }
    for (row in rows.take(MAX_MARKER_ROWS)) {
      append('\n')
      withStyle(SpanStyle(color = row.color)) {
        append("${row.name}: ")
        append(format(row.amount))
      }
    }
    if (rows.size > MAX_MARKER_ROWS) append("\n…")
    append('\n')
    withStyle(SpanStyle(fontWeight = Bold)) {
      append("$totalLabel: ")
      append(format(total))
    }
  }
}

@Composable
private fun rememberAmountFormatter(): (Amount) -> AnnotatedString {
  val numberFormatConfig = LocalNumberFormatConfig.current
  val currencyConfig = LocalCurrencyConfig.current
  val isPrivacyEnabled = LocalPrivacyEnabled.current
  val redactedFont = redactedFontFamily()
  return remember(numberFormatConfig, currencyConfig, isPrivacyEnabled, redactedFont) {
    { amount ->
      val text =
        amount.toString(
          numberFormatConfig = numberFormatConfig,
          currencyConfig = currencyConfig,
          includeSign = false,
          isPrivacyEnabled = isPrivacyEnabled,
        )
      AnnotatedString(text).redacted(redactedFont)
    }
  }
}

@Composable
private fun amountAxis(compact: Boolean) =
  VerticalAxis.rememberStart(
    line = axisLineComponent(compact),
    guideline = axisGuidelineComponent(compact),
    label = axisLabelComponent(compact),
    tick = axisTickComponent(compact),
    valueFormatter = amountYAxisFormatter(),
    itemPlacer = remember { VerticalAxis.ItemPlacer.count(count = { AMOUNT_LABELS }) },
  )

// Skips labels when there are too many to fit, the marker still names every bar
@Composable
private fun labelAxis(labels: ImmutableList<String>, compact: Boolean, rotationDegrees: Float) =
  HorizontalAxis.rememberBottom(
    line = axisLineComponent(compact),
    guideline = null,
    label = axisLabelComponent(compact),
    labelRotationDegrees = rotationDegrees,
    tick = axisTickComponent(compact),
    // Vico rejects empty labels, and a group can have an empty name
    valueFormatter =
      remember(labels) {
        CartesianValueFormatter { _, value, _ ->
          labels.getOrNull(value.roundToInt())?.ifEmpty { null } ?: " "
        }
      },
    itemPlacer =
      remember(labels.size) {
        val spacing = (labels.size + MAX_AXIS_LABELS - 1) / MAX_AXIS_LABELS
        HorizontalAxis.ItemPlacer.aligned(offset = { 0 }, spacing = { spacing.coerceAtLeast(1) })
      },
  )

private suspend fun CartesianChartModelProducer.populateBars(
  bars: List<CustomBar>,
  metric: CustomMetric,
) = runTransaction {
  if (bars.isNotEmpty()) {
    columnSeries {
      series(x = bars.indices.toList(), y = bars.map { it.amount.plotted(metric).toDouble() })
    }
  }
}

private suspend fun CartesianChartModelProducer.populateStacks(stacks: List<List<CustomBar>>) =
  runTransaction {
    val series = stackSeries(stacks)
    if (series.isNotEmpty()) {
      columnSeries { series.forEach { y -> series(x = stacks.indices.toList(), y = y) } }
    }
  }

private fun column(color: Color) = LineComponent(fill = Fill(color), thickness = BAR_THICKNESS)

// Colours each column of a single series by its x index
private class IndexedColumns(private val columns: List<LineComponent>) :
  ColumnCartesianLayer.ColumnProvider {
  override fun getColumn(
    entry: ColumnCartesianLayerModel.Entry,
    seriesIndex: Int,
    extraStore: ExtraStore,
  ): LineComponent = columns[entry.x.roundToInt().coerceIn(columns.indices)]

  override fun getWidestSeriesColumn(seriesIndex: Int, extraStore: ExtraStore) = columns.first()
}

private val BAR_THICKNESS = 16.dp
private const val BAR_LABEL_ROTATION = -35f
private const val AMOUNT_LABELS = 8
private const val MAX_AXIS_LABELS = 12
internal const val MAX_MARKER_ROWS = 10

// The interval label, the "…" when rows are cut and the total
private const val MARKER_EXTRA_LINES = 3
