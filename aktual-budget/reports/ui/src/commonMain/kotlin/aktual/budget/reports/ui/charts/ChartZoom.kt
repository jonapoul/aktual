package aktual.budget.reports.ui.charts

import aktual.budget.reports.ui.Tags
import aktual.core.ui.AktualTheme.colors
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEventPass.Initial
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import com.patrykandpatrick.vico.multiplatform.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.multiplatform.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.multiplatform.cartesian.data.LineCartesianLayerModel
import com.patrykandpatrick.vico.multiplatform.cartesian.decoration.Decoration
import com.patrykandpatrick.vico.multiplatform.common.Fill
import com.patrykandpatrick.vico.multiplatform.common.component.ShapeComponent
import com.patrykandpatrick.vico.multiplatform.common.data.ExtraStore
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

internal typealias XRange = ClosedFloatingPointRange<Double>

/**
 * Lets a full screen chart be zoomed into an x range, by long pressing then dragging across it (or
 * just dragging with a mouse). Dragging without the long press still moves the marker.
 */
@Stable
internal class ChartZoomState {
  /** The zoomed x range, or null when showing everything. */
  var range by mutableStateOf<XRange?>(null)
    private set

  // Pointer x positions of an in-progress selection
  private var selection by mutableStateOf<Pair<Float, Float>?>(null)

  // Captured each draw to map pointer positions to x values. Plain var so draws don't recompose
  private var geometry: ZoomGeometry? = null

  fun reset() {
    range = null
  }

  internal fun startSelection(x: Float) {
    selection = x to x
  }

  internal fun moveSelection(x: Float) {
    selection = selection?.copy(second = x)
  }

  internal fun cancelSelection() {
    selection = null
  }

  internal fun finishSelection() {
    val (from, to) = selection ?: return
    selection = null
    geometry?.range(from, to)?.let { range = it }
  }

  internal fun decoration(fill: Fill): Decoration = SelectionDecoration(ShapeComponent(fill))

  private inner class SelectionDecoration(private val shape: ShapeComponent) : Decoration {
    override fun drawOverLayers(context: CartesianDrawingContext) =
      with(context) {
        val bounds = layerBounds
        val geometry =
          ZoomGeometry(
            start =
              (if (isLtr) bounds.left else bounds.right) +
                layoutDirectionMultiplier * layerDimensions.startPadding - scroll,
            spacing = layoutDirectionMultiplier * layerDimensions.xSpacing,
            minX = ranges.minX,
            maxX = ranges.maxX,
            xStep = ranges.xStep,
          )
        this@ChartZoomState.geometry = geometry

        val (from, to) = selection ?: return@with
        val left = min(from, to).coerceIn(bounds.left, bounds.right)
        val right = max(from, to).coerceIn(bounds.left, bounds.right)
        shape.draw(context, left, bounds.top, right, bounds.bottom)
      }
  }
}

// Maps canvas x positions to x values, from the chart's layout on its last draw
internal class ZoomGeometry(
  private val start: Float,
  private val spacing: Float,
  private val minX: Double,
  private val maxX: Double,
  private val xStep: Double,
) {
  // Snaps to the nearest x value, so zoom bounds always land on data points
  fun toX(canvasX: Float): Double {
    if (spacing == 0f) return minX
    val steps = ((canvasX - start) / spacing).roundToInt()
    return (minX + steps * xStep).coerceIn(minX, maxX)
  }

  // Null when the selection covers a single x value or everything already shown
  fun range(from: Float, to: Float): XRange? {
    val first = min(toX(from), toX(to))
    val last = max(toX(from), toX(to))
    val isWholeRange = first <= minX && last >= maxX
    return if (last - first >= xStep && !isWholeRange) first..last else null
  }
}

@Composable
internal fun rememberChartZoomState(data: Any?): ChartZoomState =
  remember(data) { ChartZoomState() }

@Composable
internal fun rememberChartZoomDecoration(state: ChartZoomState): Decoration {
  val color = colors.reportsBlue.copy(alpha = SELECTION_ALPHA)
  return remember(state, color) { state.decoration(Fill(color)) }
}

/**
 * Hosts a chart with zoom gestures. [content] should apply the passed modifier to its
 * CartesianChartHost, so pointer positions line up with the chart canvas.
 */
@Composable
internal fun ZoomableChart(
  state: ChartZoomState,
  enabled: Boolean,
  modifier: Modifier = Modifier,
  content: @Composable BoxScope.(Modifier) -> Unit,
) =
  Box(modifier = modifier.testTag(Tags.ZoomableChart)) {
    val haptics = LocalHapticFeedback.current
    content(
      if (enabled) Modifier.fillMaxSize().zoomGestures(state, haptics) else Modifier.fillMaxSize()
    )
  }

private fun Modifier.zoomGestures(state: ChartZoomState, haptics: HapticFeedback): Modifier =
  pointerInput(state, haptics) {
    // Vico handles presses and moves for the marker without checking consumption, so listen on the
    // initial pass. The marker keeps following the pointer while selecting, showing the edge value
    awaitEachGesture {
      val down = awaitFirstDown(requireUnconsumed = false, pass = Initial)
      if (!awaitSelectionStart(down)) return@awaitEachGesture

      haptics.performHapticFeedback(LongPress)
      state.startSelection(down.position.x)
      try {
        var change: PointerInputChange?
        do {
          change = awaitPointerEvent(Initial).changes.firstOrNull { it.id == down.id }
          change?.consume()
          if (change?.pressed == true) state.moveSelection(change.position.x)
        } while (change?.pressed == true)
        if (change != null) state.finishSelection()
      } finally {
        state.cancelSelection()
      }
    }
  }

// True after a long press, or once a mouse drags past the touch slop
private suspend fun AwaitPointerEventScope.awaitSelectionStart(down: PointerInputChange): Boolean {
  // Null when nothing happened before the timeout, i.e. a long press
  val beforeLongPress =
    withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
      var started: Boolean? = null
      while (started == null) {
        val event = awaitPointerEvent(Initial)
        val change = event.changes.firstOrNull { it.id == down.id }
        started =
          when {
            change == null || !change.pressed || event.changes.size > 1 -> false
            (change.position - down.position).getDistance() > viewConfiguration.touchSlop ->
              down.type == Mouse
            else -> null
          }
      }
      started == true
    }
  return beforeLongPress == null || beforeLongPress
}

private object ZoomRangeKey : ExtraStore.Key<XRange>()

/**
 * Wraps [base] to clamp the x range to the zoom range set by [zoomTo] in the same transaction, so
 * the data and range change together.
 */
internal class ZoomRangeProvider(
  private val base: CartesianLayerRangeProvider = CartesianLayerRangeProvider.auto()
) : CartesianLayerRangeProvider {
  override fun getMinX(minX: Double, maxX: Double, extraStore: ExtraStore) =
    extraStore.getOrNull(ZoomRangeKey)?.start ?: base.getMinX(minX, maxX, extraStore)

  override fun getMaxX(minX: Double, maxX: Double, extraStore: ExtraStore) =
    extraStore.getOrNull(ZoomRangeKey)?.endInclusive ?: base.getMaxX(minX, maxX, extraStore)

  override fun getMinY(minY: Double, maxY: Double, extraStore: ExtraStore) =
    base.getMinY(minY, maxY, extraStore)

  override fun getMaxY(minY: Double, maxY: Double, extraStore: ExtraStore) =
    base.getMaxY(minY, maxY, extraStore)
}

internal fun CartesianChartModelProducer.Transaction.zoomTo(range: XRange?) = extras { store ->
  range?.let { store[ZoomRangeKey] = it }
}

internal fun LineCartesianLayerModel.BuilderScope.series(
  x: List<Number>,
  y: List<Number>,
  zoom: XRange?,
) = zoomedLine(points(x, y), zoom).let { p -> series(x = p.map { it.x }, y = p.map { it.y }) }

internal fun ColumnCartesianLayerModel.BuilderScope.series(
  x: List<Number>,
  y: List<Number>,
  zoom: XRange?,
) = zoomedColumns(points(x, y), zoom).let { p -> series(x = p.map { it.x }, y = p.map { it.y }) }

internal data class ZoomPoint(val x: Double, val y: Double)

private fun points(x: List<Number>, y: List<Number>) =
  x.zip(y) { px, py -> ZoomPoint(px.toDouble(), py.toDouble()) }.sortedBy { it.x }

// Adds points interpolated at the range edges, so lines run to the chart edges without points
// outside the range stretching the y axis
internal fun zoomedLine(points: List<ZoomPoint>, zoom: XRange?): List<ZoomPoint> {
  if (zoom == null) return points
  val inRange = points.filter { it.x in zoom }
  val start = interpolate(points, zoom.start)?.takeIf { inRange.none { p -> p.x == it.x } }
  val end = interpolate(points, zoom.endInclusive)?.takeIf { inRange.none { p -> p.x == it.x } }
  return (listOfNotNull(start) + inRange + listOfNotNull(end)).ifEmpty { nearest(points, zoom) }
}

internal fun zoomedColumns(points: List<ZoomPoint>, zoom: XRange?): List<ZoomPoint> =
  if (zoom == null) points else points.filter { it.x in zoom }.ifEmpty { nearest(points, zoom) }

// Where the line between the points either side of x crosses it, if there are points either side
private fun interpolate(points: List<ZoomPoint>, x: Double): ZoomPoint? {
  val after = points.indexOfFirst { it.x >= x }
  if (after <= 0) return null
  val (x0, y0) = points[after - 1]
  val (x1, y1) = points[after]
  return ZoomPoint(x, y0 + (y1 - y0) * (x - x0) / (x1 - x0))
}

// Vico rejects empty series, so a series with nothing in range keeps its closest point for
// ZoomRangeProvider to clip off. It's never drawn, so its y is zeroed to keep it out of the y range
// (every range provider used here already includes zero)
private fun nearest(points: List<ZoomPoint>, zoom: XRange): List<ZoomPoint> =
  listOfNotNull(
    points
      .minByOrNull { if (it.x < zoom.start) zoom.start - it.x else it.x - zoom.endInclusive }
      ?.copy(y = 0.0)
  )

private const val SELECTION_ALPHA = 0.2f
