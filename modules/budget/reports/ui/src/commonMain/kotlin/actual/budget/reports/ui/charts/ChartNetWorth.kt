package actual.budget.reports.ui.charts

import actual.budget.reports.ui.charts.PreviewShared.WIDTH
import actual.budget.reports.vm.NetWorthData
import actual.core.ui.ActualTypography
import actual.core.ui.CardShape
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewColumn
import actual.core.ui.PreviewScreen
import actual.core.ui.ScreenPreview
import actual.core.ui.Theme
import actual.core.ui.formattedString
import actual.core.ui.isInPreview
import actual.l10n.Strings
import alakazam.kotlin.compose.VerticalSpacer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
      style = ActualTypography.bodyLarge,
    )

    Text(
      text = dateRange(data.items.keys),
      overflow = TextOverflow.Ellipsis,
      color = theme.pageTextSubdued,
      style = ActualTypography.bodyMedium,
    )
  }

  Column(
    horizontalAlignment = Alignment.End,
  ) {
    val netWorthLatest = remember(data) { data.items.maxBy { (month, _) -> month }.value }
    Text(
      text = netWorthLatest.formattedString(includeSign = false),
      textAlign = TextAlign.End,
      style = ActualTypography.bodyLarge,
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
      style = ActualTypography.bodySmall,
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

@ScreenPreview
@Composable
private fun MixedRegular() = PreviewScreen {
  NetWorthChart(
    data = PreviewNetWorth.DATA,
    compact = false,
  )
}

@ScreenPreview
@Composable
private fun MixedRegularPrivate() = PreviewScreen(isPrivacyEnabled = true) {
  NetWorthChart(
    data = PreviewNetWorth.DATA,
    compact = false,
  )
}

@Preview(widthDp = WIDTH)
@Composable
private fun PreviewCompact() = PreviewColumn(isPrivacyEnabled = false) {
  NetWorthChart(
    modifier = Modifier
      .background(LocalTheme.current.tableBackground, CardShape)
      .width(WIDTH.dp),
    data = PreviewNetWorth.DATA,
    compact = true,
  )
}

@Preview(widthDp = WIDTH)
@Composable
private fun PreviewCompactPrivate() = PreviewColumn(isPrivacyEnabled = true) {
  NetWorthChart(
    modifier = Modifier
      .background(LocalTheme.current.tableBackground, CardShape)
      .width(WIDTH.dp),
    data = PreviewNetWorth.DATA,
    compact = true,
  )
}
