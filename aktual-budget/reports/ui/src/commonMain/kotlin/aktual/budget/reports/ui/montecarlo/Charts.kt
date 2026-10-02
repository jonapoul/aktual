package aktual.budget.reports.ui.montecarlo

import aktual.budget.model.Amount
import aktual.budget.reports.ui.charts.EDGE_ALPHA
import aktual.budget.reports.ui.charts.INNER_ALPHA
import aktual.budget.reports.ui.charts.OUTER_ALPHA
import aktual.budget.reports.ui.charts.PercentileBand
import aktual.budget.reports.ui.charts.VerticalLine
import aktual.budget.reports.ui.charts.ageItemPlacer
import aktual.budget.reports.ui.charts.amountYAxisFormatter
import aktual.budget.reports.ui.charts.axisGuidelineComponent
import aktual.budget.reports.ui.charts.axisLabelComponent
import aktual.budget.reports.ui.charts.axisLineComponent
import aktual.budget.reports.ui.charts.axisTickComponent
import aktual.budget.reports.ui.charts.chartAnimationSpec
import aktual.budget.reports.ui.charts.rememberMarker
import aktual.budget.reports.ui.charts.rememberMarkerHaptics
import aktual.budget.reports.vm.montecarlo.CashflowChart
import aktual.budget.reports.vm.montecarlo.MonteCarloDepletion
import aktual.budget.reports.vm.montecarlo.MonteCarloFanBand
import aktual.budget.reports.vm.montecarlo.MonteCarloGraphView
import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.isInPreview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.multiplatform.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.multiplatform.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.multiplatform.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.multiplatform.cartesian.data.columnSeries
import com.patrykandpatrick.vico.multiplatform.cartesian.data.lineSeries
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.multiplatform.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.multiplatform.cartesian.marker.CartesianMarkerController
import com.patrykandpatrick.vico.multiplatform.cartesian.marker.CartesianMarkerVisibilityListener
import com.patrykandpatrick.vico.multiplatform.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.multiplatform.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.multiplatform.common.DashedShape
import com.patrykandpatrick.vico.multiplatform.common.Fill
import com.patrykandpatrick.vico.multiplatform.common.component.LineComponent
import com.patrykandpatrick.vico.multiplatform.common.component.rememberLineComponent
import com.patrykandpatrick.vico.multiplatform.common.data.ExtraStore
import kotlin.math.roundToInt
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.runBlocking

// MonteCarloGraph: the fan of percentile bands, or one line for a focused view
@Composable
internal fun FanChart(
  bands: ImmutableList<MonteCarloFanBand>,
  view: MonteCarloGraphView,
  modifier: Modifier = Modifier,
) {
  val modelProducer = remember { CartesianChartModelProducer() }
  if (isInPreview()) {
    runBlocking { modelProducer.populateFan(bands, view) }
  } else {
    LaunchedEffect(bands, view) { modelProducer.populateFan(bands, view) }
  }

  val fill = colors.reportsChartFill
  val edge = Fill(fill.copy(alpha = EDGE_ALPHA))
  val lines =
    if (view == MonteCarloGraphView.All) {
      listOf(
        LineCartesianLayer.rememberLine(
          fill = LineCartesianLayer.LineFill.single(edge),
          stroke = LineCartesianLayer.LineStroke.Continuous(thickness = 1.dp),
        ),
        LineCartesianLayer.rememberLine(
          fill = LineCartesianLayer.LineFill.single(Fill(fill)),
          stroke = LineCartesianLayer.LineStroke.Continuous(thickness = 2.dp),
        ),
        LineCartesianLayer.rememberLine(
          fill = LineCartesianLayer.LineFill.single(edge),
          stroke = LineCartesianLayer.LineStroke.Continuous(thickness = 1.dp),
        ),
      )
    } else {
      listOf(
        LineCartesianLayer.rememberLine(
          fill = LineCartesianLayer.LineFill.single(Fill(fill)),
          stroke = LineCartesianLayer.LineStroke.Continuous(thickness = 2.dp),
          areaFill = LineCartesianLayer.AreaFill.single(Fill(fill.copy(alpha = FOCUSED_ALPHA))),
        )
      )
    }

  val decorations =
    remember(bands, view, fill) {
      if (view == MonteCarloGraphView.All) {
        listOf(
          PercentileBand(
            bands,
            fill.copy(alpha = OUTER_ALPHA),
            MonteCarloFanBand::age,
            MonteCarloFanBand::p10,
            MonteCarloFanBand::p90,
          ),
          PercentileBand(
            bands,
            fill.copy(alpha = INNER_ALPHA),
            MonteCarloFanBand::age,
            MonteCarloFanBand::p25,
            MonteCarloFanBand::p75,
          ),
        )
      } else {
        emptyList()
      }
    }

  CartesianChartHost(
    modifier = modifier,
    modelProducer = modelProducer,
    scrollState = rememberVicoScrollState(scrollEnabled = false),
    animationSpec = chartAnimationSpec(compact = false),
    chart =
      rememberCartesianChart(
        rememberLineCartesianLayer(lineProvider = LineCartesianLayer.LineProvider.series(lines)),
        startAxis = amountAxis(),
        bottomAxis = ageAxis(bands.size),
        marker = rememberMarker(),
        markerVisibilityListener = rememberMarkerHaptics(compact = false),
        decorations = decorations,
      ),
  )
}

private suspend fun CartesianChartModelProducer.populateFan(
  bands: List<MonteCarloFanBand>,
  view: MonteCarloGraphView,
) = runTransaction {
  val ages = bands.map { it.age }
  lineSeries {
    if (view == MonteCarloGraphView.All) {
      series(x = ages, y = bands.map { it.p90.toDouble() })
      series(x = ages, y = bands.map { it.p50.toDouble() })
      series(x = ages, y = bands.map { it.p10.toDouble() })
    } else {
      series(x = ages, y = bands.map { view.value(it).toDouble() })
    }
  }
}

// VIEW_DATA_KEYS: the value each focused view plots
private fun MonteCarloGraphView.value(band: MonteCarloFanBand): Amount =
  when (this) {
    SingleWorst -> band.worstRun
    WorstCase -> band.p5
    Pessimistic -> band.p30
    MonteCarloGraphView.Median -> band.p50
    Optimistic -> band.p70
    All -> band.p50
  }

// MonteCarloHistogram: failed runs by the age they ran out at, with the median failure marked.
// Tapping a bar selects its age
@Composable
internal fun DepletionHistogram(
  depletions: ImmutableList<MonteCarloDepletion>,
  medianDepletionAge: Int?,
  onSelectAge: (Int?) -> Unit,
  modifier: Modifier = Modifier,
) {
  val modelProducer = remember { CartesianChartModelProducer() }
  if (isInPreview()) {
    runBlocking { modelProducer.populateHistogram(depletions) }
  } else {
    LaunchedEffect(depletions) { modelProducer.populateHistogram(depletions) }
  }

  val medianLine =
    rememberLineComponent(
      fill = Fill(colors.noticeText),
      thickness = 1.dp,
      shape = remember { DashedShape() },
    )

  CartesianChartHost(
    modifier = modifier,
    modelProducer = modelProducer,
    scrollState = rememberVicoScrollState(scrollEnabled = false),
    animationSpec = chartAnimationSpec(compact = false),
    chart =
      rememberCartesianChart(
        rememberColumnCartesianLayer(
          ColumnCartesianLayer.ColumnProvider.series(
            rememberLineComponent(fill = Fill(colors.reportsNumberNegative), thickness = 6.dp)
          ),
          columnCollectionSpacing = 2.dp,
        ),
        startAxis =
          VerticalAxis.rememberStart(
            line = axisLineComponent(compact = false),
            guideline = axisGuidelineComponent(compact = false),
            label = axisLabelComponent(compact = false),
            tick = axisTickComponent(compact = false),
            valueFormatter =
              remember { CartesianValueFormatter { _, value, _ -> "${value.roundToInt()}" } },
            itemPlacer = remember { VerticalAxis.ItemPlacer.step(step = { WHOLE_STEP }) },
          ),
        bottomAxis = ageAxis(depletions.size),
        marker = rememberGuidelineMarker(),
        markerController = CartesianMarkerController.rememberToggleOnTap(),
        markerVisibilityListener = rememberSelectionListener(onSelectAge),
        decorations =
          remember(medianDepletionAge, medianLine) {
            listOfNotNull(medianDepletionAge?.let { VerticalLine(it.toDouble(), medianLine) })
          },
      ),
  )
}

private suspend fun CartesianChartModelProducer.populateHistogram(
  depletions: List<MonteCarloDepletion>
) = runTransaction {
  columnSeries { series(x = depletions.map { it.age }, y = depletions.map { it.count }) }
}

// MonteCarloCashflowGraph: money in stacked above zero, money out below. Years after the plan ran
// out are dimmed, since only their unfunded spending remains. Tapping a year selects it
@Composable
internal fun CashflowBars(
  chart: CashflowChart,
  onSelectAge: (Int?) -> Unit,
  modifier: Modifier = Modifier,
) {
  val modelProducer = remember { CartesianChartModelProducer() }
  if (isInPreview()) {
    runBlocking { modelProducer.populateCashflow(chart) }
  } else {
    LaunchedEffect(chart) { modelProducer.populateCashflow(chart) }
  }

  val theme = colors
  val columnProvider =
    remember(chart, theme) {
      val depletedAges = chart.years.filter { it.afterDepletion }.map { it.age.toDouble() }.toSet()
      CashflowColumns(
        columns = chart.series.map { series -> column(seriesColor(series.colorIndex, theme)) },
        dimmed =
          chart.series.map { series ->
            column(seriesColor(series.colorIndex, theme).copy(alpha = UNFUNDED_ALPHA))
          },
        // Only spending remains after the plan runs out
        dimsAfterDepletion = chart.series.map { it.kind == Phase },
        depletedAges = depletedAges,
      )
    }

  CartesianChartHost(
    modifier = modifier,
    modelProducer = modelProducer,
    scrollState = rememberVicoScrollState(scrollEnabled = false),
    animationSpec = chartAnimationSpec(compact = false),
    chart =
      rememberCartesianChart(
        rememberColumnCartesianLayer(
          columnProvider = columnProvider,
          columnCollectionSpacing = 2.dp,
          mergeMode = { ColumnCartesianLayer.MergeMode.Stacked },
        ),
        startAxis = amountAxis(),
        bottomAxis = ageAxis(chart.years.size),
        marker = rememberGuidelineMarker(),
        markerController = CartesianMarkerController.rememberToggleOnTap(),
        markerVisibilityListener = rememberSelectionListener(onSelectAge),
      ),
  )
}

private suspend fun CartesianChartModelProducer.populateCashflow(chart: CashflowChart) =
  runTransaction {
    val ages = chart.years.map { it.age }
    columnSeries {
      chart.series.indices.forEach { i ->
        series(x = ages, y = chart.years.map { Amount(it.amounts[i]).toDouble() })
      }
    }
  }

private fun column(color: Color) = LineComponent(fill = Fill(color), thickness = COLUMN_THICKNESS)

// The theme's qualitative palette, with tax in the negative colour
internal fun seriesColor(colorIndex: Int, theme: Colors): Color =
  with(theme) {
    listOf(
        chartQual1,
        chartQual2,
        chartQual3,
        chartQual4,
        chartQual5,
        chartQual6,
        chartQual7,
        chartQual8,
        chartQual9,
      )
      .getOrElse(colorIndex) { reportsNumberNegative }
  }

private class CashflowColumns(
  private val columns: List<LineComponent>,
  private val dimmed: List<LineComponent>,
  private val dimsAfterDepletion: List<Boolean>,
  private val depletedAges: Set<Double>,
) : ColumnCartesianLayer.ColumnProvider {
  override fun getColumn(
    entry: ColumnCartesianLayerModel.Entry,
    seriesIndex: Int,
    extraStore: ExtraStore,
  ): LineComponent =
    if (dimsAfterDepletion[seriesIndex] && entry.x in depletedAges) {
      dimmed[seriesIndex]
    } else {
      columns[seriesIndex]
    }

  override fun getWidestSeriesColumn(seriesIndex: Int, extraStore: ExtraStore) =
    columns[seriesIndex]
}

@Composable
private fun amountAxis() =
  VerticalAxis.rememberStart(
    line = axisLineComponent(compact = false),
    guideline = axisGuidelineComponent(compact = false),
    label = axisLabelComponent(compact = false),
    tick = axisTickComponent(compact = false),
    valueFormatter = amountYAxisFormatter(),
    itemPlacer = remember { VerticalAxis.ItemPlacer.count(count = { AMOUNT_LABELS }) },
  )

@Composable
private fun ageAxis(ageCount: Int) =
  HorizontalAxis.rememberBottom(
    line = axisLineComponent(compact = false),
    guideline = null,
    label = axisLabelComponent(compact = false),
    tick = axisTickComponent(compact = false),
    valueFormatter =
      remember { CartesianValueFormatter { _, value, _ -> "${value.roundToInt()}" } },
    itemPlacer = ageItemPlacer(ageCount),
  )

// Marks the selected age with a guideline, leaving the details to the panel under the chart
@Composable
private fun rememberGuidelineMarker(): CartesianMarker {
  val line = rememberLineComponent(fill = Fill(colors.pageTextSubdued), thickness = 1.dp)
  return remember(line) { GuidelineMarker(line) }
}

private class GuidelineMarker(private val line: LineComponent) : CartesianMarker {
  override fun drawOverLayers(
    context: CartesianDrawingContext,
    targets: List<CartesianMarker.Target>,
  ) =
    with(context) {
      for (target in targets) {
        line.drawVertical(context, target.canvasX, layerBounds.top, layerBounds.bottom)
      }
    }
}

@Composable
private fun rememberSelectionListener(
  onSelectAge: (Int?) -> Unit
): CartesianMarkerVisibilityListener {
  val currentOnSelectAge by rememberUpdatedState(onSelectAge)
  return remember {
    object : CartesianMarkerVisibilityListener {
      override fun onShown(marker: CartesianMarker, targets: List<CartesianMarker.Target>) =
        currentOnSelectAge(targets.firstOrNull()?.x?.roundToInt())

      override fun onUpdated(marker: CartesianMarker, targets: List<CartesianMarker.Target>) =
        currentOnSelectAge(targets.firstOrNull()?.x?.roundToInt())

      override fun onHidden(marker: CartesianMarker) = currentOnSelectAge(null)
    }
  }
}

private const val FOCUSED_ALPHA = 0.08f
private const val UNFUNDED_ALPHA = 0.45f
private const val AMOUNT_LABELS = 8
private const val WHOLE_STEP = 1.0
private val COLUMN_THICKNESS = 10.dp
