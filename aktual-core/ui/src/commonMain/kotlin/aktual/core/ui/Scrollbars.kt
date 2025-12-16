package aktual.core.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.ObserverModifierNode
import androidx.compose.ui.node.PointerInputModifierNode
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.node.observeReads
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastSumBy
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@Composable
internal expect fun isScrollbarInteractionEnabled(): Boolean

@Composable
fun Modifier.scrollbar(
  lazyListState: LazyListState,
  autoHide: Boolean = SCROLLBAR_AUTO_HIDE_DEFAULT,
): Modifier = scrollbar(
  scrollbarState = remember(lazyListState) { ScrollbarState(lazyListState) },
  autoHide = autoHide,
)

@Composable
fun Modifier.verticalScrollWithBar(
  scrollState: ScrollState = rememberScrollState(),
  autoHide: Boolean = false,
  enabled: Boolean = true,
  flingBehavior: FlingBehavior? = null,
  reverseScrolling: Boolean = false,
): Modifier = verticalScrollbar(scrollState, autoHide)
  .verticalScroll(scrollState, enabled, flingBehavior, reverseScrolling)

@Composable
fun Modifier.verticalScrollbar(
  scrollState: ScrollState = rememberScrollState(),
  autoHide: Boolean = SCROLLBAR_AUTO_HIDE_DEFAULT,
): Modifier = scrollbar(
  scrollbarState = remember(scrollState) { ScrollbarState(scrollState, Orientation.Vertical) },
  autoHide = autoHide,
)

@Composable
fun Modifier.horizontalScrollWithBar(
  scrollState: ScrollState = rememberScrollState(),
  autoHide: Boolean = false,
  enabled: Boolean = true,
  flingBehavior: FlingBehavior? = null,
  reverseScrolling: Boolean = false,
): Modifier = horizontalScrollbar(scrollState, autoHide)
  .horizontalScroll(scrollState, enabled, flingBehavior, reverseScrolling)

@Composable
fun Modifier.horizontalScrollbar(
  scrollState: ScrollState = rememberScrollState(),
  autoHide: Boolean = SCROLLBAR_AUTO_HIDE_DEFAULT,
): Modifier = scrollbar(
  scrollbarState = remember(scrollState) { ScrollbarState(scrollState, Orientation.Horizontal) },
  autoHide = autoHide,
)

@Composable
internal fun Modifier.scrollbar(
  scrollbarState: ScrollbarState,
  autoHide: Boolean = SCROLLBAR_AUTO_HIDE_DEFAULT,
): Modifier {
  val interactionEnabled = isScrollbarInteractionEnabled()
  return this then ScrollbarElement(scrollbarState, autoHide, interactionEnabled)
}

private data class ScrollbarElement(
  private val scrollbarState: ScrollbarState,
  private val autoHide: Boolean = SCROLLBAR_AUTO_HIDE_DEFAULT,
  private val interactionEnabled: Boolean = false,
) : ModifierNodeElement<ScrollbarNode>() {
  override fun create() = ScrollbarNode(scrollbarState, autoHide, interactionEnabled)

  override fun update(node: ScrollbarNode) {
    node.scrollbarState = scrollbarState
    node.autoHide = autoHide
    node.interactionEnabled = interactionEnabled
  }

  override fun InspectorInfo.inspectableProperties() = Unit
}

private class ScrollbarNode(
  scrollbarState: ScrollbarState,
  autoHide: Boolean = SCROLLBAR_AUTO_HIDE_DEFAULT,
  interactionEnabled: Boolean = false,
) : Modifier.Node(),
  DrawModifierNode,
  CompositionLocalConsumerModifierNode,
  ObserverModifierNode,
  PointerInputModifierNode {
  var scrollbarState by mutableStateOf(scrollbarState)
  var autoHide by mutableStateOf(autoHide)
  var interactionEnabled by mutableStateOf(interactionEnabled)

  val scrollbarColor: Color
    get() = currentValueOf(LocalTheme).scrollbar.copy(SCROLLBAR_ALPHA)

  private var fadeJob: Job? = null

  private val alpha = Animatable(1f)
  private val widthMultiplier = Animatable(1f)
  private var isHovered by mutableStateOf(false)

  private enum class GestureState {
    IDLE,
    CLICKING,
    DRAGGING,
  }

  private var gestureState by mutableStateOf(GestureState.IDLE)
  private var dragStartOffset: Offset? = null

  override fun onAttach() = showAndStateAutoFadeIfEnabled()

  override fun onPointerEvent(pointerEvent: PointerEvent, pass: PointerEventPass, bounds: IntSize) {
    if (!interactionEnabled) {
      if (pointerEvent.type == PointerEventType.Press && pass == PointerEventPass.Initial) {
        showAndStateAutoFadeIfEnabled()
      }
      return
    }

    if (pass != PointerEventPass.Initial) return

    when (pointerEvent.type) {
      PointerEventType.Press -> handlePress(pointerEvent, bounds)
      PointerEventType.Move -> handleMove(pointerEvent, bounds)
      PointerEventType.Release -> handleRelease()
      PointerEventType.Exit -> handlePointerExit()
    }
  }

  private fun handlePress(event: PointerEvent, bounds: IntSize) {
    val position = event.changes.firstOrNull()?.position ?: return
    updateHoverState(position, bounds)

    if (isPointerOnThumb(position, bounds)) {
      event.changes.forEach { it.consume() }
      gestureState = GestureState.CLICKING
      dragStartOffset = position
      showAndStateAutoFadeIfEnabled()
    } else if (isPointerOnScrollbar(position, bounds)) {
      event.changes.forEach { it.consume() }
      scrollToPosition(position, bounds, animated = true)
      showAndStateAutoFadeIfEnabled()
    }
  }

  private fun handleMove(event: PointerEvent, bounds: IntSize) {
    val position = event.changes.firstOrNull()?.position ?: return
    updateHoverState(position, bounds)

    if (gestureState == GestureState.IDLE) return

    event.changes.forEach { it.consume() }

    if (gestureState == GestureState.CLICKING) {
      gestureState = GestureState.DRAGGING
      updateWidthMultiplier() // Ensure scrollbar stays expanded when dragging starts
    }

    if (gestureState == GestureState.DRAGGING) {
      scrollToPosition(position, bounds, animated = false)
    }
  }

  private fun handleRelease() {
    gestureState = GestureState.IDLE
    dragStartOffset = null
    updateWidthMultiplier() // Shrink scrollbar if not hovering
    if (!isHovered) {
      showAndStateAutoFadeIfEnabled() // Restart fade if not hovering
    }
  }

  private fun updateHoverState(position: Offset, bounds: IntSize) {
    val shouldBeHovered = isPointerNearScrollbar(position, bounds)
    if (shouldBeHovered != isHovered) {
      isHovered = shouldBeHovered
      updateWidthMultiplier()

      if (isHovered) {
        // Cancel fade when hovering
        fadeJob?.cancel()
      } else if (gestureState == GestureState.IDLE) {
        // Only restart fade when un-hovering if we're not dragging
        showAndStateAutoFadeIfEnabled()
      }
    }
  }

  private fun updateWidthMultiplier() {
    val shouldBeExpanded = isHovered || gestureState == GestureState.DRAGGING
    coroutineScope.launch {
      widthMultiplier.animateTo(
        targetValue = if (shouldBeExpanded) SCROLLBAR_HOVER_WIDTH_MULTIPLIER else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
      )
    }
  }

  private fun handlePointerExit() {
    if (isHovered) {
      isHovered = false
      updateWidthMultiplier() // Will stay expanded if dragging
      if (gestureState == GestureState.IDLE) {
        // Only restart fade when pointer exits if we're not dragging
        showAndStateAutoFadeIfEnabled()
      }
    }
  }

  private fun isPointerNearScrollbar(position: Offset, bounds: IntSize): Boolean {
    val density = currentValueOf(LocalDensity)
    val proximityThresholdPx = with(density) { SCROLLBAR_HOVER_PROXIMITY.toPx() }
    return if (scrollbarState.orientation == Orientation.Vertical) {
      position.x >= bounds.width - proximityThresholdPx
    } else {
      position.y >= bounds.height - proximityThresholdPx
    }
  }

  override fun onCancelPointerInput() {
    gestureState = GestureState.IDLE
    dragStartOffset = null
    updateWidthMultiplier() // Shrink scrollbar if not hovering
    if (!isHovered) {
      showAndStateAutoFadeIfEnabled() // Restart fade if not hovering
    }
  }

  override fun onObservedReadsChanged() = showAndStateAutoFadeIfEnabled()

  private fun showAndStateAutoFadeIfEnabled() {
    fadeJob?.cancel()
    fadeJob = coroutineScope.launch {
      alpha.animateTo(1f)
      if (autoHide && !isHovered) {
        delay(SCROLLBAR_FADE_DELAY)
        if (!isHovered) {
          alpha.animateTo(0f, animationSpec = spring(stiffness = Spring.StiffnessVeryLow))
        }
      }
    }
    observeChanges()
  }

  private fun observeChanges() = observeReads {
    autoHide
    scrollbarState.orientation
    scrollbarState.scrollPosition
  }

  private fun isPointerOnScrollbar(position: Offset, bounds: IntSize): Boolean {
    val density = currentValueOf(LocalDensity)
    val baseStrokeWidthPx = with(density) { SCROLLBAR_STROKE_WIDTH.toPx() }
    val strokeWidthPx = baseStrokeWidthPx * widthMultiplier.value
    return if (scrollbarState.orientation == Orientation.Vertical) {
      position.x >= bounds.width - strokeWidthPx
    } else {
      position.y >= bounds.height - strokeWidthPx
    }
  }

  private fun isPointerOnThumb(position: Offset, bounds: IntSize): Boolean {
    if (!isPointerOnScrollbar(position, bounds)) return false

    val normalizedPosition = if (scrollbarState.orientation == Orientation.Vertical) {
      position.y / bounds.height
    } else {
      position.x / bounds.width
    }

    val thumbStart = scrollbarState.scrollPosition
    val thumbEnd = thumbStart + scrollbarState.scrollbarSize

    return normalizedPosition in thumbStart..thumbEnd
  }

  private fun calculateTargetScroll(position: Offset, bounds: IntSize): Int {
    val normalizedPosition = if (scrollbarState.orientation == Orientation.Vertical) {
      (position.y / bounds.height).coerceIn(0f, 1f)
    } else {
      (position.x / bounds.width).coerceIn(0f, 1f)
    }

    val totalSize = scrollbarState.totalSize
    val viewportSize = scrollbarState.viewportSize
    val maxScroll = (totalSize - viewportSize).coerceAtLeast(0)
    return (normalizedPosition * maxScroll).toInt().coerceIn(0, maxScroll)
  }

  private fun scrollToPosition(position: Offset, bounds: IntSize, animated: Boolean) {
    val targetScroll = calculateTargetScroll(position, bounds)

    coroutineScope.launch {
      if (animated) {
        scrollbarState.animateScrollTo(targetScroll)
      } else {
        val delta = targetScroll - scrollbarState.scrollPositionPx
        scrollbarState.scrollBy(delta.toFloat())
      }
    }
  }

  override fun ContentDrawScope.draw() {
    drawContent()
    if (scrollbarState.scrollbarSize == 0f) return
    val baseStrokeWidthPx = SCROLLBAR_STROKE_WIDTH.toPx()
    val strokeWidthPx = baseStrokeWidthPx * widthMultiplier.value
    val scrollbarPosition = if (scrollbarState.orientation == Orientation.Vertical) {
      Offset(size.width - strokeWidthPx, size.height * scrollbarState.scrollPosition)
    } else {
      Offset(size.width * scrollbarState.scrollPosition, size.height - strokeWidthPx)
    }
    val scrollbarSize = if (scrollbarState.orientation == Orientation.Vertical) {
      Size(strokeWidthPx, size.height * scrollbarState.scrollbarSize)
    } else {
      Size(size.width * scrollbarState.scrollbarSize, strokeWidthPx)
    }
    val cornerRadius = CornerRadius(strokeWidthPx / 2f, strokeWidthPx / 2f)
    drawRoundRect(
      color = scrollbarColor,
      topLeft = scrollbarPosition,
      size = scrollbarSize,
      cornerRadius = cornerRadius,
      alpha = alpha.value,
      style = Fill,
    )
  }
}

private val SCROLLBAR_FADE_DELAY = 2.seconds
private val SCROLLBAR_STROKE_WIDTH = 3.dp
private val SCROLLBAR_HOVER_PROXIMITY = 32.dp
private const val SCROLLBAR_ALPHA = 0.52f
private const val SCROLLBAR_AUTO_HIDE_DEFAULT = true
private const val SCROLLBAR_HOVER_WIDTH_MULTIPLIER = 4f

@Stable
internal data class ScrollbarState(private val delegate: ScrollableDelegate) {
  val orientation: Orientation get() = delegate.orientation

  val scrollPosition: Float by derivedStateOf {
    delegate.scrollPosition divByOrZero delegate.totalSize
  }

  val scrollbarSize: Float by derivedStateOf {
    with(delegate) { if (viewportSize >= totalSize) 0f else viewportSize divByOrZero totalSize }
  }

  val totalSize: Int get() = delegate.totalSize
  val viewportSize: Int get() = delegate.viewportSize
  val scrollPositionPx: Int get() = delegate.scrollPosition

  suspend fun animateScrollTo(value: Int) {
    delegate.animateScrollTo(value)
  }

  suspend fun scrollBy(value: Float): Float {
    var consumed = 0f
    delegate.scroll {
      consumed = scrollBy(value)
    }
    return consumed
  }

  constructor(state: LazyListState) :
    this(LazyListStateScrollableDelegate(state))

  constructor(state: ScrollState, orientation: Orientation) :
    this(ScrollStateScrollableDelegate(state, orientation))
}

internal sealed interface ScrollableDelegate : ScrollableState {
  val orientation: Orientation
  val scrollPosition: Int
  val viewportSize: Int
  val totalSize: Int

  suspend fun animateScrollTo(value: Int)
}

private data class LazyListStateScrollableDelegate(
  private val lazyListState: LazyListState,
) : ScrollableDelegate, ScrollableState by lazyListState {
  override val orientation get() = lazyListState.layoutInfo.orientation

  override val viewportSize by derivedStateOf {
    with(lazyListState.layoutInfo) {
      if (orientation == Orientation.Vertical) viewportSize.height else viewportSize.width
    }
  }

  override val scrollPosition by derivedStateOf {
    with(lazyListState) {
      averageItemSize * firstVisibleItemIndex + firstVisibleItemScrollOffset
    }
  }

  override val totalSize by derivedStateOf {
    with(lazyListState.layoutInfo) {
      averageItemSize * totalItemsCount + beforeContentPadding + afterContentPadding
    }
  }

  override suspend fun animateScrollTo(value: Int) {
    val avgSize = averageItemSize
    if (avgSize == 0) return
    val targetIndex = value / avgSize
    val targetOffset = value % avgSize
    lazyListState.animateScrollToItem(targetIndex, targetOffset)
  }

  private val averageItemSize by derivedStateOf {
    lazyListState.layoutInfo
      .visibleItemsInfo
      .takeIf { it.isNotEmpty() }
      ?.run { fastSumBy { it.size } / size }
      ?: 0
  }
}

private data class ScrollStateScrollableDelegate(
  private val scrollState: ScrollState,
  override val orientation: Orientation,
) : ScrollableDelegate, ScrollableState by scrollState {
  override val scrollPosition: Int get() = scrollState.value
  override val totalSize: Int get() = scrollState.maxValue + viewportSize
  override val viewportSize: Int get() = scrollState.viewportSize

  override suspend fun animateScrollTo(value: Int) {
    scrollState.animateScrollTo(value)
  }
}

private infix fun Int.divByOrZero(other: Int) = if (other == 0) 0f else this / other.toFloat()
