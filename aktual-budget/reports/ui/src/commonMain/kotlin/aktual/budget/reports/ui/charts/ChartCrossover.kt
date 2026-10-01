package aktual.budget.reports.ui.charts

import aktual.budget.model.Amount
import aktual.budget.reports.vm.CrossoverData
import aktual.budget.reports.vm.CrossoverDatum
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.CardShape
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.PreviewWithColors
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.multiplatform.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.multiplatform.cartesian.data.lineSeries
import com.patrykandpatrick.vico.multiplatform.cartesian.decoration.Decoration
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.multiplatform.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.multiplatform.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.multiplatform.common.DashedShape
import com.patrykandpatrick.vico.multiplatform.common.Fill
import com.patrykandpatrick.vico.multiplatform.common.component.rememberLineComponent
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Month.APRIL
import kotlinx.datetime.Month.AUGUST
import kotlinx.datetime.Month.FEBRUARY
import kotlinx.datetime.Month.JANUARY
import kotlinx.datetime.Month.JULY
import kotlinx.datetime.Month.JUNE
import kotlinx.datetime.Month.MARCH
import kotlinx.datetime.Month.MAY
import kotlinx.datetime.YearMonth

@Composable
internal fun CrossoverChart(
  data: CrossoverData,
  compact: Boolean,
  modifier: Modifier = Modifier,
  includeHeader: Boolean = true,
  zoom: ChartZoomState = rememberChartZoomState(data),
) =
  Column(modifier = modifier) {
    if (includeHeader) {
      Header(modifier = Modifier.fillMaxWidth(), data = data)
    }

    Chart(
      modifier = if (compact) Modifier.fillMaxSize() else Modifier.weight(1f),
      data = data,
      compact = compact,
      zoom = zoom,
    )

    if (!compact) {
      Footer(title = Strings.reportsCrossoverWhatTitle, text = Strings.reportsCrossoverWhat)
    }
  }

@Composable
private fun Header(
  data: CrossoverData,
  modifier: Modifier = Modifier,
) =
  Row(
    modifier = modifier.padding(start = 4.dp, end = 4.dp, top = 4.dp).fillMaxWidth(),
    verticalAlignment = CenterVertically,
  ) {
    Text(
      modifier = Modifier.weight(1f),
      text = data.title ?: Strings.reportsChooseTypeCrossover,
      overflow = Ellipsis,
      color = colors.pageText,
      style = typography.bodyLarge,
    )

    Column(horizontalAlignment = Alignment.End) {
      val years = data.yearsToRetire
      Text(
        text =
          if (years == null) {
            Strings.reportsCrossoverNone
          } else {
            Strings.reportsCrossoverYears("%.2f".format(years))
          },
        textAlign = End,
        style = typography.bodyLarge,
        color = colors.pageText,
      )

      VerticalSpacer(4.dp)

      Text(
        text = Strings.reportsCrossoverYearsToRetire,
        textAlign = End,
        style = typography.bodySmall,
        color = colors.pageTextSubdued,
      )
    }
  }

@Composable
private fun Chart(
  data: CrossoverData,
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
  val crossoverLine = rememberCrossoverLine(data)

  ZoomableChart(modifier = modifier, state = zoom, enabled = !compact) { chartModifier ->
    CartesianChartHost(
      modifier = chartModifier,
      modelProducer = modelProducer,
      scrollState = rememberVicoScrollState(scrollEnabled = false),
      chart =
        rememberCartesianChart(
          rememberLineCartesianLayer(
            rangeProvider = remember { ZoomRangeProvider() },
            lineProvider =
              LineCartesianLayer.LineProvider.series(
                LineCartesianLayer.rememberLine(
                  fill = LineCartesianLayer.LineFill.single(Fill(colors.reportsNumberPositive)),
                  pointConnector = LineCartesianLayer.PointConnector.cubic(),
                ),
                LineCartesianLayer.rememberLine(
                  fill = LineCartesianLayer.LineFill.single(Fill(colors.reportsNumberNegative)),
                  pointConnector = LineCartesianLayer.PointConnector.cubic(),
                ),
                LineCartesianLayer.rememberLine(
                  fill = LineCartesianLayer.LineFill.single(Fill(colors.reportsNumberNegative)),
                  stroke =
                    LineCartesianLayer.LineStroke.Dashed(dashLength = 5.dp, gapLength = 5.dp),
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
            ),
          bottomAxis =
            HorizontalAxis.rememberBottom(
              line = line,
              guideline = guideline,
              label = label,
              tick = tick,
              valueFormatter = yearMonthXAxisFormatter(),
              itemPlacer = hItemPlacer(compact),
            ),
          marker = if (compact) null else rememberMarker(),
          markerVisibilityListener = rememberMarkerHaptics(compact),
          decorations = listOfNotNull(crossoverLine, rememberChartZoomDecoration(zoom)),
        ),
    )
  }
}

@Composable
private fun rememberCrossoverLine(data: CrossoverData): Decoration? {
  val line =
    rememberLineComponent(
      fill = Fill(colors.noticeText),
      thickness = 1.dp,
      shape = remember { DashedShape(dashLength = 4.dp, gapLength = 4.dp) },
    )
  val month = data.crossover ?: return null
  return remember(line, month) { VerticalLine(x = month.monthNumber().toDouble(), line = line) }
}

private suspend fun CartesianChartModelProducer.populate(data: CrossoverData, zoom: XRange?) =
  runTransaction {
    zoomTo(zoom)
    if (data.items.isEmpty()) return@runTransaction
    val months = data.items.keys.map { it.monthNumber() }
    val adjusted =
      data.items.mapNotNull { (month, datum) ->
        datum.adjustedExpenses?.let { month.monthNumber() to it.toDouble() }
      }
    lineSeries {
      series(x = months, y = data.items.values.map { it.investmentIncome.toDouble() }, zoom = zoom)
      series(x = months, y = data.items.values.map { it.expenses.toDouble() }, zoom = zoom)
      if (adjusted.isNotEmpty()) {
        series(x = adjusted.map { it.first }, y = adjusted.map { it.second }, zoom = zoom)
      }
    }
  }

@Preview
@Composable
private fun PreviewCrossoverChart(
  @PreviewParameter(CrossoverChartProvider::class) params: ColoredParams<CrossoverChartParams>
) =
  PreviewWithColors(params.colors) {
    CrossoverChart(
      modifier =
        Modifier.background(colors.tableBackground, CardShape)
          .width(WIDTH.dp)
          .let { m -> if (params.data.compact) m.height(300.dp) else m }
          .padding(5.dp),
      data = params.data.data,
      compact = params.data.compact,
    )
  }

private data class CrossoverChartParams(val data: CrossoverData, val compact: Boolean)

private class CrossoverChartProvider :
  ColoredParameterProvider<CrossoverChartParams>(
    CrossoverChartParams(PREVIEW_CROSSOVER_DATA, compact = true),
    CrossoverChartParams(PREVIEW_CROSSOVER_DATA, compact = false),
    CrossoverChartParams(
      PREVIEW_CROSSOVER_DATA.copy(crossover = null, yearsToRetire = null),
      compact = true,
    ),
  )

private fun datum(income: Double, expenses: Double, nestEgg: Double, adjusted: Double? = null) =
  CrossoverDatum(
    investmentIncome = Amount(income),
    expenses = Amount(expenses),
    nestEgg = Amount(nestEgg),
    adjustedExpenses = adjusted?.let(::Amount),
  )

internal val PREVIEW_CROSSOVER_DATA =
  CrossoverData(
    title = "Crossover Point",
    items =
      persistentMapOf(
        date(2026, JANUARY) to datum(income = 1000.0, expenses = 2100.0, nestEgg = 300_000.0),
        date(2026, FEBRUARY) to datum(income = 1150.0, expenses = 1900.0, nestEgg = 345_000.0),
        date(2026, MARCH) to datum(income = 1300.0, expenses = 2300.0, nestEgg = 390_000.0),
        date(2026, APRIL) to datum(income = 1450.0, expenses = 1800.0, nestEgg = 435_000.0),
        date(2026, MAY) to datum(1600.0, 2000.0, 480_000.0, adjusted = 2000.0),
        date(2026, JUNE) to datum(1750.0, 2000.0, 525_000.0, adjusted = 2000.0),
        date(2026, JULY) to datum(1900.0, 2000.0, 570_000.0, adjusted = 2000.0),
        date(2026, AUGUST) to datum(2050.0, 2000.0, 615_000.0, adjusted = 2000.0),
      ),
    crossover = YearMonth(2026, AUGUST),
    yearsToRetire = 0.83,
  )
