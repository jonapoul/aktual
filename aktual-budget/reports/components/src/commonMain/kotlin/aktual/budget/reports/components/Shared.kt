@file:Suppress("TooManyFunctions")

package aktual.budget.reports.components

import aktual.budget.model.Amount
import aktual.budget.model.CurrencyConfig
import aktual.budget.model.DateRangeType
import aktual.budget.model.NumberFormatConfig
import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.textSizes
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.LocalCurrencyConfig
import aktual.core.ui.LocalNumberFormatConfig
import aktual.core.ui.LocalPrivacyEnabled
import aktual.core.ui.redacted
import aktual.core.ui.redactedFontFamily
import aktual.core.ui.stringShort
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLineComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisTickComponent
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.decoration.Decoration
import com.patrykandpatrick.vico.compose.cartesian.layer.CartesianLayerPadding
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarkerVisibilityListener
import com.patrykandpatrick.vico.compose.cartesian.marker.ColumnCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.compose.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.LineCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.Insets
import com.patrykandpatrick.vico.compose.common.LayeredComponent
import com.patrykandpatrick.vico.compose.common.component.LineComponent
import com.patrykandpatrick.vico.compose.common.component.ShapeComponent
import com.patrykandpatrick.vico.compose.common.component.TextComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import kotlin.math.roundToLong
import kotlinx.collections.immutable.ImmutableCollection
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import kotlinx.datetime.number

internal fun date(year: Int, month: Month) = YearMonth(year, month)

@Composable
fun axisLineComponent(compact: Boolean) =
  if (compact) {
    null
  } else {
    rememberAxisLineComponent(strokeThickness = Hairline)
  }

@Composable
fun axisGuidelineComponent(compact: Boolean) =
  if (compact) {
    null
  } else {
    rememberAxisGuidelineComponent()
  }

@Composable
fun axisTickComponent(compact: Boolean) =
  if (compact) {
    rememberAxisTickComponent(fill = Fill(Transparent))
  } else {
    rememberAxisTickComponent()
  }

@Composable
fun axisLabelComponent(compact: Boolean) =
  if (compact) {
    null
  } else {
    rememberAxisLabelComponent(
      style = TextStyle(color = colors.pageText, fontSize = textSizes.small),
    )
  }

// Line charts pull the end labels inward so the lines run to the plot edges. Column charts keep
// the labels centred under their columns, which pad the plot anyway. Line charts need
// EdgeLayerPadding too
@Composable
internal fun hItemPlacer(lines: Boolean) =
  remember(lines) {
    HorizontalAxis.ItemPlacer.aligned(offset = { 0 }, spacing = { 1 }, shiftExtremeLabels = lines)
  }

// Compact charts skip animating for smoother list scrolling. 500ms tween mirrors vico's default
fun chartAnimationSpec(compact: Boolean): AnimationSpec<Float>? =
  if (compact) null else DiffAnimationSpec

private val DiffAnimationSpec: AnimationSpec<Float> = tween(durationMillis = 500)

// Vico throws on blank axis labels, whitespace included. Shown for x values with no point, e.g.
// while the model still holds the previous data
internal const val NO_AXIS_LABEL = "-"

@Composable
internal fun yearMonthXAxisFormatter(): CartesianValueFormatter {
  val monthStrings = monthStringsMap()
  return remember(monthStrings) {
    CartesianValueFormatter { _, value, _ ->
      val date = YearMonth.fromMonthNumber(value.roundToLong())
      val month = monthStrings[date.month] ?: "???"
      val year = date.year.toString().substring(startIndex = 2)
      "$month $year"
    }
  }
}

@Composable
fun amountYAxisFormatter(
  numberFormatConfig: NumberFormatConfig = LocalNumberFormatConfig.current,
  currencyConfig: CurrencyConfig = LocalCurrencyConfig.current,
  isPrivacyEnabled: Boolean = LocalPrivacyEnabled.current,
  hideFraction: Boolean = true,
): CartesianValueFormatter {
  val redactedFont = redactedFontFamily()
  return remember(
    numberFormatConfig,
    currencyConfig,
    isPrivacyEnabled,
    hideFraction,
    redactedFont,
  ) {
    CartesianValueFormatter { _, value, _ ->
      val text =
        Amount(value)
          .toString(
            numberFormatConfig = numberFormatConfig.copy(hideFraction = hideFraction),
            currencyConfig = currencyConfig,
            includeSign = false,
            isPrivacyEnabled = isPrivacyEnabled,
          )
      AnnotatedString(text).redacted(redactedFont)
    }
  }
}

@Composable
internal fun monthStringsMap(): ImmutableMap<Month, String> =
  Month.entries.associateWith { month -> month.stringShort() }.toImmutableMap()

/**
 * Adapted from
 * https://github.com/patrykandpatrick/vico/blob/master/sample/compose/src/main/kotlin/com/patrykandpatrick/vico/sample/compose/Marker.kt
 */
@Composable
fun rememberMarker(
  valueFormatter: DefaultCartesianMarker.ValueFormatter,
  markerShape: Shape = RoundedCornerShape(CornerSize(percent = 50)),
  lineCount: Int = 2,
  labelPosition: DefaultCartesianMarker.LabelPosition = Top,
): CartesianMarker {
  // Labels inside the chart need a background to stay readable over the lines and fills
  val labelBackground =
    if (labelPosition == AroundPoint) {
      rememberShapeComponent(
        fill = Fill(colors.tableBackground.copy(alpha = MARKER_BACKGROUND_ALPHA)),
        shape = RoundedCornerShape(8.dp),
      )
    } else {
      null
    }
  val label =
    rememberTextComponent(
      style = TextStyle(color = colors.pageText, textAlign = Center),
      lineCount = lineCount,
      padding = Insets(8.dp, 4.dp),
      background = labelBackground,
      minWidth = TextComponent.MinWidth.fixed(40.dp),
    )
  val indicatorFrontComponent =
    rememberShapeComponent(fill = Fill(color = colors.pageBackground), shape = markerShape)
  val guideline = rememberAxisGuidelineComponent()
  return rememberDefaultCartesianMarker(
    label = label,
    valueFormatter = valueFormatter,
    labelPosition = labelPosition,
    indicator = { color ->
      LayeredComponent(
        back = ShapeComponent(Fill(color.copy(alpha = 0.15f)), markerShape),
        front =
          LayeredComponent(
            back = ShapeComponent(fill = Fill(color), shape = markerShape),
            front = indicatorFrontComponent,
            padding = Insets(5.dp),
          ),
        padding = Insets(10.dp),
      )
    },
    indicatorSize = 36.dp,
    guideline = guideline,
  )
}

private const val MARKER_BACKGROUND_ALPHA = 0.9f

// The x value's label in bold, usually from the bottom axis's formatter, then each series' value
@Composable
fun rememberMarkerFormatter(
  xFormatter: CartesianValueFormatter,
  yFormatter: CartesianValueFormatter = amountYAxisFormatter(hideFraction = false),
): DefaultCartesianMarker.ValueFormatter =
  remember(xFormatter, yFormatter) {
    DefaultCartesianMarker.ValueFormatter { context, targets ->
      val x = targets.firstOrNull()?.x ?: return@ValueFormatter " "
      val values = targets.flatMap { it.coloredValues() }
      buildAnnotatedString {
        withStyle(SpanStyle(fontWeight = Bold)) { append(xFormatter.format(context, x, null)) }
        append('\n')
        values.forEachIndexed { index, (y, color) ->
          if (index > 0) append(", ")
          withStyle(SpanStyle(color = color)) { append(yFormatter.format(context, y, null)) }
        }
      }
    }
  }

private fun CartesianMarker.Target.coloredValues(): List<Pair<Double, Color>> =
  when (this) {
    is LineCartesianLayerMarkerTarget -> points.map { it.entry.y to it.color }
    is ColumnCartesianLayerMarkerTarget -> columns.map { it.entry.y to it.color }
    else -> emptyList()
  }

@Composable
fun rememberMarkerHaptics(compact: Boolean): CartesianMarkerVisibilityListener? {
  val haptics = LocalHapticFeedback.current
  return remember(haptics, compact) {
    if (compact) {
      null
    } else {
      object : CartesianMarkerVisibilityListener {
        override fun onShown(marker: CartesianMarker, targets: List<CartesianMarker.Target>) =
          haptics.performHapticFeedback(SegmentTick)

        override fun onUpdated(marker: CartesianMarker, targets: List<CartesianMarker.Target>) =
          haptics.performHapticFeedback(SegmentTick)
      }
    }
  }
}

// Upstream's qualitative colour scale, which wraps around once every colour is used
internal fun Colors.qualitative(index: Int): Color =
  listOf(
      chartQual1,
      chartQual2,
      chartQual3,
      chartQual4,
      chartQual5,
      chartQual6,
      chartQual7,
      chartQual8,
      chartQual9,
    )
    .let { it[index % it.size] }

private const val MONTHS_PER_YEAR = 12L

internal fun YearMonth.monthNumber(): Long = year * MONTHS_PER_YEAR + month.number

internal fun YearMonth.Companion.fromMonthNumber(number: Long): YearMonth {
  val adjustedNumber = number - 1
  return YearMonth(
    year = (adjustedNumber / MONTHS_PER_YEAR).toInt(),
    month = Month((adjustedNumber % MONTHS_PER_YEAR + 1).toInt()),
  )
}

@Stable
@Composable
internal fun dateRange(start: YearMonth, end: YearMonth) =
  "${start.stringShort()} - ${end.stringShort()}"

@Stable
@Composable
internal fun dateRange(months: ImmutableCollection<YearMonth>): String =
  dateRange(months.min(), months.max())

// Shared so the subtitle under each chart's title is styled the same across report types
@Composable
internal fun DateRangeText(
  text: String,
  modifier: Modifier = Modifier,
  color: Color = colors.pageTextSubdued,
) =
  Text(
    modifier = modifier,
    text = text,
    color = color,
    overflow = Ellipsis,
    maxLines = 1,
    style = typography.bodyMedium,
  )

// Swaps in the visible months while zoomed, mapping x values with [toMonth]
@Composable
internal fun DateRangeText(
  text: String,
  zoom: ChartZoomState,
  toMonth: (Double) -> YearMonth?,
  modifier: Modifier = Modifier,
  color: Color = colors.pageTextSubdued,
) {
  val zoomed =
    zoom.range?.let { range ->
      val start = toMonth(range.start) ?: return@let null
      val end = toMonth(range.endInclusive) ?: return@let null
      dateRange(start, end)
    }
  DateRangeText(
    modifier = modifier,
    text = zoomed ?: text,
    color = if (zoomed == null) color else colors.reportsBlue,
  )
}

@Composable
fun DateRangeType.string() =
  when (this) {
    ThisWeek -> Strings.reportsDateTypeThisWeek
    LastWeek -> Strings.reportsDateTypeLastWeek
    ThisMonth -> Strings.reportsDateTypeThisMonth
    LastMonth -> Strings.reportsDateTypeLastMonth
    CurrentQuarter -> Strings.reportsDateTypeCurrentQuarter
    PreviousQuarter -> Strings.reportsDateTypePreviousQuarter
    Last30Days -> Strings.reportsDateTypeLast30Days
    Last3Months -> Strings.reportsDateTypeLast3Months
    Last6Months -> Strings.reportsDateTypeLast6Months
    Last12Months -> Strings.reportsDateTypeLast12Months
    YearToDate -> Strings.reportsDateTypeYearToDate
    LastYear -> Strings.reportsDateTypeLastYear
    PriorYearToDate -> Strings.reportsDateTypePriorYearToDate
    AllTime -> Strings.reportsDateTypeAllTime
    Unknown -> Strings.reportsDateTypeUnknown
  }

// Vertical line at an x value, drawn over the chart's layers. Hidden when zoomed out of range
class VerticalLine(private val x: Double, private val line: LineComponent) : Decoration {
  override fun drawOverLayers(context: CartesianDrawingContext) =
    with(context) {
      if (x < ranges.minX || x > ranges.maxX) return@with
      val start =
        (if (isLtr) layerBounds.left else layerBounds.right) +
          layoutDirectionMultiplier * layerDimensions.startPadding - scroll
      val steps = ((x - ranges.minX) / ranges.xStep).toFloat()
      val canvasX = start + layoutDirectionMultiplier * layerDimensions.xSpacing * steps
      line.drawVertical(context, canvasX, layerBounds.top, layerBounds.bottom)
    }
}

// Vico skips axis labels on the exact edges of the plot, so line charts keep a sliver of padding
internal val EdgeLayerPadding = CartesianLayerPadding(unscalableStart = 1.dp, unscalableEnd = 1.dp)
