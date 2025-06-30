package actual.budget.reports.ui.charts

import actual.budget.model.Amount
import actual.budget.model.YearAndMonth
import actual.core.ui.CardShape
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewColumn
import actual.core.ui.Theme
import actual.core.ui.WithNumberFormatConfig
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
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
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Month

@Composable
internal fun NetWorthChart(
  modelProducer: CartesianChartModelProducer,
  compact: Boolean,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
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

@Preview(widthDp = PREVIEW_WIDTH) @Composable private fun MixedRegular() = PreviewChart(Data, compact = false)
@Preview(widthDp = PREVIEW_WIDTH) @Composable private fun MixedCompact() = PreviewChart(Data, compact = true)

@Preview(widthDp = PREVIEW_WIDTH) @Composable private fun PosRegular() = PreviewChart(DataPositive, compact = false)
@Preview(widthDp = PREVIEW_WIDTH) @Composable private fun PosCompact() = PreviewChart(DataPositive, compact = true)

@Preview(widthDp = PREVIEW_WIDTH) @Composable private fun NegRegular() = PreviewChart(DataNegative, compact = false)
@Preview(widthDp = PREVIEW_WIDTH) @Composable private fun NegCompact() = PreviewChart(DataNegative, compact = true)

@Composable
private fun PreviewChart(data: ImmutableMap<YearAndMonth, Amount>, compact: Boolean) = PreviewColumn {
  WithNumberFormatConfig {
    NetWorthChart(
      modifier = Modifier
        .background(LocalTheme.current.tableBackground, CardShape)
        .width(PREVIEW_WIDTH.dp)
        .padding(5.dp),
      modelProducer = previewModelProducer(data),
      compact = compact,
    )
  }
}

@Composable
private fun previewModelProducer(data: ImmutableMap<YearAndMonth, Amount>): CartesianChartModelProducer {
  val modelProducer = remember { CartesianChartModelProducer() }
  runBlocking {
    modelProducer.runTransaction {
      lineSeries {
        series(
          x = data.keys.map { it.monthNumber() },
          y = data.values.map { it.toDouble() },
        )
      }
    }
  }
  return modelProducer
}

private val Data = persistentMapOf(
  YearAndMonth(2023, Month.JANUARY) to Amount(-44_000.00),
  YearAndMonth(2024, Month.JANUARY) to Amount(-18_000.00),
  YearAndMonth(2024, Month.FEBRUARY) to Amount(21_000.34),
  YearAndMonth(2024, Month.MARCH) to Amount(25_000.67),
  YearAndMonth(2024, Month.JULY) to Amount(-10123.98),
  YearAndMonth(2024, Month.AUGUST) to Amount(-5123.69),
  YearAndMonth(2024, Month.DECEMBER) to Amount(-6789.12),
  YearAndMonth(2025, Month.JANUARY) to Amount(198.0),
  YearAndMonth(2025, Month.FEBRUARY) to Amount(1.98),
  YearAndMonth(2025, Month.MARCH) to Amount(256.0),
  YearAndMonth(2025, Month.JUNE) to Amount(30000.0),
)

private val DataPositive: PersistentMap<YearAndMonth, Amount> = Data
  .mapValues { (_, amount) -> amount + Amount(100_000.0) }
  .toPersistentMap()

private val DataNegative: PersistentMap<YearAndMonth, Amount> = Data
  .mapValues { (_, amount) -> amount - Amount(100_000.0) }
  .toPersistentMap()
