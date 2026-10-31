package aktual.budget.reports.components

import aktual.budget.reports.vm.CustomDrilldown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalViewConfiguration
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarkerController
import com.patrykandpatrick.vico.compose.cartesian.marker.ColumnCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.compose.cartesian.marker.Interaction
import com.patrykandpatrick.vico.compose.cartesian.marker.LineCartesianLayerMarkerTarget
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/** Opens the transactions behind part of a custom report */
internal typealias OnDrilldown = (CustomDrilldown) -> Unit

/** Where a tap landed on a chart, and the marker targets under it */
internal typealias OnChartTap = (y: Float, targets: List<CartesianMarker.Target>) -> Unit

// Shows the marker while pressed, as Vico does by default, and passes a quick tap to [onTap]. A
// press held long enough to read the marker isn't a tap
@Composable
internal fun rememberDrilldownController(onTap: OnChartTap?): CartesianMarkerController {
  if (onTap == null) return CartesianMarkerController.rememberShowOnPress()
  val currentOnTap by rememberUpdatedState(onTap)
  val timeout = LocalViewConfiguration.current.longPressTimeoutMillis.milliseconds
  return remember(timeout) {
    DrilldownController(timeout) { y, targets -> currentOnTap(y, targets) }
  }
}

private class DrilldownController(private val timeout: Duration, private val onTap: OnChartTap) :
  CartesianMarkerController {
  private var isPressed = false
  private var pressedAt: TimeMark? = null

  override val acceptsLongPress = false

  override val lock = CartesianMarkerController.Lock.Position

  override fun shouldAcceptInteraction(
    interaction: Interaction,
    targets: List<CartesianMarker.Target>,
  ): Boolean =
    when (interaction) {
      is Press -> {
        isPressed = true
        pressedAt = TimeSource.Monotonic.markNow()
        true
      }

      is Move -> {
        isPressed
      }

      is Release -> {
        isPressed = false
        true
      }

      is Tap -> {
        val isQuick = pressedAt?.let { it.elapsedNow() < timeout } != false
        pressedAt = null
        if (isQuick && targets.isNotEmpty()) onTap(interaction.point.y, targets)
        false
      }

      is LongPress,
      is Zoom,
      is Enter,
      is Exit -> {
        false
      }
    }

  override fun shouldShowMarker(
    interaction: Interaction,
    targets: List<CartesianMarker.Target>,
  ): Boolean = interaction !is Release
}

/**
 * The series of a stack of columns that holds [y]. Vico lists them in series order, each with the
 * edge furthest from the axis
 */
internal fun ColumnCartesianLayerMarkerTarget.seriesAt(y: Float): Int? {
  if (columns.isEmpty()) return null
  // Above the axis the edge is a column's top, and canvas y grows downwards
  val above = columns.withIndex().filter { it.value.entry.y > 0 }
  val below = columns.withIndex().filter { it.value.entry.y < 0 }
  return above.firstOrNull { it.value.canvasY <= y }?.index
    ?: below.firstOrNull { it.value.canvasY >= y }?.index
    ?: columns.indices.minBy { abs(columns[it].canvasY - y) }
}

/** The series of the point nearest [y] */
internal fun LineCartesianLayerMarkerTarget.seriesAt(y: Float): Int? =
  points.indices.minByOrNull { abs(points[it].canvasY - y) }

/**
 * The slice of a donut under [tap], given each slice's value. Vico draws them clockwise from 12
 * o'clock
 */
internal fun sliceAt(tap: Offset, center: Offset, values: List<Double>): Int? {
  val total = values.sum()
  if (total <= 0.0) return null
  val degrees = atan2(tap.y - center.y, tap.x - center.x) * 180 / PI
  val share = (degrees + QUARTER_TURN + FULL_TURN) % FULL_TURN / FULL_TURN
  var sum = 0.0
  values.forEachIndexed { index, value ->
    sum += value / total
    if (share < sum) return index
  }
  return values.lastIndex
}

private const val QUARTER_TURN = 90.0
private const val FULL_TURN = 360.0
