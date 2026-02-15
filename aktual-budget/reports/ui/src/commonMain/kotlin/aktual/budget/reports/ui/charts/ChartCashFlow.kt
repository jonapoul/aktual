package aktual.budget.reports.ui.charts

import aktual.budget.model.Amount
import aktual.budget.reports.vm.CashFlowData
import aktual.budget.reports.vm.CashFlowDatum
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTypography
import aktual.core.ui.CardShape
import aktual.core.ui.LocalTheme
import aktual.core.ui.PreviewWithColorScheme
import aktual.core.ui.Theme
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import aktual.core.ui.WrapWidthTable
import aktual.core.ui.formattedString
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
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.multiplatform.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.multiplatform.cartesian.data.columnSeries
import com.patrykandpatrick.vico.multiplatform.cartesian.data.lineSeries
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.multiplatform.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.multiplatform.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.multiplatform.common.Fill
import com.patrykandpatrick.vico.multiplatform.common.component.rememberLineComponent
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Month

@Composable
internal fun CashFlowChart(
  data: CashFlowData,
  compact: Boolean,
  modifier: Modifier = Modifier,
  includeHeader: Boolean = true,
  theme: Theme = LocalTheme.current,
) =
  Column(modifier = modifier) {
    if (includeHeader) {
      if (compact) {
        CompactHeader(data, theme = theme)
      } else {
        RegularHeader(data, theme = theme)
      }
    }

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
      modifier = if (compact) Modifier.fillMaxSize() else Modifier.weight(1f),
      modelProducer = modelProducer,
      scrollState = rememberVicoScrollState(scrollEnabled = false),
      chart =
        rememberCartesianChart(
          rememberColumnCartesianLayer(
            ColumnCartesianLayer.ColumnProvider.series(
              rememberLineComponent(fill = Fill(theme.reportsBlue), thickness = 16.dp)
            )
          ),
          rememberColumnCartesianLayer(
            ColumnCartesianLayer.ColumnProvider.series(
              rememberLineComponent(fill = Fill(theme.reportsRed), thickness = 16.dp)
            )
          ),
          rememberLineCartesianLayer(
            lineProvider =
              LineCartesianLayer.LineProvider.series(
                LineCartesianLayer.rememberLine(
                  fill = LineCartesianLayer.LineFill.single(Fill(theme.pageTextLight)),
                  stroke = LineCartesianLayer.LineStroke.Continuous(thickness = 3.dp),
                )
              )
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
              valueFormatter = yearMonthXAxisFormatter(),
              itemPlacer = hItemPlacer(compact),
            ),
          marker = if (compact) null else rememberMarker(),
        ),
    )

    if (!compact) {
      Footer(title = Strings.reportsCashFlowFooterTitle, text = Strings.reportsCashFlowFooter)
    }
  }

@Stable
private fun calculateNetFlow(data: CashFlowData): Amount {
  var total = Amount.Zero
  for ((_, value) in data.items) {
    total += value.income + value.expenses
  }
  return total
}

@Immutable
private data class SummaryData(
  val income: Amount,
  val expenses: Amount,
  val transfers: Amount,
  val net: Amount,
)

@Stable
private fun summaryData(data: CashFlowData): SummaryData {
  var income = Amount.Zero
  var expenses = Amount.Zero
  var transfers = Amount.Zero

  for ((_, item) in data.items) {
    income += item.income
    expenses += item.expenses
    transfers += item.transfers
  }

  return SummaryData(income, expenses, transfers, net = income + expenses + transfers)
}

@Composable
private fun RegularHeader(
  data: CashFlowData,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) =
  Row(modifier = modifier.padding(start = 4.dp, end = 4.dp, top = 4.dp).fillMaxWidth()) {
    Text(
      modifier = Modifier.weight(1f),
      text = dateRange(data.items.keys),
      overflow = TextOverflow.Ellipsis,
    )

    val padding = PaddingValues(horizontal = 2.dp)
    val normalStyle =
      AktualTypography.labelMedium.copy(textAlign = TextAlign.Start, color = theme.pageText)
    val boldStyle = normalStyle.copy(textAlign = TextAlign.End, fontWeight = FontWeight.W600)
    val summaryData = summaryData(data)

    Column {
      WrapWidthTable(
        modifier = Modifier.heightIn(max = 200.dp),
        ellipsize = false,
        data =
          persistentListOf(
            persistentListOf(Strings.reportsCashFlowIncome, summaryData.income.formattedString()),
            persistentListOf(
              Strings.reportsCashFlowExpenses,
              summaryData.expenses.formattedString(),
            ),
            persistentListOf(
              Strings.reportsCashFlowTransfers,
              summaryData.transfers.formattedString(),
            ),
          ),
        textStyles = persistentListOf(normalStyle, boldStyle),
        paddings = persistentListOf(padding, padding),
      )

      Text(
        modifier = Modifier.padding(padding),
        text = summaryData.net.formattedString(includeSign = true),
        textAlign = TextAlign.End,
        style = boldStyle,
        color = if (summaryData.net.isPositive()) theme.successText else theme.errorText,
      )
    }
  }

@Composable
private fun CompactHeader(
  data: CashFlowData,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) =
  Row(modifier = modifier.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
    Column(modifier = Modifier.weight(1f)) {
      Text(text = data.title, color = theme.pageText, overflow = TextOverflow.Ellipsis)
      Text(
        text = dateRange(data.items.keys),
        color = theme.pageTextSubdued,
        overflow = TextOverflow.Ellipsis,
        style = AktualTypography.labelMedium,
      )
    }

    val netFlow = calculateNetFlow(data)
    Text(
      text = netFlow.formattedString(includeSign = true),
      color = if (netFlow.isPositive()) theme.successText else theme.errorText,
      overflow = TextOverflow.Ellipsis,
    )
  }

private suspend fun CartesianChartModelProducer.populate(data: CashFlowData) =
  with(data.items) {
    runTransaction {
      columnSeries {
        series(x = keys.map { it.monthNumber() }, y = values.map { it.income.toDouble() })
      }

      columnSeries {
        series(x = keys.map { it.monthNumber() }, y = values.map { it.expenses.toDouble() })
      }

      lineSeries {
        series(x = keys.map { it.monthNumber() }, y = values.map { it.balance.toDouble() })
      }
    }
  }

@Preview
@Composable
private fun PreviewCashFlowChart(
  @PreviewParameter(CashFlowChartProvider::class) params: ThemedParams<CashFlowChartParams>
) =
  PreviewWithColorScheme(
    schemeType = params.type,
    isPrivacyEnabled = params.data.isPrivacyEnabled,
  ) {
    CashFlowChart(
      modifier =
        Modifier.background(LocalTheme.current.tableBackground, CardShape)
          .let { m -> if (params.data.compact) m.height(300.dp) else m }
          .width(WIDTH.dp)
          .padding(5.dp),
      data = params.data.data,
      compact = params.data.compact,
    )
  }

private data class CashFlowChartParams(
  val data: CashFlowData,
  val compact: Boolean,
  val isPrivacyEnabled: Boolean = false,
)

private class CashFlowChartProvider :
  ThemedParameterProvider<CashFlowChartParams>(
    CashFlowChartParams(PREVIEW_CASH_FLOW_DATA, compact = false),
    CashFlowChartParams(PREVIEW_CASH_FLOW_DATA, compact = false, isPrivacyEnabled = true),
    CashFlowChartParams(PREVIEW_CASH_FLOW_DATA, compact = true),
  )

internal val PREVIEW_CASH_FLOW_DATA =
  CashFlowData(
    title = "My Cash Flow",
    items =
      persistentMapOf(
        date(2024, Month.JULY) to
          datum(income = 6683, expenses = -4695, transfers = -1779, balance = 4781),
        date(2024, Month.AUGUST) to
          datum(income = 6071, expenses = -4111, transfers = -729, balance = 6012),
        date(2024, Month.SEPTEMBER) to
          datum(income = 6041, expenses = -4233, transfers = -779, balance = 7041),
        date(2024, Month.OCTOBER) to
          datum(income = 6041, expenses = -3602, transfers = -3819, balance = 5662),
        date(2024, Month.NOVEMBER) to
          datum(income = 9200, expenses = -5191, transfers = -1111, balance = 8560),
        date(2024, Month.DECEMBER) to
          datum(income = 27, expenses = -4536, transfers = -4508, balance = 2389),
        date(2025, Month.JANUARY) to
          datum(income = 34551, expenses = -17336, transfers = -3477, balance = 15403),
        date(2025, Month.FEBRUARY) to
          datum(income = 9913, expenses = -12977, transfers = -834, balance = 11505),
        date(2025, Month.MARCH) to
          datum(income = 9850, expenses = -7413, transfers = -4146, balance = 9796),
        date(2025, Month.APRIL) to
          datum(income = 10218, expenses = -6161, transfers = -4990, balance = 8862),
        date(2025, Month.MAY) to
          datum(income = 9791, expenses = -5751, transfers = -2422, balance = 10480),
        date(2025, Month.JUNE) to
          datum(income = 143, expenses = -745, transfers = -4041, balance = 5836),
      ),
  )

private fun datum(income: Int, expenses: Int, transfers: Int, balance: Int) =
  CashFlowDatum(
    income = Amount(income.toDouble()),
    expenses = Amount(expenses.toDouble()),
    transfers = Amount(transfers.toDouble()),
    balance = Amount(balance.toDouble()),
  )
