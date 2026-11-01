package aktual.budget.reports.components

import aktual.budget.reports.vm.NetWorthData
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTheme.colors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.compose.cartesian.axis.Axis
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer.LineFill
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer.LineProvider
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer.LineStroke
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarkerVisibilityListener
import com.patrykandpatrick.vico.compose.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.compose.common.Fill
import kotlin.math.roundToLong
import kotlinx.datetime.YearMonth

// packages/desktop-client/src/components/reports/graphs/NetWorthGraph.tsx: recharts' default
// stacking, so each account's line is the running total of it and every account before it, and the
// last line is net worth
internal fun netWorthStacks(data: NetWorthData): List<List<Double>> {
  val months = data.items.keys.toList()
  val totals = DoubleArray(months.size)
  return data.accounts.map { account ->
    months.mapIndexed { index, month ->
      totals[index] += account.balances[month]?.toDouble() ?: 0.0
      totals[index]
    }
  }
}

internal fun NetWorthData.isStacked(): Boolean = mode == Stacked && accounts.isNotEmpty()

@Composable
internal fun rememberStackedLineProvider(
  data: NetWorthData,
  compact: Boolean,
  highlighted: Int?,
): LineProvider {
  val colors = colors
  return remember(data.accounts.size, colors, compact, highlighted) {
    // Shared by every band, holding the area under the line drawn before it
    val previous = Path()
    LineProvider.series(
      data.accounts.indices.map { index ->
        val alpha = if (highlighted == null || highlighted == index) 1f else DIMMED_ALPHA
        val color = colors.qualitative(index).copy(alpha = alpha)
        LineCartesianLayer.Line(
          fill = LineFill.single(Fill(color)),
          stroke =
            LineStroke.Continuous(
              thickness = if (compact) STACKED_COMPACT_LINE_THICKNESS else STACKED_LINE_THICKNESS,
            ),
          areaFill =
            StackedBandFill(
              color = color.copy(alpha = STACKED_FILL_ALPHA * alpha),
              previous = previous,
              first = index == 0,
            ),
        )
      },
    )
  }
}

@Composable
internal fun rememberStackedMarkerFormatter(
  data: NetWorthData,
): DefaultCartesianMarker.ValueFormatter {
  val colors = colors
  val format = rememberAmountFormatter()
  val total = Strings.reportsCustomTotal
  val monthStrings = monthStringsMap()
  return remember(data, colors, format, total, monthStrings) {
    DefaultCartesianMarker.ValueFormatter { _, targets ->
      val x = targets.firstOrNull()?.x ?: return@ValueFormatter " "
      val month = YearMonth.fromMonthNumber(x.roundToLong())
      val label = "${monthStrings[month.month]} ${month.year}"
      // Top band first, like the chart reads
      val stack =
        data.accounts
          .mapIndexed { index, account ->
            CustomBar(
              name = account.name,
              amount = account.balances[month] ?: Zero,
              color = colors.qualitative(index),
            )
          }
          .asReversed()
      stackedMarkerText(label, stack, total, format)
    }
  }
}

// Shows the tapped month in the breakdown list, back to the latest month once the marker hides
@Composable
internal fun rememberMonthSelection(
  compact: Boolean,
  onSelect: (YearMonth?) -> Unit,
): CartesianMarkerVisibilityListener? {
  val haptics = rememberMarkerHaptics(compact) ?: return null
  val currentOnSelect by rememberUpdatedState(onSelect)
  return remember(haptics) {
    object : CartesianMarkerVisibilityListener {
      override fun onShown(marker: CartesianMarker, targets: List<CartesianMarker.Target>) {
        haptics.onShown(marker, targets)
        currentOnSelect(targets.month())
      }

      override fun onUpdated(marker: CartesianMarker, targets: List<CartesianMarker.Target>) {
        haptics.onUpdated(marker, targets)
        currentOnSelect(targets.month())
      }

      override fun onHidden(marker: CartesianMarker) {
        haptics.onHidden(marker)
        currentOnSelect(null)
      }

      private fun List<CartesianMarker.Target>.month() =
        firstOrNull()?.x?.let { YearMonth.fromMonthNumber(it.roundToLong()) }
    }
  }
}

// Fills between this line and the one drawn before it, or zero for the first. Each line's area down
// to the bottom of the chart is added to one path with the even-odd rule, so only the band between
// the two lines is filled, even where they cross each other or zero. This avoids Path.op, which can
// invert self-intersecting paths (see Vico's BaseAreaFill). Relies on Vico drawing the series in
// order
private class StackedBandFill(
  private val color: Color,
  private val previous: Path,
  private val first: Boolean,
) : LineCartesianLayer.AreaFill {
  private val paint = Paint()
  private val area = Path()
  private val band = Path()

  override fun draw(
    context: CartesianDrawingContext,
    linePath: Path,
    halfLineThickness: Float,
    verticalAxisPosition: Axis.Position.Vertical?,
  ) =
    with(context) {
      val bounds = linePath.getBounds()
      val bottom = layerBounds.bottom
      area.rewind()
      area.addPath(linePath)
      area.lineTo(if (isLtr) bounds.right else bounds.left, bottom)
      area.lineTo(if (isLtr) bounds.left else bounds.right, bottom)
      area.close()

      band.rewind()
      band.fillType = EvenOdd
      band.addPath(area)
      if (first) {
        val yRange = ranges.getYRange(verticalAxisPosition)
        val zeroY =
          (bottom - ((0.0 - yRange.minY) / yRange.length).toFloat() * layerBounds.height).coerceIn(
            layerBounds.top,
            bottom,
          )
        band.addRect(Rect(bounds.left, zeroY, bounds.right, bottom))
      } else {
        band.addPath(previous)
      }

      paint.color = color
      canvas.drawPath(band, paint)

      previous.rewind()
      previous.addPath(area)
    }
}

private const val STACKED_FILL_ALPHA = 0.5f
private val STACKED_LINE_THICKNESS = 1.5.dp
private val STACKED_COMPACT_LINE_THICKNESS = 1.dp
