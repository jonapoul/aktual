package actual.budget.reports.ui.charts

import actual.budget.reports.ui.charts.PreviewShared.WIDTH
import actual.budget.reports.vm.NetWorthData
import actual.core.ui.CardShape
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewColumn
import actual.core.ui.Theme
import actual.core.ui.isInPreview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import kotlinx.coroutines.runBlocking

@Composable
internal fun NetWorthChart(
  data: NetWorthData,
  compact: Boolean,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  val modelProducer = remember { CartesianChartModelProducer() }

  if (isInPreview()) {
    runBlocking { modelProducer.populate(data) }
  } else {
    LaunchedEffect(data) { modelProducer.populate(data) }
  }

  val label = axisLabelComponent(compact)
  val tick = axisTickComponent(compact)
  val guideline = axisGuidelineComponent(compact)
  val line = axisLineComponent(compact)

  CartesianChartHost(
    modifier = modifier,
    modelProducer = modelProducer,
    scrollState = rememberVicoScrollState(scrollEnabled = false),
    chart = rememberCartesianChart(
      rememberLineCartesianLayer(
        lineProvider = LineCartesianLayer.LineProvider.series(
          LineCartesianLayer.rememberLine(
            fill = LineCartesianLayer.LineFill.single(fill(theme.reportsBlue)),
            areaFill = LineCartesianLayer.AreaFill.double(
              topFill = fill(theme.reportsBlue.copy(alpha = 0.2f)),
              bottomFill = fill(theme.reportsRed.copy(alpha = 0.2f)),
            ),
          ),
        ),
      ),
      startAxis = VerticalAxis.rememberStart(
        line = line,
        guideline = guideline,
        label = label,
        tick = tick,
        valueFormatter = yAxisFormatter(),
        itemPlacer = remember { VerticalAxis.ItemPlacer.count(count = { 8 }) },
      ),
      bottomAxis = HorizontalAxis.rememberBottom(
        line = line,
        guideline = guideline,
        label = label,
        tick = tick,
        valueFormatter = xAxisFormatter(),
        itemPlacer = hItemPlacer(compact),
      ),
      marker = if (compact) null else rememberMarker(),
    ),
  )
}

private suspend fun CartesianChartModelProducer.populate(data: NetWorthData) = with(data.items) {
  runTransaction {
    lineSeries {
      series(
        x = keys.map { it.monthNumber() },
        y = values.map { it.toDouble() },
      )
    }
  }
}

@Preview(widthDp = WIDTH)
@Composable
private fun MixedRegular() = PreviewChart(PreviewNetWorth.DATA, compact = false)

@Preview(widthDp = WIDTH)
@Composable
private fun MixedCompact() = PreviewChart(PreviewNetWorth.DATA, compact = true)

@Preview(widthDp = WIDTH)
@Composable
private fun PositiveRegular() = PreviewChart(PreviewNetWorth.POSITIVE_DATA, compact = false)

@Preview(widthDp = WIDTH)
@Composable
private fun PositiveCompact() = PreviewChart(PreviewNetWorth.POSITIVE_DATA, compact = true)

@Preview(widthDp = WIDTH)
@Composable
private fun NegativeRegular() = PreviewChart(PreviewNetWorth.NEGATIVE_DATA, compact = false)

@Preview(widthDp = WIDTH)
@Composable
private fun NegativeCompact() = PreviewChart(PreviewNetWorth.NEGATIVE_DATA, compact = true)

@Composable
private fun PreviewChart(data: NetWorthData, compact: Boolean) = PreviewColumn {
  NetWorthChart(
    modifier = Modifier
      .background(LocalTheme.current.tableBackground, CardShape)
      .width(WIDTH.dp)
      .padding(5.dp),
    data = data,
    compact = compact,
  )
}
