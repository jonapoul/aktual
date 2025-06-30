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
import androidx.compose.runtime.Immutable
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
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Month
import kotlin.collections.map

@Composable
internal fun CashFlowChart(
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

@Preview(widthDp = PREVIEW_WIDTH) @Composable private fun PreviewRegular() = PreviewChart(Data, compact = false)
@Preview(widthDp = PREVIEW_WIDTH) @Composable private fun PreviewCompact() = PreviewChart(Data, compact = true)

@Composable
private fun PreviewChart(data: ImmutableMap<YearAndMonth, CashFlowDatum>, compact: Boolean) = PreviewColumn {
  WithNumberFormatConfig {
    CashFlowChart(
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
private fun previewModelProducer(data: ImmutableMap<YearAndMonth, CashFlowDatum>): CartesianChartModelProducer {
  val modelProducer = remember { CartesianChartModelProducer() }
  runBlocking {
    modelProducer.runTransaction {
      columnSeries {
        series(
          x = data.keys.map { it.monthNumber() },
          y = data.values.map { it.income.toDouble() },
        )
      }

      columnSeries {
        series(
          x = data.keys.map { it.monthNumber() },
          y = data.values.map { it.expenses.toDouble() },
        )
      }

      lineSeries {
        series(
          x = data.keys.map { it.monthNumber() },
          y = data.values.map { it.balance.toDouble() },
        )
      }
    }
  }
  return modelProducer
}

@Immutable
data class CashFlowDatum(
  val income: Amount,
  val expenses: Amount,
  val transfers: Amount,
  val balance: Amount,
  val change: Amount = income + expenses + transfers,
)

private fun datum(income: Int, expenses: Int, transfers: Int, balance: Int) = CashFlowDatum(
  income = Amount(income.toDouble()),
  expenses = Amount(expenses.toDouble()),
  transfers = Amount(transfers.toDouble()),
  balance = Amount(balance.toDouble()),
)

private val Data = persistentMapOf(
  YearAndMonth(2024, Month.JULY) to datum(income = 6683, expenses = -4695, transfers = -1779, balance = 4781),
  YearAndMonth(2024, Month.AUGUST) to datum(income = 6071, expenses = -4111, transfers = -729, balance = 6012),
  YearAndMonth(2024, Month.SEPTEMBER) to datum(income = 6041, expenses = -4233, transfers = -779, balance = 7041),
  YearAndMonth(2024, Month.OCTOBER) to datum(income = 6041, expenses = -3602, transfers = -3819, balance = 5662),
  YearAndMonth(2024, Month.NOVEMBER) to datum(income = 9200, expenses = -5191, transfers = -1111, balance = 8560),
  YearAndMonth(2024, Month.DECEMBER) to datum(income = 27, expenses = -4536, transfers = -4508, balance = 2389),
  YearAndMonth(2025, Month.JANUARY) to datum(income = 34551, expenses = -17336, transfers = -3477, balance = 15403),
  YearAndMonth(2025, Month.FEBRUARY) to datum(income = 9913, expenses = -12977, transfers = -834, balance = 11505),
  YearAndMonth(2025, Month.MARCH) to datum(income = 9850, expenses = -7413, transfers = -4146, balance = 9796),
  YearAndMonth(2025, Month.APRIL) to datum(income = 10218, expenses = -6161, transfers = -4990, balance = 8862),
  YearAndMonth(2025, Month.MAY) to datum(income = 9791, expenses = -5751, transfers = -2422, balance = 10480),
  YearAndMonth(2025, Month.JUNE) to datum(income = 143, expenses = -745, transfers = -4041, balance = 5836),
)
