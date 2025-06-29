package actual.budget.reports.ui.charts

import actual.core.ui.LocalTheme
import actual.core.ui.Theme
import actual.l10n.R
import android.text.Layout
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.common.component.fixed
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.insets
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.common.LayeredComponent
import com.patrykandpatrick.vico.core.common.component.ShapeComponent
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import com.patrykandpatrick.vico.core.common.shape.Shape
import kotlinx.datetime.Month

internal fun Month.resId() = when (this) {
  Month.JANUARY -> R.string.reports_month_jan
  Month.FEBRUARY -> R.string.reports_month_feb
  Month.MARCH -> R.string.reports_month_mar
  Month.APRIL -> R.string.reports_month_apr
  Month.MAY -> R.string.reports_month_may
  Month.JUNE -> R.string.reports_month_jun
  Month.JULY -> R.string.reports_month_jul
  Month.AUGUST -> R.string.reports_month_aug
  Month.SEPTEMBER -> R.string.reports_month_sep
  Month.OCTOBER -> R.string.reports_month_oct
  Month.NOVEMBER -> R.string.reports_month_nov
  Month.DECEMBER -> R.string.reports_month_dec
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
