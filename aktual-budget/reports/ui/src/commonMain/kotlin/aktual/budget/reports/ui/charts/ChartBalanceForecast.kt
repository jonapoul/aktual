package aktual.budget.reports.ui.charts

import aktual.budget.model.Amount
import aktual.budget.reports.vm.BalanceForecastData
import aktual.core.l10n.Plurals
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.CardShape
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.formattedString
import aktual.core.ui.isInPreview
import alakazam.compose.VerticalSpacer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.multiplatform.cartesian.data.lineSeries
import com.patrykandpatrick.vico.multiplatform.cartesian.decoration.Decoration
import com.patrykandpatrick.vico.multiplatform.cartesian.decoration.HorizontalLine
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.multiplatform.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.multiplatform.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.multiplatform.common.DashedShape
import com.patrykandpatrick.vico.multiplatform.common.Fill
import com.patrykandpatrick.vico.multiplatform.common.component.rememberLineComponent
import kotlin.math.roundToInt
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.DateTimeUnit.Companion.DAY
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month.DECEMBER
import kotlinx.datetime.Month.JANUARY
import kotlinx.datetime.YearMonth
import kotlinx.datetime.plus
import kotlinx.datetime.yearMonth

@Composable
internal fun BalanceForecastChart(
  data: BalanceForecastData,
  compact: Boolean,
  modifier: Modifier = Modifier,
  includeHeader: Boolean = true,
  zoom: ChartZoomState = rememberChartZoomState(data),
) =
  Column(modifier = modifier) {
    if (includeHeader) {
      Header(modifier = Modifier.fillMaxWidth(), data = data)
    }

    val chartModifier = if (compact) Modifier.fillMaxSize() else Modifier.weight(1f)
    if (data.items.isEmpty()) {
      Empty(modifier = chartModifier)
    } else {
      Chart(modifier = chartModifier, data = data, compact = compact, zoom = zoom)
    }

    if (!compact) {
      if (data.items.isNotEmpty()) {
        Text(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
          text = data.summary(),
          style = typography.bodySmall,
          color = colors.pageTextSubdued,
        )
      }

      if (data.source == TrackingBudget) {
        Footer(
          title = Strings.reportsBalanceForecastHowTitle,
          text = Strings.reportsBalanceForecastTrackingHow,
        )
      } else {
        Footer(
          title = Strings.reportsBalanceForecastHowTitle,
          text = Strings.reportsBalanceForecastHow,
        )
      }
    }
  }

@Composable
private fun Header(
  data: BalanceForecastData,
  modifier: Modifier = Modifier,
) =
  Row(
    modifier = modifier.padding(start = 4.dp, end = 4.dp, top = 4.dp).fillMaxWidth(),
    verticalAlignment = CenterVertically,
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = data.title ?: Strings.reportsChooseTypeBalanceForecast,
        overflow = Ellipsis,
        color = colors.pageText,
        style = typography.bodyLarge,
      )

      DateRangeText(dateRange(data.start, data.end))
    }

    val ending = data.items.values.lastOrNull() ?: return@Row
    Column(horizontalAlignment = Alignment.End) {
      Text(
        text = ending.formattedString(),
        textAlign = End,
        style = typography.bodyLarge,
        color = if (ending.isPositive()) colors.pageText else colors.errorText,
      )

      VerticalSpacer(4.dp)

      Text(
        text = Strings.reportsBalanceForecastEnding,
        textAlign = End,
        style = typography.bodySmall,
        color = colors.pageTextSubdued,
      )

      // Like upstream, the low point only shows when it isn't the ending balance
      val lowest = remember(data) { data.items.entries.minBy { it.value } }
      if (lowest.key != data.items.keys.last()) {
        VerticalSpacer(2.dp)
        Text(
          text = Strings.reportsBalanceForecastLow(lowest.value.formattedString()),
          textAlign = End,
          style = typography.bodySmall,
          color = if (lowest.value.isPositive()) colors.pageTextSubdued else colors.errorText,
        )
      }
    }
  }

@Composable
private fun Empty(modifier: Modifier = Modifier) =
  Box(modifier = modifier.padding(16.dp), contentAlignment = Alignment.Center) {
    Text(
      text = Strings.reportsBalanceForecastEmpty,
      textAlign = Center,
      style = typography.bodyMedium,
      color = colors.pageTextSubdued,
    )
  }

@Composable
private fun BalanceForecastData.summary(): String =
  when {
    source == TrackingBudget -> Strings.reportsBalanceForecastTracking
    scheduledCount == 0 -> Strings.reportsBalanceForecastNoScheduled
    else -> Plurals.reportsBalanceForecastScheduled(scheduledCount, scheduledCount)
  }

@Composable
private fun Chart(
  data: BalanceForecastData,
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
            lineProvider =
              LineCartesianLayer.LineProvider.series(
                LineCartesianLayer.rememberLine(
                  // Upstream turns the line red below zero
                  fill =
                    LineCartesianLayer.LineFill.double(
                      topFill = Fill(colors.reportsChartFill),
                      bottomFill = Fill(colors.reportsNumberNegative),
                    ),
                  areaFill =
                    LineCartesianLayer.AreaFill.double(
                      topFill = Fill(colors.reportsChartFill.copy(alpha = 0.2f)),
                      bottomFill = Fill(colors.reportsNumberNegative.copy(alpha = 0.2f)),
                    ),
                )
              ),
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
              valueFormatter = forecastXAxisFormatter(data),
              itemPlacer = forecastItemPlacer(data),
            ),
          marker = if (compact) null else rememberMarker(),
          markerVisibilityListener = rememberMarkerHaptics(compact),
          decorations =
            listOfNotNull(
              rememberTodayLine(data),
              rememberZeroLine(data),
              rememberChartZoomDecoration(zoom),
            ),
        ),
    )
  }
}

// Daily ranges have too many points to label them all
@Composable
private fun forecastItemPlacer(data: BalanceForecastData): HorizontalAxis.ItemPlacer {
  val spacing =
    when (data.granularity) {
      Daily -> (data.items.size / DAILY_LABELS).coerceAtLeast(1)
      Monthly,
      Unknown -> 1
    }
  return remember(spacing) {
    HorizontalAxis.ItemPlacer.aligned(offset = { 0 }, spacing = { spacing })
  }
}

// x values are indexes into the points, since days don't map onto month numbers
@Composable
private fun forecastXAxisFormatter(data: BalanceForecastData): CartesianValueFormatter {
  val monthStrings = monthStringsMap()
  return remember(data, monthStrings) {
    val dates = data.items.keys.toList()
    CartesianValueFormatter { _, value, _ ->
      val date = dates.getOrNull(value.roundToInt()) ?: return@CartesianValueFormatter ""
      val month = monthStrings[date.month] ?: "???"
      when (data.granularity) {
        Daily -> "${date.day} $month"
        Monthly,
        Unknown -> "$month ${date.year.toString().substring(startIndex = 2)}"
      }
    }
  }
}

@Composable
private fun rememberTodayLine(data: BalanceForecastData): Decoration? {
  val line =
    rememberLineComponent(
      fill = Fill(colors.reportsBlue),
      thickness = 1.dp,
      shape = remember { DashedShape(dashLength = 4.dp, gapLength = 4.dp) },
    )
  val index =
    remember(data) {
      val today =
        when (data.granularity) {
          Daily -> data.today
          Monthly,
          Unknown -> data.today.yearMonth.firstDay
        }
      data.items.keys.indexOf(today)
    }
  if (index < 0) return null
  return remember(line, index) { VerticalLine(x = index.toDouble(), line = line) }
}

@Composable
private fun rememberZeroLine(data: BalanceForecastData): Decoration? {
  val line = rememberLineComponent(fill = Fill(colors.pageTextSubdued), thickness = 1.dp)
  val hasNegative = remember(data) { data.items.values.any { !it.isPositive() } }
  if (!hasNegative) return null
  return remember(line) { HorizontalLine(y = { 0.0 }, line = line) }
}

private suspend fun CartesianChartModelProducer.populate(data: BalanceForecastData, zoom: XRange?) =
  runTransaction {
    zoomTo(zoom)
    if (data.items.isEmpty()) return@runTransaction
    lineSeries {
      series(
        x = data.items.keys.indices.toList(),
        y = data.items.values.map { it.toDouble() },
        zoom = zoom,
      )
    }
  }

private const val DAILY_LABELS = 6

@Preview
@Composable
private fun PreviewBalanceForecastChart(
  @PreviewParameter(BalanceForecastChartProvider::class)
  params: ColoredParams<BalanceForecastChartParams>
) =
  PreviewWithColoredParams(params) {
    BalanceForecastChart(
      modifier =
        Modifier.background(colors.tableBackground, CardShape)
          .width(WIDTH.dp)
          .let { m -> if (compact) m.height(300.dp) else m }
          .padding(5.dp),
      data = data,
      compact = compact,
    )
  }

private data class BalanceForecastChartParams(val data: BalanceForecastData, val compact: Boolean)

private class BalanceForecastChartProvider :
  ColoredParameterProvider<BalanceForecastChartParams>(
    BalanceForecastChartParams(PREVIEW_BALANCE_FORECAST_DATA, compact = true),
    BalanceForecastChartParams(PREVIEW_BALANCE_FORECAST_DATA, compact = false),
    BalanceForecastChartParams(PREVIEW_DAILY_BALANCE_FORECAST_DATA, compact = false),
    BalanceForecastChartParams(
      PREVIEW_BALANCE_FORECAST_DATA.copy(items = persistentMapOf()),
      compact = true,
    ),
  )

internal val PREVIEW_BALANCE_FORECAST_DATA =
  BalanceForecastData(
    title = "Balance Forecast",
    start = YearMonth(2026, JANUARY),
    end = YearMonth(2026, DECEMBER),
    granularity = Monthly,
    source = Schedules,
    items =
      listOf(
          2400.0,
          1850.0,
          1200.0,
          450.0,
          -300.0,
          -650.0,
          150.0,
          900.0,
          1600.0,
          2350.0,
          3100.0,
          3900.0,
        )
        .mapIndexed { i, balance -> LocalDate(2026, i + 1, 1) to Amount(balance) }
        .toMap()
        .toImmutableMap(),
    today = LocalDate(2026, 3, 14),
    scheduledCount = 36,
  )

private val PREVIEW_DAILY_BALANCE_FORECAST_DATA =
  PREVIEW_BALANCE_FORECAST_DATA.copy(
    end = YearMonth(2026, JANUARY),
    granularity = Daily,
    items =
      (0 until 31)
        .associate { i ->
          val paid = if (i >= 24) 3200.0 else 0.0
          val rent = if (i >= 1) -1500.0 else 0.0
          LocalDate(2026, 1, 1).plus(i, DAY) to Amount(1200.0 + rent + paid - i * 25.0)
        }
        .toImmutableMap(),
    today = LocalDate(2026, 1, 10),
    scheduledCount = 3,
  )
