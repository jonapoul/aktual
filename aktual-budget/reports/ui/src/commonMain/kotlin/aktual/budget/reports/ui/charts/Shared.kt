package aktual.budget.reports.ui.charts

import aktual.budget.model.Amount
import aktual.budget.model.DateRangeType
import aktual.budget.model.NumberFormatConfig
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTypography
import aktual.core.ui.LocalNumberFormatConfig
import aktual.core.ui.LocalPrivacyEnabled
import aktual.core.ui.LocalTheme
import aktual.core.ui.Theme
import aktual.core.ui.stringShort
import alakazam.compose.VerticalSpacer
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.rememberAxisLineComponent
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.rememberAxisTickComponent
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.multiplatform.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.multiplatform.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.multiplatform.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.multiplatform.common.Fill
import com.patrykandpatrick.vico.multiplatform.common.Insets
import com.patrykandpatrick.vico.multiplatform.common.LayeredComponent
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
    rememberAxisLineComponent(strokeThickness = Dp.Hairline)
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
    rememberAxisTickComponent(fill = Fill(Color.Transparent))
  } else {
    rememberAxisTickComponent()
  }

@Composable
internal fun axisLabelComponent(compact: Boolean, theme: Theme = LocalTheme.current) =
  if (compact) {
    null
  } else {
    rememberAxisLabelComponent(style = TextStyle(color = theme.pageText, fontSize = 12.sp))
  }

@Composable
internal fun hItemPlacer(compact: Boolean) =
  if (compact) {
    remember { HorizontalAxis.ItemPlacer.aligned(offset = { 0 }, spacing = { 1 }) }
  } else {
    remember { HorizontalAxis.ItemPlacer.aligned(offset = { 0 }, spacing = { 1 }) }
  }

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
  config: NumberFormatConfig = LocalNumberFormatConfig.current,
  isPrivacyEnabled: Boolean = LocalPrivacyEnabled.current,
) =
  remember(config, isPrivacyEnabled) {
    CartesianValueFormatter { _, value, _ ->
      Amount(value)
        .toString(
          config = config.copy(hideFraction = true),
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
  markerShape: Shape = RoundedCornerShape(CornerSize(percent = 50)),
  theme: Theme = LocalTheme.current,
): CartesianMarker {
  val label =
    rememberTextComponent(
      style = TextStyle(color = theme.pageText, textAlign = TextAlign.Center),
      padding = Insets(8.dp, 4.dp),
      minWidth = TextComponent.MinWidth.fixed(40.dp),
    )
  val indicatorFrontComponent =
    rememberShapeComponent(fill = Fill(color = theme.pageBackground), shape = markerShape)
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

private const val MONTHS_PER_YEAR = 12L

internal fun YearMonth.monthNumber(): Long = (year * MONTHS_PER_YEAR) + month.number

internal fun YearMonth.Companion.fromMonthNumber(number: Long): YearMonth {
  val adjustedNumber = number - 1 // Convert to 0-based indexing
  return YearMonth(
    year = (adjustedNumber / MONTHS_PER_YEAR).toInt(),
    month = Month(((adjustedNumber % MONTHS_PER_YEAR) + 1).toInt()),
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

@Composable
internal fun Footer(title: String, text: String, modifier: Modifier = Modifier) =
  Column(modifier = modifier.fillMaxWidth().padding(8.dp)) {
    Text(text = title, fontWeight = FontWeight.Bold, style = AktualTypography.bodyMedium)

    VerticalSpacer(4.dp)

    Text(text = text, style = AktualTypography.bodySmall)
  }

@Composable
internal fun DateRangeType.string() =
  when (this) {
    DateRangeType.ThisWeek -> Strings.reportsDateTypeThisWeek
    DateRangeType.LastWeek -> Strings.reportsDateTypeLastWeek
    DateRangeType.ThisMonth -> Strings.reportsDateTypeThisMonth
    DateRangeType.LastMonth -> Strings.reportsDateTypeLastMonth
    DateRangeType.Last3Months -> Strings.reportsDateTypeLast3Months
    DateRangeType.Last6Months -> Strings.reportsDateTypeLast6Months
    DateRangeType.Last12Months -> Strings.reportsDateTypeLast12Months
    DateRangeType.YearToDate -> Strings.reportsDateTypeYearToDate
    DateRangeType.LastYear -> Strings.reportsDateTypeLastYear
    DateRangeType.AllTime -> Strings.reportsDateTypeAllTime
  }
