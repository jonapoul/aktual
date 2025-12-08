package aktual.budget.reports.ui.charts

import aktual.budget.model.Amount
import aktual.budget.reports.ui.charts.WIDTH
import aktual.budget.reports.vm.NetWorthData
import aktual.core.ui.AktualTypography
import aktual.core.ui.CardShape
import aktual.core.ui.LocalTheme
import aktual.core.ui.PreviewWithColorScheme
import aktual.core.ui.Theme
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import aktual.core.ui.formattedString
import aktual.core.ui.isInPreview
import aktual.l10n.Strings
import alakazam.kotlin.compose.VerticalSpacer
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.multiplatform.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.multiplatform.cartesian.data.lineSeries
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.multiplatform.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.multiplatform.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.multiplatform.common.Fill
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Month

@Composable
internal fun NetWorthChart(
  data: NetWorthData,
  compact: Boolean,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
  includeHeader: Boolean = true,
) = Column(modifier = modifier) {
  if (includeHeader) {
    Header(
      modifier = Modifier.fillMaxWidth(),
      data = data,
    )
  }

  Chart(
    modifier = if (compact) Modifier.fillMaxSize() else Modifier.weight(1f),
    data = data,
    compact = compact,
    theme = theme,
  )

  if (!compact) {
    Footer(
      title = Strings.reportsNetWorthFooterTitle,
      text = Strings.reportsNetWorthFooter,
    )
  }
}

@Composable
private fun Header(
  data: NetWorthData,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) = Row(
  modifier = modifier
    .padding(start = 4.dp, end = 4.dp, top = 4.dp)
    .fillMaxWidth(),
  verticalAlignment = Alignment.CenterVertically,
) {
  Column(
    modifier = Modifier.weight(1f),
  ) {
    Text(
      text = data.title,
      overflow = TextOverflow.Ellipsis,
      color = theme.pageText,
      style = AktualTypography.bodyLarge,
    )

    Text(
      text = dateRange(data.items.keys),
      overflow = TextOverflow.Ellipsis,
      color = theme.pageTextSubdued,
      style = AktualTypography.bodyMedium,
    )
  }

  Column(
    horizontalAlignment = Alignment.End,
  ) {
    val netWorthLatest = remember(data) { data.items.maxBy { (month, _) -> month }.value }
    Text(
      text = netWorthLatest.formattedString(includeSign = false),
      textAlign = TextAlign.End,
      style = AktualTypography.bodyLarge,
      color = theme.pageText,
    )

    VerticalSpacer(4.dp)

    val netWorthChange = remember(data, netWorthLatest) {
      val netWorthFirst = data.items.minBy { (month, _) -> month }.value
      netWorthLatest - netWorthFirst
    }
    Text(
      text = netWorthChange.formattedString(includeSign = true),
      textAlign = TextAlign.End,
      style = AktualTypography.bodySmall,
      color = if (netWorthChange.isPositive()) theme.successText else theme.errorText,
    )
  }
}

@Composable
private fun Chart(
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
            fill = LineCartesianLayer.LineFill.single(Fill(theme.reportsBlue)),
            areaFill = LineCartesianLayer.AreaFill.double(
              topFill = Fill(theme.reportsBlue.copy(alpha = 0.2f)),
              bottomFill = Fill(theme.reportsRed.copy(alpha = 0.2f)),
            ),
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

@Preview
@Composable
private fun PreviewNetWorthChart(
  @PreviewParameter(NetWorthChartProvider::class) params: ThemedParams<NetWorthChartParams>,
) = PreviewWithColorScheme(schemeType = params.type, isPrivacyEnabled = params.data.private) {
  NetWorthChart(
    modifier = Modifier
      .background(LocalTheme.current.tableBackground, CardShape)
      .width(WIDTH.dp)
      .let { m -> if (params.data.compact) m.height(300.dp) else m }
      .padding(5.dp),
    data = params.data.data,
    compact = params.data.compact,
  )
}

private data class NetWorthChartParams(
  val data: NetWorthData,
  val compact: Boolean,
  val private: Boolean,
)

private class NetWorthChartProvider : ThemedParameterProvider<NetWorthChartParams>(
  NetWorthChartParams(PREVIEW_NET_WORTH_DATA, compact = true, private = false),
  NetWorthChartParams(PREVIEW_NET_WORTH_DATA, compact = true, private = true),
  NetWorthChartParams(PREVIEW_NET_WORTH_DATA, compact = false, private = false),
  NetWorthChartParams(PREVIEW_NET_WORTH_DATA, compact = false, private = true),
)

internal val PREVIEW_NET_WORTH_DATA = NetWorthData(
  title = "My Net Worth",
  items = persistentMapOf(
    date(2023, Month.JANUARY) to Amount(-44_000.00),
    date(2024, Month.JANUARY) to Amount(-18_000.00),
    date(2024, Month.FEBRUARY) to Amount(21_000.34),
    date(2024, Month.MARCH) to Amount(25_000.67),
    date(2024, Month.JULY) to Amount(-10123.98),
    date(2024, Month.AUGUST) to Amount(-5123.69),
    date(2024, Month.DECEMBER) to Amount(-6789.12),
    date(2025, Month.JANUARY) to Amount(198.0),
    date(2025, Month.FEBRUARY) to Amount(1.98),
    date(2025, Month.MARCH) to Amount(256.0),
    date(2025, Month.JUNE) to Amount(30000.0),
  ),
)
