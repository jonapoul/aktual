package aktual.budget.reports.components

import aktual.core.ui.AktualTheme.colors
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateZoom
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.data.LineCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.decoration.Decoration
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.ShapeComponent
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

internal typealias XRange = ClosedFloatingPointRange<Double>

/**
 * Lets a full screen chart be zoomed into an x range, by pinching with two fingers (or dragging
 * across it with a mouse). Dragging with one finger still moves the marker.
 */
@Stable
class ChartZoomState {
  /** The zoomed x range, or null when showing everything. */
  var range by mutableStateOf<XRange?>(null)
    private set

  // Pointer x positions of an in-progress selection
  private var selection by mutableStateOf<Pair<Float, Float>?>(null)

  // Captured each draw to map pointer positions to x values. Plain var so draws don't recompose
  private var geometry: ZoomGeometry? = null

  // The x range of the whole chart, from its last draw without a zoom range
  private var bounds: XRange? = null

  // The chart layout when an in-progress pinch began
  private var pinch: Pinch? = null

  // Model changes after zooming only move the range, so they're drawn in place without animating
  private var hasZoomed by mutableStateOf(false)

  fun reset() {
    updateRange(null)
  }

  @Composable
  internal fun animationSpec(): AnimationSpec<Float>? =
    if (hasZoomed) null else chartAnimationSpec()

  private fun updateRange(range: XRange?) {
    hasZoomed = true
    this.range = range
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
    geometry?.range(from, to)?.let(::updateRange)
  }

  internal fun startPinch(centroid: Float) {
    val geometry = geometry ?: return
    val bounds = bounds ?: return
    pinch = Pinch(geometry, bounds, centroid)
  }

  internal fun movePinch(zoom: Float, centroid: Float) {
    val pinch = pinch ?: return
    updateRange(
      pinch.geometry.pinched(pinch.bounds, from = pinch.centroid, to = centroid, zoom = zoom),
    )
  }

  internal fun finishPinch() {
    pinch = null
  }

  private data class Pinch(val geometry: ZoomGeometry, val bounds: XRange, val centroid: Float)

  internal fun decoration(
    fill: Fill,
    edgeColor: Color,
    trackColor: Color,
  ): Decoration = SelectionDecoration(ShapeComponent(fill), edgeColor, trackColor)

  private inner class SelectionDecoration(
    private val shape: ShapeComponent,
    private val edgeColor: Color,
    trackColor: Color,
  ) : Decoration {
    private val edgePaint = Paint()
    private val trackPaint = Paint().apply { color = trackColor }
    private val thumbPaint = Paint().apply { color = edgeColor.copy(alpha = 1f) }

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
        val full = this@ChartZoomState.bounds
        val isZoomed = model.extraStore.getOrNull(ZoomRangeKey) != null
        if (!isZoomed) {
          this@ChartZoomState.bounds = ranges.minX..ranges.maxX
        } else if (full != null) {
          if (ranges.minX > full.start) drawEdge(atStart = true)
          if (ranges.maxX < full.endInclusive) drawEdge(atStart = false)
          drawScrollbar(full)
        }

        val (from, to) = selection ?: return@with
        val left = min(from, to).coerceIn(bounds.left, bounds.right)
        val right = max(from, to).coerceIn(bounds.left, bounds.right)
        shape.draw(context, left, bounds.top, right, bounds.bottom)
      }

    // Shows where the visible range sits within the whole chart, along the bottom of the plot
    private fun CartesianDrawingContext.drawScrollbar(full: XRange) {
      val span = full.endInclusive - full.start
      if (span <= 0.0) return
      val bounds = layerBounds
      val height = with(density) { ZOOM_SCROLLBAR_HEIGHT.toPx() }
      val top = bounds.bottom - height
      val startFraction = ((ranges.minX - full.start) / span).toFloat()
      val endFraction = ((ranges.maxX - full.start) / span).toFloat()
      val (from, to) =
        if (isLtr) {
          startFraction to endFraction
        } else {
          1f - endFraction to 1f - startFraction
        }
      canvas.drawRect(
        left = bounds.left,
        top = top,
        right = bounds.right,
        bottom = bounds.bottom,
        paint = trackPaint,
      )
      canvas.drawRect(
        left = bounds.left + from * bounds.width,
        top = top,
        right = bounds.left + to * bounds.width,
        bottom = bounds.bottom,
        paint = thumbPaint,
      )
    }

    // Fades in from an edge with data hidden past it
    private fun CartesianDrawingContext.drawEdge(atStart: Boolean) {
      val bounds = layerBounds
      val width = with(density) { ZOOM_EDGE_WIDTH.toPx() }
      val isLeft = atStart == isLtr
      val edge = if (isLeft) bounds.left else bounds.right
      val inner = if (isLeft) edge + width else edge - width
      edgePaint.shader =
        LinearGradientShader(
          from = Offset(edge, 0f),
          to = Offset(inner, 0f),
          colors = listOf(edgeColor, Transparent),
        )
      canvas.drawRect(min(edge, inner), bounds.top, max(edge, inner), bounds.bottom, edgePaint)
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

  /**
   * The range after pinching by [zoom] from this layout, keeping the x value under the [from]
   * centroid under the [to] centroid. Snaps to x values and stays within [bounds], with null when
   * that covers everything.
   */
  fun pinched(bounds: XRange, from: Float, to: Float, zoom: Float): XRange? {
    if (spacing == 0f || xStep <= 0.0 || maxX <= minX) return null
    val fullSpan = bounds.endInclusive - bounds.start
    val span = ((maxX - minX) / zoom).coerceIn(min(xStep, fullSpan), fullSpan)
    val anchor = minX + (from - start) / spacing * xStep
    val unclamped = anchor - (to - start) / spacing * xStep * (span / (maxX - minX))
    val first = unclamped.coerceIn(bounds.start, bounds.endInclusive - span)
    val firstSteps = ((first - bounds.start) / xStep).roundToInt()
    val spanSteps = max((span / xStep).roundToInt(), 1)
    val snappedFirst = bounds.start + firstSteps * xStep
    val snappedLast = min(snappedFirst + spanSteps * xStep, bounds.endInclusive)
    val isWholeRange = snappedFirst <= bounds.start && snappedLast >= bounds.endInclusive
    return if (isWholeRange) null else snappedFirst..snappedLast
  }
}

@Composable
fun rememberChartZoomState(data: Any?): ChartZoomState = remember(data) { ChartZoomState() }

@Composable
internal fun rememberChartZoomDecoration(
  state: ChartZoomState,
): Decoration {
  val color = colors.reportsBlue.copy(alpha = SELECTION_ALPHA)
  val edgeColor = colors.reportsBlue.copy(alpha = ZOOM_EDGE_ALPHA)
  val trackColor = colors.reportsBlue.copy(alpha = ZOOM_TRACK_ALPHA)
  return remember(state, color, edgeColor, trackColor) {
    state.decoration(Fill(color), edgeColor, trackColor)
  }
}

/**
 * Hosts a chart with zoom gestures. [content] should apply the passed modifier to its
 * CartesianChartHost, so pointer positions line up with the chart canvas. The gestures sit on this
 * box rather than the host, since the host swaps its layout node once the model loads, which would
 * cancel a selection in progress
 */
@Composable
internal fun ZoomableChart(
  state: ChartZoomState,
  enabled: Boolean,
  modifier: Modifier = Modifier,
  content: @Composable BoxScope.(Modifier) -> Unit,
) {
  val gestures = if (enabled) Modifier.zoomGestures(state) else Modifier
  Box(modifier = modifier.testTag(ChartTags.ZoomableChart).then(gestures)) {
    content(Modifier.fillMaxSize())
  }
}

private fun Modifier.zoomGestures(state: ChartZoomState): Modifier =
  pointerInput(state) {
    // Vico handles presses and moves for the marker without checking consumption, so listen on the
    // initial pass
    awaitEachGesture {
      val down = awaitFirstDown(requireUnconsumed = false, pass = Initial)
      if (down.type == Mouse) awaitMouseSelection(state, down) else awaitPinch(state)
    }
  }

// Zooms while two or more fingers are down, until they all lift
private suspend fun AwaitPointerEventScope.awaitPinch(state: ChartZoomState) {
  var zoom: Float? = null
  try {
    do {
      val event = awaitPointerEvent(Initial)
      val pressed = event.changes.count { it.pressed && it.previousPressed }
      if (pressed >= 2) {
        val centroid = event.calculateCentroid(useCurrent = true).x
        if (zoom == null) {
          zoom = 1f
          state.startPinch(event.calculateCentroid(useCurrent = false).x)
        }
        zoom *= event.calculateZoom()
        state.movePinch(zoom, centroid)
        event.changes.forEach { it.consume() }
      }
    } while (event.changes.any { it.pressed })
  } finally {
    state.finishPinch()
  }
}

// Selects the range dragged across, once the mouse moves past the touch slop. The marker keeps
// following the pointer while selecting, showing the edge value
private suspend fun AwaitPointerEventScope.awaitMouseSelection(
  state: ChartZoomState,
  down: PointerInputChange,
) {
  var change: PointerInputChange?
  do {
    change = awaitPointerEvent(Initial).changes.firstOrNull { it.id == down.id }
    if (change?.pressed != true) return
  } while ((change.position - down.position).getDistance() <= viewConfiguration.touchSlop)

  state.startSelection(down.position.x)
  try {
    while (change?.pressed == true) {
      change.consume()
      state.moveSelection(change.position.x)
      change = awaitPointerEvent(Initial).changes.firstOrNull { it.id == down.id }
    }
    if (change != null) state.finishSelection()
  } finally {
    state.cancelSelection()
  }
}

private object ZoomRangeKey : ExtraStore.Key<XRange>()

/**
 * Wraps [base] to clamp the x range to the zoom range set by [zoomTo] in the same transaction, so
 * the data and range change together.
 */
internal class ZoomRangeProvider(
  private val base: CartesianLayerRangeProvider = CartesianLayerRangeProvider.auto(),
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
      ?.copy(y = 0.0),
  )

private const val SELECTION_ALPHA = 0.2f
private const val ZOOM_EDGE_ALPHA = 0.35f
private val ZOOM_EDGE_WIDTH = 24.dp
private const val ZOOM_TRACK_ALPHA = 0.15f
private val ZOOM_SCROLLBAR_HEIGHT = 3.dp
