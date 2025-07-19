package actual.budget.reports.ui.charts

import actual.budget.model.Amount
import actual.budget.model.NumberFormatConfig
import actual.core.ui.LocalNumberFormatConfig
import actual.core.ui.LocalPrivacyEnabled
import actual.core.ui.LocalTheme
import actual.core.ui.Theme
import actual.l10n.R
import android.content.Context
import android.text.Layout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLineComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisTickComponent
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.common.component.fixed
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.insets
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.common.LayeredComponent
import com.patrykandpatrick.vico.core.common.component.ShapeComponent
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import com.patrykandpatrick.vico.core.common.shape.Shape
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import kotlinx.datetime.number
import kotlin.math.roundToLong

@Composable
internal fun axisLineComponent(compact: Boolean) = if (compact) {
  null
} else {
  rememberAxisLineComponent()
}

@Composable
internal fun axisGuidelineComponent(compact: Boolean) = if (compact) {
  null
} else {
  rememberAxisGuidelineComponent()
}

@Composable
internal fun axisTickComponent(compact: Boolean) = if (compact) {
  rememberAxisTickComponent(fill = fill(Color.Transparent))
} else {
  rememberAxisTickComponent()
}

@Composable
internal fun axisLabelComponent(compact: Boolean, theme: Theme = LocalTheme.current) = if (compact) {
  null
} else {
  rememberAxisLabelComponent(color = theme.pageText)
}

@Composable
internal fun hItemPlacer(compact: Boolean) = if (compact) {
  remember { HorizontalAxis.ItemPlacer.aligned(offset = { 0 }, spacing = { 1 }) }
} else {
  remember { HorizontalAxis.ItemPlacer.aligned(offset = { 0 }, spacing = { 1 }) }
}

@Composable
internal fun yearMonthXAxisFormatter(
  androidContext: Context = LocalContext.current,
) = remember(androidContext) {
  CartesianValueFormatter { _, value, _ ->
    val date = YearMonth.fromMonthNumber(value.roundToLong())
    val month = androidContext.getString(date.month.resId())
    val year = date.year.toString().substring(startIndex = 2)
    "$month $year"
  }
}

@Composable
internal fun amountYAxisFormatter(
  config: NumberFormatConfig = LocalNumberFormatConfig.current,
  isPrivacyEnabled: Boolean = LocalPrivacyEnabled.current,
) = remember(config, isPrivacyEnabled) {
  CartesianValueFormatter { _, value, _ ->
    Amount(value).toString(
      config = config.copy(hideFraction = true),
      includeSign = false,
      isPrivacyEnabled = isPrivacyEnabled,
    )
  }
}

internal fun Month.resId() = when (this) {
  Month.JANUARY -> R.string.month_jan_short
  Month.FEBRUARY -> R.string.month_feb_short
  Month.MARCH -> R.string.month_mar_short
  Month.APRIL -> R.string.month_apr_short
  Month.MAY -> R.string.month_may_short
  Month.JUNE -> R.string.month_jun_short
  Month.JULY -> R.string.month_jul_short
  Month.AUGUST -> R.string.month_aug_short
  Month.SEPTEMBER -> R.string.month_sep_short
  Month.OCTOBER -> R.string.month_oct_short
  Month.NOVEMBER -> R.string.month_nov_short
  Month.DECEMBER -> R.string.month_dec_short
}

/**
 * Adapted from https://github.com/patrykandpatrick/vico/blob/master/sample/compose/src/main/kotlin/com/patrykandpatrick/vico/sample/compose/Marker.kt
 */
@Composable
internal fun rememberMarker(
  markerShape: Shape = CorneredShape.Pill,
  theme: Theme = LocalTheme.current,
): CartesianMarker {
  val label = rememberTextComponent(
    color = theme.pageText,
    textAlignment = Layout.Alignment.ALIGN_CENTER,
    padding = insets(8.dp, 4.dp),
    minWidth = TextComponent.MinWidth.fixed(40.dp),
  )
  val indicatorFrontComponent = rememberShapeComponent(
    fill = fill(color = theme.pageBackground),
    shape = markerShape,
  )
  val guideline = rememberAxisGuidelineComponent()
  return rememberDefaultCartesianMarker(
    label = label,
    valueFormatter = DefaultCartesianMarker.ValueFormatter.default(),
    indicator = { color ->
      LayeredComponent(
        back = ShapeComponent(fill(color.copy(alpha = 0.15f)), markerShape),
        front = LayeredComponent(
          back = ShapeComponent(fill = fill(color), shape = markerShape),
          front = indicatorFrontComponent,
          padding = insets(5.dp),
        ),
        padding = insets(10.dp),
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
