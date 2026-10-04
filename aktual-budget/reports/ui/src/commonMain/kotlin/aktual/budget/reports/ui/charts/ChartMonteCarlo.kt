package aktual.budget.reports.ui.charts

import aktual.budget.model.Amount
import aktual.budget.reports.vm.MonteCarloBand
import aktual.budget.reports.vm.MonteCarloData
import aktual.core.l10n.Plurals
import aktual.core.l10n.Strings
import aktual.core.model.Percent
import aktual.core.model.percent
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.CardShape
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.formattedString
import aktual.core.ui.isInPreview
import alakazam.compose.VerticalSpacer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.multiplatform.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.multiplatform.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.multiplatform.cartesian.data.lineSeries
import com.patrykandpatrick.vico.multiplatform.cartesian.decoration.Decoration
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.multiplatform.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.multiplatform.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.multiplatform.common.Fill
import kotlin.math.exp
import kotlin.math.roundToInt
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.runBlocking

@Composable
internal fun MonteCarloChart(
  data: MonteCarloData,
  compact: Boolean,
  modifier: Modifier = Modifier,
  includeHeader: Boolean = true,
  zoom: ChartZoomState = rememberChartZoomState(data),
) =
  Column(modifier = modifier) {
    if (includeHeader) {
      Header(modifier = Modifier.fillMaxWidth(), data = data)
    }

    val chartModifier = if (compact) Modifier.fillMaxSize() else Modifier.weight(1f)
    Chart(modifier = chartModifier, data = data, compact = compact, zoom = zoom)

    if (!compact) {
      Summary(data, Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp))
      Footer(title = Strings.reportsMonteCarloHowTitle, text = Strings.reportsMonteCarloHow)
    }
  }

@Composable
private fun Header(
  data: MonteCarloData,
  modifier: Modifier = Modifier,
) =
  Row(
    modifier = modifier.padding(start = 4.dp, end = 4.dp, top = 4.dp).fillMaxWidth(),
    verticalAlignment = CenterVertically,
  ) {
    Text(
      modifier = Modifier.weight(1f),
      text = data.title ?: Strings.reportsChooseTypeMonteCarlo,
      overflow = Ellipsis,
      color = colors.pageText,
      style = typography.bodyLarge,
    )

    Column(horizontalAlignment = Alignment.End) {
      Text(
        text = data.successRate.toString(decimalPlaces = 1),
        textAlign = End,
        style = typography.bodyLarge,
        color = successColor(data.successRate),
      )

      VerticalSpacer(4.dp)

      Text(
        text = Strings.reportsMonteCarloSuccessToAge(data.targetAge),
        textAlign = End,
        style = typography.bodySmall,
        color = colors.pageTextSubdued,
      )
    }
  }

@Composable
private fun Summary(data: MonteCarloData, modifier: Modifier = Modifier) =
  Column(modifier = modifier) {
    val style = typography.bodySmall
    val color = colors.pageTextSubdued
    Text(
      text = Strings.reportsMonteCarloMedianEnding(data.medianEndingBalance.formattedString()),
      style = style,
      color = color,
    )

    data.medianDepletionAge?.let { age ->
      Text(text = Strings.reportsMonteCarloRunsOut(age), style = style, color = color)
    }

    Text(
      text = Plurals.reportsMonteCarloSimulations(data.simulationCount, data.simulationCount),
      style = style,
      color = color,
    )
  }

// Upstream's thresholds
@Composable
@ReadOnlyComposable
internal fun successColor(successRate: Percent): Color {
  val percent = successRate.doubleValue
  return when {
    percent >= GOOD_SUCCESS -> colors.reportsNumberPositive
    percent >= OK_SUCCESS -> colors.warningText
    else -> colors.reportsNumberNegative
  }
}

@Composable
private fun Chart(
  data: MonteCarloData,
  compact: Boolean,
  zoom: ChartZoomState,
  modifier: Modifier = Modifier,
) {
  val modelProducer = remember { CartesianChartModelProducer() }

  if (isInPreview()) {
    runBlocking { modelProducer.populate(data, zoom.range) }
  } else {
    LaunchedEffect(data, zoom.range) { modelProducer.populate(data, zoom.range) }
  }

  val label = axisLabelComponent(compact)
  val tick = axisTickComponent(compact)
  val guideline = axisGuidelineComponent(compact)
  val line = axisLineComponent(compact)
  val fill = colors.reportsChartFill
  val edge = Fill(fill.copy(alpha = EDGE_ALPHA))

  ZoomableChart(modifier = modifier, state = zoom, enabled = !compact) { chartModifier ->
    CartesianChartHost(
      modifier = chartModifier,
      modelProducer = modelProducer,
      scrollState = rememberVicoScrollState(scrollEnabled = false),
      animationSpec = chartAnimationSpec(compact),
      chart =
        rememberCartesianChart(
          rememberLineCartesianLayer(
            rangeProvider = remember { ZoomRangeProvider() },
            lineProvider =
              LineCartesianLayer.LineProvider.series(
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
              ),
          ),
          startAxis =
            VerticalAxis.rememberStart(
              line = line,
              guideline = guideline,
              label = label,
              tick = tick,
              valueFormatter = amountYAxisFormatter(),
              itemPlacer = remember { VerticalAxis.ItemPlacer.count(count = { 8 }) },
            ),
          bottomAxis =
            HorizontalAxis.rememberBottom(
              line = line,
              guideline = guideline,
              label = label,
              tick = tick,
              valueFormatter =
                remember { CartesianValueFormatter { _, value, _ -> "${value.roundToInt()}" } },
              itemPlacer = ageItemPlacer(data.bands.size),
            ),
          marker = if (compact) null else rememberMarker(),
          markerVisibilityListener = rememberMarkerHaptics(compact),
          decorations =
            remember(data, fill) {
              [
                PercentileBand(
                  data.bands,
                  fill.copy(alpha = OUTER_ALPHA),
                  MonteCarloBand::age,
                  MonteCarloBand::p10,
                  MonteCarloBand::p90,
                ),
                PercentileBand(
                  data.bands,
                  fill.copy(alpha = INNER_ALPHA),
                  MonteCarloBand::age,
                  MonteCarloBand::p25,
                  MonteCarloBand::p75,
                ),
              ]
            } + rememberChartZoomDecoration(zoom),
        ),
    )
  }
}

@Composable
internal fun ageItemPlacer(ageCount: Int): HorizontalAxis.ItemPlacer {
  val spacing = (ageCount / AGE_LABELS).coerceAtLeast(1)
  return remember(spacing) {
    HorizontalAxis.ItemPlacer.aligned(offset = { 0 }, spacing = { spacing })
  }
}

// Fills the area between two percentiles, since vico's area fills only run down to a fixed y
internal class PercentileBand<T>(
  private val bands: List<T>,
  private val color: Color,
  private val age: (T) -> Int,
  private val lower: (T) -> Amount,
  private val upper: (T) -> Amount,
) : Decoration {
  private val path = Path()
  private val paint = Paint()

  override fun drawUnderLayers(context: CartesianDrawingContext) =
    with(context) {
      if (bands.isEmpty()) return@with
      val yRange = ranges.getYRange(null)
      val start =
        (if (isLtr) layerBounds.left else layerBounds.right) +
          layoutDirectionMultiplier * layerDimensions.startPadding - scroll

      fun canvasX(age: Int): Float {
        val steps = ((age - ranges.minX) / ranges.xStep).toFloat()
        return start + layoutDirectionMultiplier * layerDimensions.xSpacing * steps
      }

      fun canvasY(amount: Amount): Float =
        layerBounds.bottom -
          ((amount.toDouble() - yRange.minY) / yRange.length).toFloat() * layerBounds.height

      path.rewind()
      bands.forEachIndexed { i, band ->
        val x = canvasX(age(band))
        val y = canvasY(upper(band))
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
      }
      for (i in bands.indices.reversed()) {
        path.lineTo(canvasX(age(bands[i])), canvasY(lower(bands[i])))
      }
      path.close()
      paint.color = color
      // Layers are clipped to their bounds but decorations aren't, so clip to hide zoomed out bands
      canvas.save()
      canvas.clipRect(layerBounds)
      canvas.drawPath(path, paint)
      canvas.restore()
    }
}

private suspend fun CartesianChartModelProducer.populate(data: MonteCarloData, zoom: XRange?) =
  runTransaction {
    zoomTo(zoom)
    val ages = data.bands.map { it.age }
    lineSeries {
      series(x = ages, y = data.bands.map { it.p90.toDouble() }, zoom = zoom)
      series(x = ages, y = data.bands.map { it.p50.toDouble() }, zoom = zoom)
      series(x = ages, y = data.bands.map { it.p10.toDouble() }, zoom = zoom)
    }
  }

private const val GOOD_SUCCESS = 75.0
private const val OK_SUCCESS = 50.0
internal const val EDGE_ALPHA = 0.4f
internal const val OUTER_ALPHA = 0.15f
internal const val INNER_ALPHA = 0.3f
private const val AGE_LABELS = 6

@Preview
@Composable
private fun PreviewMonteCarloChart(
  @PreviewParameter(MonteCarloChartProvider::class) params: ColoredParams<MonteCarloChartParams>
) =
  PreviewWithColors(params.colors) {
    MonteCarloChart(
      modifier =
        Modifier.background(colors.tableBackground, CardShape)
          .width(WIDTH.dp)
          .let { m -> if (params.data.compact) m.height(300.dp) else m.height(600.dp) }
          .padding(5.dp),
      data = params.data.data,
      compact = params.data.compact,
    )
  }

private data class MonteCarloChartParams(val data: MonteCarloData, val compact: Boolean)

private class MonteCarloChartProvider :
  ColoredParameterProvider<MonteCarloChartParams>(
    MonteCarloChartParams(PREVIEW_MONTE_CARLO_DATA, compact = true),
    MonteCarloChartParams(PREVIEW_MONTE_CARLO_DATA, compact = false),
  )

// A fan that widens and drifts down over 30 years, like the default plan
internal val PREVIEW_MONTE_CARLO_DATA =
  MonteCarloData(
    title = "Monte Carlo Analysis",
    successRate = 76.7.percent,
    currentAge = 60,
    targetAge = 90,
    bands =
      (0..30)
        .map { year ->
          val median = 500_000.0 * exp(-year / 40.0) - year * 4_000.0
          val spread = 12_000.0 * year
          fun amount(value: Double) = Amount(value.coerceAtLeast(0.0))
          MonteCarloBand(
            age = 60 + year,
            p10 = amount(median - 1.4 * spread),
            p25 = amount(median - 0.7 * spread),
            p50 = amount(median),
            p75 = amount(median + 0.9 * spread),
            p90 = amount(median + 2.0 * spread),
          )
        }
        .toImmutableList(),
    medianEndingBalance = Amount(212_508.85),
    medianDepletionAge = 85,
    simulationCount = 5000,
  )
