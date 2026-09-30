package aktual.budget.reports.ui.charts

import aktual.budget.reports.vm.AgeOfMoneyData
import aktual.budget.reports.vm.AgeOfMoneyTrend
import aktual.core.l10n.Plurals
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.CardShape
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.isInPreview
import alakazam.compose.VerticalSpacer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.multiplatform.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.multiplatform.cartesian.data.lineSeries
import com.patrykandpatrick.vico.multiplatform.cartesian.decoration.HorizontalLine
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.multiplatform.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.multiplatform.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.multiplatform.common.DashedShape
import com.patrykandpatrick.vico.multiplatform.common.Fill
import com.patrykandpatrick.vico.multiplatform.common.Position
import com.patrykandpatrick.vico.multiplatform.common.component.rememberLineComponent
import com.patrykandpatrick.vico.multiplatform.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.multiplatform.common.component.rememberTextComponent
import com.patrykandpatrick.vico.multiplatform.common.data.ExtraStore
import kotlin.math.roundToInt
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month.JANUARY
import kotlinx.datetime.Month.JUNE
import kotlinx.datetime.YearMonth

private const val TARGET_AGE_DAYS = 30
private const val OK_AGE_DAYS = 14

@Composable
internal fun AgeOfMoneyChart(
  data: AgeOfMoneyData,
  compact: Boolean,
  modifier: Modifier = Modifier,
  includeHeader: Boolean = true,
) {
  Column(modifier = modifier) {
    if (includeHeader) {
      Header(modifier = Modifier.fillMaxWidth(), data = data)
    }

    Chart(
      modifier = if (compact) Modifier.fillMaxSize() else Modifier.weight(1f),
      data = data,
      compact = compact,
    )

    if (!compact) {
      Footer(title = Strings.reportsAgeOfMoneyWhatTitle, text = Strings.reportsAgeOfMoneyWhat)
      Footer(title = Strings.reportsAgeOfMoneyHowTitle, text = Strings.reportsAgeOfMoneyHow)
    }
  }
}

@Composable
private fun Header(
  data: AgeOfMoneyData,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.padding(start = 4.dp, end = 4.dp, top = 4.dp).fillMaxWidth(),
    verticalAlignment = CenterVertically,
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = data.title ?: Strings.reportsChooseTypeAgeOfMoney,
        overflow = Ellipsis,
        color = colors.pageText,
        style = typography.bodyLarge,
      )

      DateRangeText(dateRange(data.start, data.end))
    }

    Column(horizontalAlignment = Alignment.End) {
      val age = data.currentAge
      Text(
        text =
          if (age == null) {
            Strings.reportsAgeOfMoneyNone
          } else {
            Plurals.reportsAgeOfMoneyDays(age, age)
          },
        textAlign = End,
        style = typography.bodyLarge,
        color = ageColor(age),
      )

      if (age != null) {
        VerticalSpacer(4.dp)
        Text(
          text = data.trend.string(),
          textAlign = End,
          style = typography.bodySmall,
          color = colors.pageTextSubdued,
        )
      }

      if (data.insufficientData) {
        VerticalSpacer(2.dp)
        Text(
          text = Strings.reportsAgeOfMoneyIncomplete,
          textAlign = End,
          style = typography.bodySmall,
          color = colors.warningText,
        )
      }
    }
  }
}

@Composable
@ReadOnlyComposable
private fun ageColor(age: Int?): Color =
  when {
    age == null -> colors.reportsNumberNeutral
    age >= TARGET_AGE_DAYS -> colors.reportsNumberPositive
    age >= OK_AGE_DAYS -> colors.warningText
    else -> colors.reportsNumberNegative
  }

@Composable
private fun AgeOfMoneyTrend.string(): String =
  when (this) {
    Up -> Strings.reportsAgeOfMoneyImproving
    Down -> Strings.reportsAgeOfMoneyDeclining
    Stable -> Strings.reportsAgeOfMoneyStable
  }

@Composable
private fun Chart(
  data: AgeOfMoneyData,
  compact: Boolean,
  modifier: Modifier = Modifier,
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
  val dayFormatter = dayYAxisFormatter()

  CartesianChartHost(
    modifier = modifier,
    modelProducer = modelProducer,
    scrollState = rememberVicoScrollState(scrollEnabled = false),
    animationSpec = chartAnimationSpec(compact),
    chart =
      rememberCartesianChart(
        rememberLineCartesianLayer(
          rangeProvider = remember { IncludeTargetAge },
          lineProvider =
            LineCartesianLayer.LineProvider.series(
              LineCartesianLayer.rememberLine(
                fill = LineCartesianLayer.LineFill.single(Fill(colors.reportsChartFill)),
                areaFill =
                  LineCartesianLayer.AreaFill.single(
                    Fill(
                      Brush.verticalGradient(
                        listOf(
                          colors.reportsChartFill.copy(alpha = 0.3f),
                          colors.reportsChartFill.copy(alpha = 0.05f),
                        )
                      )
                    )
                  ),
                pointProvider = if (compact) null else rememberPointProvider(),
                pointConnector = LineCartesianLayer.PointConnector.cubic(),
              )
            ),
        ),
        startAxis =
          VerticalAxis.rememberStart(
            line = line,
            guideline = guideline,
            label = label,
            tick = tick,
            valueFormatter = dayFormatter,
          ),
        bottomAxis =
          HorizontalAxis.rememberBottom(
            line = line,
            guideline = guideline,
            label = label,
            tick = tick,
            valueFormatter = periodXAxisFormatter(data),
            itemPlacer = hItemPlacer(compact),
          ),
        marker = if (compact) null else rememberMarker(),
        markerVisibilityListener = rememberMarkerHaptics(compact),
        decorations = if (compact) emptyList() else listOf(rememberTargetLine()),
      ),
  )
}

// Keep the target line in view even when every age is below it
private object IncludeTargetAge : CartesianLayerRangeProvider {
  override fun getMaxY(minY: Double, maxY: Double, extraStore: ExtraStore): Double =
    maxOf(maxY, TARGET_AGE_DAYS.toDouble())
}

@Composable
private fun rememberPointProvider(): LineCartesianLayer.PointProvider {
  val component = rememberShapeComponent(fill = Fill(colors.reportsChartFill), shape = CircleShape)
  return remember(component) {
    LineCartesianLayer.PointProvider.single(LineCartesianLayer.Point(component, size = 6.dp))
  }
}

@Composable
private fun rememberTargetLine(): HorizontalLine {
  val line =
    rememberLineComponent(
      fill = Fill(colors.reportsGreen),
      thickness = 1.dp,
      shape = remember { DashedShape(dashLength = 5.dp, gapLength = 5.dp) },
    )
  val label =
    rememberTextComponent(style = TextStyle(color = colors.reportsGreen, fontSize = 10.sp))
  val text = Plurals.reportsAgeOfMoneyDays(TARGET_AGE_DAYS, TARGET_AGE_DAYS)
  return remember(line, label, text) {
    HorizontalLine(
      y = { TARGET_AGE_DAYS.toDouble() },
      line = line,
      labelComponent = label,
      label = { text },
      horizontalLabelPosition = Position.Horizontal.End,
      verticalLabelPosition = Position.Vertical.Bottom,
    )
  }
}

@Composable
private fun dayYAxisFormatter(): CartesianValueFormatter {
  val suffix = Strings.reportsAgeOfMoneyAxisSuffix
  return remember(suffix) {
    CartesianValueFormatter { _, value, _ -> "${value.roundToInt()}$suffix" }
  }
}

// x values are indexes into the periods, since daily and weekly periods don't map onto months
@Composable
private fun periodXAxisFormatter(data: AgeOfMoneyData): CartesianValueFormatter {
  val monthStrings = monthStringsMap()
  return remember(data, monthStrings) {
    val periods = data.items.keys.toList()
    CartesianValueFormatter { _, value, _ ->
      val date = periods.getOrNull(value.roundToInt()) ?: return@CartesianValueFormatter ""
      val month = monthStrings[date.month] ?: "???"
      when (data.granularity) {
        Daily,
        Weekly -> "${date.day} $month"
        Monthly,
        Unknown -> "$month ${date.year.toString().substring(startIndex = 2)}"
      }
    }
  }
}

private suspend fun CartesianChartModelProducer.populate(data: AgeOfMoneyData) = runTransaction {
  if (data.items.isEmpty()) return@runTransaction
  lineSeries { series(x = data.items.keys.indices.toList(), y = data.items.values.toList()) }
}

@Preview
@Composable
private fun PreviewAgeOfMoneyChart(
  @PreviewParameter(AgeOfMoneyChartProvider::class) params: ColoredParams<AgeOfMoneyChartParams>
) =
  PreviewWithColors(params.colors) {
    AgeOfMoneyChart(
      modifier =
        Modifier.background(colors.tableBackground, CardShape)
          .width(WIDTH.dp)
          .let { m -> if (params.data.compact) m.height(300.dp) else m }
          .padding(5.dp),
      data = params.data.data,
      compact = params.data.compact,
    )
  }

private data class AgeOfMoneyChartParams(val data: AgeOfMoneyData, val compact: Boolean)

private class AgeOfMoneyChartProvider :
  ColoredParameterProvider<AgeOfMoneyChartParams>(
    AgeOfMoneyChartParams(PREVIEW_AGE_OF_MONEY_DATA, compact = true),
    AgeOfMoneyChartParams(PREVIEW_AGE_OF_MONEY_DATA, compact = false),
    AgeOfMoneyChartParams(
      PREVIEW_AGE_OF_MONEY_DATA.copy(currentAge = null, insufficientData = true),
      compact = true,
    ),
  )

internal val PREVIEW_AGE_OF_MONEY_DATA =
  AgeOfMoneyData(
    title = "Age of Money",
    start = YearMonth(2026, JANUARY),
    end = YearMonth(2026, JUNE),
    granularity = Monthly,
    items =
      persistentMapOf(
        LocalDate(2026, 1, 1) to 12,
        LocalDate(2026, 2, 1) to 17,
        LocalDate(2026, 3, 1) to 21,
        LocalDate(2026, 4, 1) to 19,
        LocalDate(2026, 5, 1) to 26,
        LocalDate(2026, 6, 1) to 33,
      ),
    currentAge = 33,
    trend = Up,
    insufficientData = false,
  )
