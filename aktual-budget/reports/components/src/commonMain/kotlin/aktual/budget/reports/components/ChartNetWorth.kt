package aktual.budget.reports.components

import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.Condition
import aktual.budget.model.ConditionType.Boolean
import aktual.budget.model.sumOf
import aktual.budget.reports.vm.NetWorthAccount
import aktual.budget.reports.vm.NetWorthData
import aktual.budget.reports.vm.NetWorthReportMeta
import aktual.budget.reports.vm.TimeFrame
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.CardShape
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.formattedText
import aktual.core.ui.isInPreview
import alakazam.compose.VerticalSpacer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.lineModel
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.Fill
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Month.DECEMBER
import kotlinx.datetime.Month.JANUARY
import kotlinx.datetime.Month.MARCH
import kotlinx.datetime.YearMonth
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive

@Composable
internal fun NetWorthChart(
  data: NetWorthData,
  compact: Boolean,
  modifier: Modifier = Modifier,
  includeHeader: Boolean = true,
  zoom: ChartZoomState = rememberChartZoomState(data),
) =
  Column(modifier = modifier) {
    if (includeHeader) {
      Header(modifier = Modifier.fillMaxWidth(), data = data)
    }

    var selected by remember(data) { mutableStateOf<YearMonth?>(null) }
    var highlighted by remember(data) { mutableStateOf<AccountId?>(null) }

    Chart(
      modifier = if (compact) Modifier.fillMaxSize() else Modifier.weight(1f),
      data = data,
      compact = compact,
      zoom = zoom,
      highlighted = highlighted,
      onSelectMonth = { selected = it },
    )

    val month = selected ?: data.items.keys.maxOrNull()
    if (!compact && month != null) {
      NetWorthBreakdownList(
        modifier = Modifier.fillMaxWidth().heightIn(max = BREAKDOWN_MAX_HEIGHT).padding(top = 8.dp),
        data = data,
        month = month,
        highlighted = highlighted,
        onHighlight = { highlighted = it },
      )
    }
  }

@Composable
private fun Header(
  data: NetWorthData,
  modifier: Modifier = Modifier,
) =
  Row(
    modifier = modifier.padding(start = 4.dp, end = 4.dp, top = 4.dp).fillMaxWidth(),
    verticalAlignment = CenterVertically,
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = data.title ?: Strings.reportsChooseTypeNetWorth,
        overflow = Ellipsis,
        color = colors.pageText,
        style = typography.bodyLarge,
      )

      DateRangeText(dateRange(data.items.keys))
    }

    Column(horizontalAlignment = Alignment.End) {
      val netWorthLatest = remember(data) { data.items.maxBy { (month, _) -> month }.value }
      Text(
        text = netWorthLatest.formattedText(includeSign = false),
        textAlign = End,
        style = typography.bodyLarge,
        color = colors.pageText,
      )

      VerticalSpacer(4.dp)

      val netWorthChange =
        remember(data, netWorthLatest) {
          val netWorthFirst = data.items.minBy { (month, _) -> month }.value
          netWorthLatest - netWorthFirst
        }
      Text(
        text = netWorthChange.formattedText(includeSign = true),
        textAlign = End,
        style = typography.bodySmall,
        color = if (netWorthChange.isPositive()) colors.noticeText else colors.errorText,
      )
    }
  }

@Composable
private fun Chart(
  data: NetWorthData,
  compact: Boolean,
  zoom: ChartZoomState,
  highlighted: AccountId?,
  onSelectMonth: (YearMonth?) -> Unit,
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
  val stacked = data.isStacked()
  val trendLines =
    LineCartesianLayer.LineProvider.series(
      LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(Fill(colors.reportsChartFill)),
        areaFill =
          LineCartesianLayer.AreaFill.double(
            topFill = Fill(colors.reportsNumberPositive.copy(alpha = 0.2f)),
            bottomFill = Fill(colors.reportsRed.copy(alpha = 0.2f)),
          ),
      ),
    )
  val highlightedIndex = data.accounts.indexOfFirst { it.id == highlighted }.takeIf { it >= 0 }
  val stackedLines = rememberStackedLineProvider(data, compact, highlightedIndex)
  val stackedMarker = rememberStackedMarkerFormatter(data)

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
            lineProvider = if (stacked) stackedLines else trendLines,
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
          marker =
            when {
              compact -> null
              stacked ->
                rememberMarker(
                  valueFormatter = stackedMarker,
                  lineCount = markerLines(data.accounts.size),
                  // A label this tall would squash the chart if Vico made room for it on top
                  labelPosition = AroundPoint,
                )
              else -> rememberMarker()
            },
          markerVisibilityListener = rememberMonthSelection(compact, onSelectMonth),
          decorations = listOf(rememberChartZoomDecoration(zoom)),
        ),
    )
  }
}

private suspend fun CartesianChartModelProducer.populate(data: NetWorthData, zoom: XRange?) {
  val x = data.items.keys.map { it.monthNumber() }
  val stacks = if (data.isStacked()) netWorthStacks(data) else null
  runTransaction {
    zoomTo(zoom)
    lineModel {
      if (stacks == null) {
        series(x = x, y = data.items.values.map { it.toDouble() }, zoom = zoom)
      } else {
        stacks.forEach { y -> series(x = x, y = y, zoom = zoom) }
      }
    }
  }
}

private val BREAKDOWN_MAX_HEIGHT = 240.dp

@Preview
@Composable
private fun PreviewNetWorthChart(
  @PreviewParameter(NetWorthChartProvider::class) params: ColorParams<NetWorthChartParams>,
) =
  PreviewWithColorParams(params, isPrivacyEnabled = params.data.private) {
    NetWorthChart(
      modifier =
        Modifier.background(colors.tableBackground, CardShape)
          .width(WIDTH.dp)
          .let { m -> if (compact) m.height(300.dp) else m }
          .padding(5.dp),
      data = data,
      compact = compact,
    )
  }

private data class NetWorthChartParams(
  val data: NetWorthData,
  val compact: Boolean,
  val private: Boolean,
)

private class NetWorthChartProvider :
  ColorParamsProvider<NetWorthChartParams>(
    NetWorthChartParams(PREVIEW_NET_WORTH_DATA, compact = true, private = false),
    NetWorthChartParams(PREVIEW_NET_WORTH_DATA, compact = true, private = true),
    NetWorthChartParams(PREVIEW_NET_WORTH_DATA, compact = false, private = false),
    NetWorthChartParams(PREVIEW_NET_WORTH_DATA, compact = false, private = true),
    NetWorthChartParams(PREVIEW_NET_WORTH_STACKED, compact = true, private = false),
    NetWorthChartParams(PREVIEW_NET_WORTH_STACKED, compact = false, private = false),
    NetWorthChartParams(PREVIEW_NET_WORTH_STACKED, compact = false, private = true),
  )

val PREVIEW_NET_WORTH_META =
  NetWorthReportMeta(
    name = "My Net Worth",
    conditions =
      listOf(
        Condition(
          field = Transfer,
          operator = Is,
          value =
            JsonArray(
              listOf(
                JsonPrimitive("13a3d249-1bda-4e72-8ab1-eddae21fc795"),
                JsonPrimitive("7d7c7459-59bf-4158-ae33-116c2e841020"),
              ),
            ),
          type = Boolean,
        ),
      ),
    conditionsOp = null,
    timeFrame =
      TimeFrame(
        start = YearMonth(2026, JANUARY),
        end = YearMonth(2026, MARCH),
        mode = SlidingWindow,
      ),
    interval = Weekly,
    mode = Stacked,
  )

val PREVIEW_NET_WORTH_DATA =
  NetWorthData(
    title = "My Net Worth",
    items =
      persistentMapOf(
        date(2023, JANUARY) to Amount(-44_000.00),
        date(2024, JANUARY) to Amount(-18_000.00),
        date(2024, FEBRUARY) to Amount(21_000.34),
        date(2024, MARCH) to Amount(25_000.67),
        date(2024, JULY) to Amount(-10123.98),
        date(2024, AUGUST) to Amount(-5123.69),
        date(2024, DECEMBER) to Amount(-6789.12),
        date(2025, JANUARY) to Amount(198.0),
        date(2025, FEBRUARY) to Amount(1.98),
        date(2025, MARCH) to Amount(256.0),
        date(2025, JUNE) to Amount(30000.0),
      ),
  )

private val PREVIEW_MONTHS = (YearMonth(2025, JANUARY)..YearMonth(2025, DECEMBER)).toList()

private fun previewAccount(id: String, name: String, start: Double, step: Double) =
  NetWorthAccount(
    id = AccountId(id),
    name = name,
    balances =
      PREVIEW_MONTHS.mapIndexed { i, month -> month to Amount(start + step * i) }
        .toMap()
        .toImmutableMap(),
  )

// Stacking order, smallest total first
private val PREVIEW_STACKED_ACCOUNTS =
  persistentListOf(
    previewAccount("loan", "Car loan", start = -9_000.0, step = 400.0),
    previewAccount("card", "Credit card", start = -2_500.0, step = 150.0),
    previewAccount("checking", "Checking", start = 3_000.0, step = 120.0),
    previewAccount("savings", "Savings", start = 8_000.0, step = 500.0),
    previewAccount("isa", "Stocks ISA", start = 15_000.0, step = 900.0),
  )

val PREVIEW_NET_WORTH_STACKED =
  NetWorthData(
    title = PREVIEW_NET_WORTH_DATA.title,
    items =
      PREVIEW_MONTHS.associateWith { month ->
          PREVIEW_STACKED_ACCOUNTS.sumOf { it.balances.getValue(month) }
        }
        .toImmutableMap(),
    mode = Stacked,
    accounts = PREVIEW_STACKED_ACCOUNTS,
  )
