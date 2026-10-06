package aktual.budget.reports.ui.charts

import aktual.budget.model.Amount
import aktual.budget.model.CurrencyConfig
import aktual.budget.model.DateRangeType
import aktual.budget.model.NumberFormatConfig
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.LocalCurrencyConfig
import aktual.core.ui.LocalNumberFormatConfig
import aktual.core.ui.LocalPrivacyEnabled
import aktual.core.ui.stringShort
import alakazam.compose.VerticalSpacer
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.multiplatform.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.rememberAxisLineComponent
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.rememberAxisTickComponent
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.multiplatform.cartesian.decoration.Decoration
import com.patrykandpatrick.vico.multiplatform.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.multiplatform.cartesian.marker.CartesianMarkerVisibilityListener
import com.patrykandpatrick.vico.multiplatform.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.multiplatform.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.multiplatform.common.Fill
import com.patrykandpatrick.vico.multiplatform.common.Insets
import com.patrykandpatrick.vico.multiplatform.common.LayeredComponent
import com.patrykandpatrick.vico.multiplatform.common.component.LineComponent
import com.patrykandpatrick.vico.multiplatform.common.component.ShapeComponent
import com.patrykandpatrick.vico.multiplatform.common.component.TextComponent
import com.patrykandpatrick.vico.multiplatform.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.multiplatform.common.component.rememberTextComponent
import kotlin.math.roundToLong
import kotlinx.collections.immutable.ImmutableCollection
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import kotlinx.datetime.number

internal fun date(year: Int, month: Month) = YearMonth(year, month)

@Composable
internal fun axisLineComponent(compact: Boolean) =
  if (compact) {
    null
  } else {
    rememberAxisLineComponent(strokeThickness = Hairline)
  }

@Composable
internal fun axisGuidelineComponent(compact: Boolean) =
  if (compact) {
    null
  } else {
    rememberAxisGuidelineComponent()
  }

@Composable
internal fun axisTickComponent(compact: Boolean) =
  if (compact) {
    rememberAxisTickComponent(fill = Fill(Transparent))
  } else {
    rememberAxisTickComponent()
  }

@Composable
internal fun axisLabelComponent(compact: Boolean) =
  if (compact) {
    null
  } else {
    rememberAxisLabelComponent(style = TextStyle(color = colors.pageText, fontSize = 12.sp))
  }

@Composable
internal fun hItemPlacer(compact: Boolean) =
  if (compact) {
    remember { HorizontalAxis.ItemPlacer.aligned(offset = { 0 }, spacing = { 1 }) }
  } else {
    remember { HorizontalAxis.ItemPlacer.aligned(offset = { 0 }, spacing = { 1 }) }
  }

// Compact charts skip animating for smoother list scrolling. 500ms tween mirrors vico's default
internal fun chartAnimationSpec(compact: Boolean): AnimationSpec<Float>? =
  if (compact) null else DiffAnimationSpec

private val DiffAnimationSpec: AnimationSpec<Float> = tween(durationMillis = 500)

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
internal fun amountYAxisFormatter(
  numberFormatConfig: NumberFormatConfig = LocalNumberFormatConfig.current,
  currencyConfig: CurrencyConfig = LocalCurrencyConfig.current,
  isPrivacyEnabled: Boolean = LocalPrivacyEnabled.current,
) =
  remember(numberFormatConfig, currencyConfig, isPrivacyEnabled) {
    CartesianValueFormatter { _, value, _ ->
      Amount(value)
        .toString(
          numberFormatConfig = numberFormatConfig.copy(hideFraction = true),
          currencyConfig = currencyConfig,
          includeSign = false,
          isPrivacyEnabled = isPrivacyEnabled,
        )
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
internal fun rememberMarker(
  markerShape: Shape = RoundedCornerShape(CornerSize(percent = 50))
): CartesianMarker {
  val label =
    rememberTextComponent(
      style = TextStyle(color = colors.pageText, textAlign = Center),
      padding = Insets(8.dp, 4.dp),
      minWidth = TextComponent.MinWidth.fixed(40.dp),
    )
  val indicatorFrontComponent =
    rememberShapeComponent(fill = Fill(color = colors.pageBackground), shape = markerShape)
  val guideline = rememberAxisGuidelineComponent()
  return rememberDefaultCartesianMarker(
    label = label,
    valueFormatter = DefaultCartesianMarker.ValueFormatter.default(),
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

@Composable
internal fun rememberMarkerHaptics(compact: Boolean): CartesianMarkerVisibilityListener? {
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

private const val MONTHS_PER_YEAR = 12L

internal fun YearMonth.monthNumber(): Long = year * MONTHS_PER_YEAR + month.number

internal fun YearMonth.Companion.fromMonthNumber(number: Long): YearMonth {
  val adjustedNumber = number - 1 // Convert to 0-based indexing
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

@Composable
internal fun Footer(title: String, text: String, modifier: Modifier = Modifier) =
  Column(modifier = modifier.fillMaxWidth().padding(8.dp)) {
    Text(text = title, fontWeight = Bold, color = colors.pageText, style = typography.bodyMedium)

    VerticalSpacer(4.dp)

    Text(text = text, color = colors.pageText, style = typography.bodySmall)
  }

@Composable
internal fun DateRangeType.string() =
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
internal class VerticalLine(private val x: Double, private val line: LineComponent) : Decoration {
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
