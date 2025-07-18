package actual.budget.reports.ui.charts

import actual.budget.reports.ui.charts.PreviewShared.WIDTH
import actual.budget.reports.vm.CashFlowData
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
import com.patrykandpatrick.vico.compose.cartesian.layer.continuous
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import kotlinx.coroutines.runBlocking

@Composable
internal fun CashFlowChart(
  data: CashFlowData,
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
      rememberColumnCartesianLayer(
        ColumnCartesianLayer.ColumnProvider.series(
          rememberLineComponent(fill = fill(theme.reportsBlue), thickness = 16.dp),
        ),
      ),
      rememberColumnCartesianLayer(
        ColumnCartesianLayer.ColumnProvider.series(
          rememberLineComponent(fill = fill(theme.reportsRed), thickness = 16.dp),
        ),
      ),
      rememberLineCartesianLayer(
        lineProvider = LineCartesianLayer.LineProvider.series(
          LineCartesianLayer.rememberLine(
            fill = LineCartesianLayer.LineFill.single(fill(theme.pageTextLight)),
            stroke = LineCartesianLayer.LineStroke.continuous(thickness = 3.dp),
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

private suspend fun CartesianChartModelProducer.populate(data: CashFlowData) = with(data.items) {
  runTransaction {
    columnSeries {
      series(
        x = keys.map { it.monthNumber() },
        y = values.map { it.income.toDouble() },
      )
    }

    columnSeries {
      series(
        x = keys.map { it.monthNumber() },
        y = values.map { it.expenses.toDouble() },
      )
    }

    lineSeries {
      series(
        x = keys.map { it.monthNumber() },
        y = values.map { it.balance.toDouble() },
      )
    }
  }
}

@Preview(widthDp = WIDTH)
@Composable
private fun PreviewRegular() = PreviewChart(compact = false)

@Preview(widthDp = WIDTH)
@Composable
private fun PreviewRegularPrivate() = PreviewChart(compact = false, isPrivacyEnabled = true)

@Preview(widthDp = WIDTH)
@Composable
private fun PreviewCompact() = PreviewChart(compact = true)

@Composable
private fun PreviewChart(
  compact: Boolean,
  isPrivacyEnabled: Boolean = false,
) = PreviewColumn(isPrivacyEnabled = isPrivacyEnabled) {
  CashFlowChart(
    modifier = Modifier
      .background(LocalTheme.current.tableBackground, CardShape)
      .width(WIDTH.dp)
      .padding(5.dp),
    data = PreviewCashFlow.DATA,
    compact = compact,
  )
}
