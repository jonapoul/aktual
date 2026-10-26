package aktual.budget.reports.ui.charts

import aktual.budget.model.Amount
import aktual.budget.reports.vm.BudgetAnalysisData
import aktual.budget.reports.vm.BudgetAnalysisDatum
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.CardShape
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.ColorParams
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.WrapWidthTable
import aktual.core.ui.formattedString
import aktual.core.ui.formattedText
import aktual.core.ui.isInPreview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChart
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.columnModel
import com.patrykandpatrick.vico.compose.cartesian.data.lineModel
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.runBlocking

@Composable
internal fun BudgetAnalysisChart(
  data: BudgetAnalysisData,
  compact: Boolean,
  modifier: Modifier = Modifier,
  includeHeader: Boolean = true,
  zoom: ChartZoomState = rememberChartZoomState(data),
) =
  Column(modifier = modifier) {
    if (includeHeader) {
      if (compact) {
        CompactHeader(data)
      } else {
        RegularHeader(data)
      }
    }

    Chart(
      modifier = if (compact) Modifier.fillMaxSize() else Modifier.weight(1f),
      data = data,
      compact = compact,
      zoom = zoom,
    )
  }

@Composable
private fun CompactHeader(
  data: BudgetAnalysisData,
  modifier: Modifier = Modifier,
) =
  Row(modifier = modifier.padding(4.dp), verticalAlignment = CenterVertically) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = data.title ?: Strings.reportsChooseTypeBudgetAnalysis,
        color = colors.pageText,
        overflow = Ellipsis,
      )
      DateRangeText(dateRange(data.items.keys))
    }

    Text(
      text = data.endingBalance.formattedText(),
      color = data.endingBalance.color(),
      overflow = Ellipsis,
    )
  }

@Composable
private fun RegularHeader(
  data: BudgetAnalysisData,
  modifier: Modifier = Modifier,
) =
  Row(modifier = modifier.padding(start = 4.dp, end = 4.dp, top = 4.dp).fillMaxWidth()) {
    DateRangeText(
      modifier = Modifier.weight(1f),
      text = dateRange(data.items.keys),
      color = colors.pageText,
    )

    val padding = PaddingValues(horizontal = 2.dp)
    val normalStyle = typography.labelMedium.copy(textAlign = Start, color = colors.pageText)
    val boldStyle = normalStyle.copy(textAlign = End, fontWeight = W600)

    WrapWidthTable(
      modifier = Modifier.heightIn(max = 200.dp),
      ellipsize = false,
      data =
        persistentListOf(
          persistentListOf(
            Strings.reportsBudgetAnalysisBudgeted,
            data.totalBudgeted.formattedString(),
          ),
          persistentListOf(Strings.reportsBudgetAnalysisSpent, data.totalSpent.formattedString()),
          persistentListOf(
            Strings.reportsBudgetAnalysisOverspending,
            data.totalOverspendingAdjustment.formattedString(),
          ),
          persistentListOf(
            Strings.reportsBudgetAnalysisEndingBalance,
            data.endingBalance.formattedString(),
          ),
        ),
      textStyles = persistentListOf(normalStyle, boldStyle),
      paddings = persistentListOf(padding, padding),
    )
  }

@Composable
@ReadOnlyComposable
private fun Amount.color(): Color = if (isPositive()) colors.noticeText else colors.errorText

private enum class Series {
  Budgeted,
  Spent,
  Overspending,
  Balance,
}

private fun Series.value(datum: BudgetAnalysisDatum): Amount =
  when (this) {
    Budgeted -> datum.budgeted
    Spent -> datum.spent
    Overspending -> datum.overspendingAdjustment
    Balance -> datum.balance
  }

@Composable
@ReadOnlyComposable
private fun Series.color(): Color =
  when (this) {
    Budgeted -> colors.reportsNumberPositive
    Spent -> colors.reportsNumberNegative
    Overspending -> colors.templateNumberUnderFunded
    Balance -> colors.reportsGray
  }

@Immutable
private data class Layers(val columns: ImmutableList<Series>, val lines: ImmutableList<Series>)

// Like upstream, the bar chart still draws the balance as a line, and balance only is always a line
private fun layers(data: BudgetAnalysisData): Layers {
  val categories =
    if (data.balanceOnly) {
      emptyList()
    } else {
      listOf<Series>(Budgeted, Spent, Overspending)
    }
  val balances = if (data.showBalance || data.balanceOnly) listOf(Series.Balance) else emptyList()
  return if (data.graphType == Bar && !data.balanceOnly) {
    Layers(categories.toImmutableList(), balances.toImmutableList())
  } else {
    Layers(persistentListOf(), (categories + balances).toImmutableList())
  }
}

@Composable
private fun Chart(
  data: BudgetAnalysisData,
  compact: Boolean,
  zoom: ChartZoomState,
  modifier: Modifier = Modifier,
) {
  val layers = remember(data) { layers(data) }
  val modelProducer = remember { CartesianChartModelProducer() }
  if (isInPreview()) {
    runBlocking { modelProducer.populate(data, layers, zoom.range) }
  } else {
    LaunchedEffect(data, layers, zoom.range) { modelProducer.populate(data, layers, zoom.range) }
  }

  val chart = rememberChart(layers, compact, zoom)

  ZoomableChart(modifier = modifier, state = zoom, enabled = !compact) { chartModifier ->
    CartesianChartHost(
      modifier = chartModifier,
      modelProducer = modelProducer,
      scrollState = rememberVicoScrollState(scrollEnabled = false),
      animationSpec = chartAnimationSpec(compact),
      chart = chart,
    )
  }
}

@Composable
private fun rememberChart(layers: Layers, compact: Boolean, zoom: ChartZoomState): CartesianChart {
  val label = axisLabelComponent(compact)
  val tick = axisTickComponent(compact)
  val guideline = axisGuidelineComponent(compact)
  val line = axisLineComponent(compact)

  val columnLayer =
    if (layers.columns.isEmpty()) {
      null
    } else {
      rememberColumnCartesianLayer(
        ColumnCartesianLayer.ColumnProvider.series(
          layers.columns.map { series ->
            rememberLineComponent(fill = Fill(series.color()), thickness = 8.dp)
          },
        ),
        rangeProvider = remember { ZoomRangeProvider() },
      )
    }
  val lineLayer =
    if (layers.lines.isEmpty()) {
      null
    } else {
      rememberLineCartesianLayer(
        lineProvider =
          LineCartesianLayer.LineProvider.series(
            layers.lines.map { series ->
              LineCartesianLayer.rememberLine(
                fill = LineCartesianLayer.LineFill.single(Fill(series.color())),
                stroke = LineCartesianLayer.LineStroke.Continuous(thickness = 2.dp),
                interpolator = LineCartesianLayer.Interpolator.catmullRom(),
              )
            },
          ),
        rangeProvider = remember { ZoomRangeProvider() },
      )
    }

  val startAxis =
    VerticalAxis.rememberStart(
      line = line,
      guideline = guideline,
      label = label,
      tick = tick,
      valueFormatter = amountYAxisFormatter(),
      itemPlacer = remember { VerticalAxis.ItemPlacer.count(count = { 8 }) },
    )
  val bottomAxis =
    HorizontalAxis.rememberBottom(
      line = line,
      guideline = guideline,
      label = label,
      tick = tick,
      valueFormatter = yearMonthXAxisFormatter(),
      itemPlacer = hItemPlacer(compact),
    )
  val marker = if (compact) null else rememberMarker()
  val markerListener = rememberMarkerHaptics(compact)
  val decorations = listOf(rememberChartZoomDecoration(zoom))

  // Vico takes layers as varargs, and there's always at least one of them
  return when {
    columnLayer != null && lineLayer != null ->
      rememberCartesianChart(
        columnLayer,
        lineLayer,
        startAxis = startAxis,
        bottomAxis = bottomAxis,
        marker = marker,
        markerVisibilityListener = markerListener,
        decorations = decorations,
      )

    columnLayer != null ->
      rememberCartesianChart(
        columnLayer,
        startAxis = startAxis,
        bottomAxis = bottomAxis,
        marker = marker,
        markerVisibilityListener = markerListener,
        decorations = decorations,
      )

    else ->
      rememberCartesianChart(
        checkNotNull(lineLayer),
        startAxis = startAxis,
        bottomAxis = bottomAxis,
        marker = marker,
        markerVisibilityListener = markerListener,
        decorations = decorations,
      )
  }
}

private suspend fun CartesianChartModelProducer.populate(
  data: BudgetAnalysisData,
  layers: Layers,
  zoom: XRange?,
) = runTransaction {
  zoomTo(zoom)
  if (data.items.isEmpty()) return@runTransaction
  val months = data.items.keys.map { it.monthNumber() }
  val values = data.items.values
  if (layers.columns.isNotEmpty()) {
    columnModel {
      layers.columns.forEach { kind ->
        series(x = months, y = values.map { kind.value(it).toDouble() }, zoom = zoom)
      }
    }
  }
  if (layers.lines.isNotEmpty()) {
    lineModel {
      layers.lines.forEach { kind ->
        series(x = months, y = values.map { kind.value(it).toDouble() }, zoom = zoom)
      }
    }
  }
}

@Preview
@Composable
private fun PreviewBudgetAnalysisChart(
    @PreviewParameter(BudgetAnalysisChartProvider::class)
  params: ColorParams<BudgetAnalysisChartParams>,
) =
  PreviewWithColorParams(params) {
    BudgetAnalysisChart(
      modifier =
        Modifier.background(colors.tableBackground, CardShape)
          .width(WIDTH.dp)
          .let { m -> if (compact) m.height(300.dp) else m }
          .padding(5.dp),
      data = data,
      compact = compact,
    )
  }

private data class BudgetAnalysisChartParams(val data: BudgetAnalysisData, val compact: Boolean)

private class BudgetAnalysisChartProvider :
  ColorParamsProvider<BudgetAnalysisChartParams>(
    BudgetAnalysisChartParams(PREVIEW_BUDGET_ANALYSIS_DATA, compact = true),
    BudgetAnalysisChartParams(PREVIEW_BUDGET_ANALYSIS_DATA, compact = false),
    BudgetAnalysisChartParams(
      PREVIEW_BUDGET_ANALYSIS_DATA.copy(graphType = Line),
      compact = false,
    ),
    BudgetAnalysisChartParams(
      PREVIEW_BUDGET_ANALYSIS_DATA.copy(balanceOnly = true),
      compact = true,
    ),
  )

private fun datum(budgeted: Double, spent: Double, overspending: Double, balance: Double) =
  BudgetAnalysisDatum(
    budgeted = Amount(budgeted),
    spent = Amount(spent),
    overspendingAdjustment = Amount(overspending),
    balance = Amount(balance),
  )

internal val PREVIEW_BUDGET_ANALYSIS_DATA =
  BudgetAnalysisData(
    title = "Budget Analysis",
    items =
      persistentMapOf(
        date(2026, JANUARY) to datum(2400.0, -2150.0, 0.0, 650.0),
        date(2026, FEBRUARY) to datum(2400.0, -2620.0, 0.0, 430.0),
        date(2026, MARCH) to datum(2400.0, -2310.0, 120.0, 520.0),
        date(2026, APRIL) to datum(2500.0, -2080.0, 0.0, 940.0),
        date(2026, MAY) to datum(2500.0, -2730.0, 0.0, 710.0),
        date(2026, JUNE) to datum(2500.0, -2290.0, 60.0, 920.0),
      ),
    graphType = Bar,
    showBalance = true,
    balanceOnly = false,
  )
