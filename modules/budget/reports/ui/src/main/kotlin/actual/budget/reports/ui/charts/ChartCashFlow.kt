package actual.budget.reports.ui.charts

import actual.budget.model.Amount
import actual.budget.reports.ui.charts.PreviewShared.WIDTH
import actual.budget.reports.vm.CashFlowData
import actual.core.ui.CardShape
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewColumn
import actual.core.ui.PreviewScreen
import actual.core.ui.ScreenPreview
import actual.core.ui.Theme
import actual.core.ui.WrapWidthTable
import actual.core.ui.formattedString
import actual.core.ui.isInPreview
import actual.l10n.Strings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
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
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.runBlocking

@Composable
internal fun CashFlowChart(
  data: CashFlowData,
  compact: Boolean,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) = Column(
  modifier = modifier,
) {
  if (compact) {
    CompactHeader(data, theme = theme)
  } else {
    RegularHeader(data, theme = theme)
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
        valueFormatter = amountYAxisFormatter(),
        itemPlacer = remember { VerticalAxis.ItemPlacer.count(count = { 8 }) },
      ),
      bottomAxis = HorizontalAxis.rememberBottom(
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
    Footer(
      title = Strings.reportsCashFlowFooterTitle,
      text = Strings.reportsCashFlowFooter,
    )
  }
}

@Stable
private fun calculateNetFlow(data: CashFlowData): Amount {
  var total = Amount.Zero
  for ((_, value) in data.items) {
    total += (value.income + value.expenses)
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
) = Row(
  modifier = modifier
    .padding(start = 4.dp, end = 4.dp, top = 4.dp)
    .fillMaxWidth(),
) {
  Text(
    modifier = Modifier.weight(1f),
    text = dateRange(data.items.keys),
    overflow = TextOverflow.Ellipsis,
  )

  val padding = PaddingValues(horizontal = 2.dp)
  val normalStyle = MaterialTheme.typography.labelMedium.copy(textAlign = TextAlign.Start, color = theme.pageText)
  val boldStyle = normalStyle.copy(textAlign = TextAlign.End, fontWeight = FontWeight.W600)
  val summaryData = summaryData(data)

  Column {
    WrapWidthTable(
      modifier = Modifier.heightIn(max = 200.dp),
      ellipsize = false,
      data = persistentListOf(
        persistentListOf(Strings.reportsCashFlowIncome, summaryData.income.formattedString()),
        persistentListOf(Strings.reportsCashFlowExpenses, summaryData.expenses.formattedString()),
        persistentListOf(Strings.reportsCashFlowTransfers, summaryData.transfers.formattedString()),
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
) = Row(
  modifier = modifier.padding(4.dp),
  verticalAlignment = Alignment.CenterVertically,
) {
  Column(modifier = Modifier.weight(1f)) {
    Text(
      text = data.title,
      color = theme.pageText,
      overflow = TextOverflow.Ellipsis,
    )
    Text(
      text = dateRange(data.items.keys),
      color = theme.pageTextSubdued,
      overflow = TextOverflow.Ellipsis,
      style = MaterialTheme.typography.labelMedium,
    )
  }

  val netFlow = calculateNetFlow(data)
  Text(
    text = netFlow.formattedString(includeSign = true),
    color = if (netFlow.isPositive()) theme.successText else theme.errorText,
    overflow = TextOverflow.Ellipsis,
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

@ScreenPreview
@Composable
private fun PreviewRegular() = PreviewScreen(isPrivacyEnabled = false) {
  CashFlowChart(
    modifier = Modifier.padding(horizontal = 8.dp),
    data = PreviewCashFlow.DATA,
    compact = false,
  )
}

@ScreenPreview
@Composable
private fun PreviewRegularPrivate() = PreviewScreen(isPrivacyEnabled = true) {
  CashFlowChart(
    modifier = Modifier.padding(horizontal = 8.dp),
    data = PreviewCashFlow.DATA,
    compact = false,
  )
}

@Preview(widthDp = WIDTH)
@Composable
private fun PreviewCompact() = PreviewColumn(isPrivacyEnabled = false) {
  CashFlowChart(
    modifier = Modifier
      .background(LocalTheme.current.tableBackground, CardShape)
      .width(WIDTH.dp)
      .padding(5.dp),
    data = PreviewCashFlow.DATA,
    compact = true,
  )
}
