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
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastSumBy
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

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
  scrollState: ScrollState,
  autoHide: Boolean = false,
  enabled: Boolean = true,
  flingBehavior: FlingBehavior? = null,
  reverseScrolling: Boolean = false,
): Modifier = verticalScrollbar(scrollState, autoHide)
  .verticalScroll(scrollState, enabled, flingBehavior, reverseScrolling)

@Composable
fun Modifier.verticalScrollbar(
  scrollState: ScrollState,
  autoHide: Boolean = SCROLLBAR_AUTO_HIDE_DEFAULT,
): Modifier = scrollbar(
  scrollbarState = remember(scrollState) { ScrollbarState(scrollState, Orientation.Vertical) },
  autoHide = autoHide,
)

@Composable
fun Modifier.horizontalScrollWithBar(
  scrollState: ScrollState,
  autoHide: Boolean = false,
  enabled: Boolean = true,
  flingBehavior: FlingBehavior? = null,
  reverseScrolling: Boolean = false,
): Modifier = horizontalScrollbar(scrollState, autoHide)
  .horizontalScroll(scrollState, enabled, flingBehavior, reverseScrolling)

@Composable
fun Modifier.horizontalScrollbar(
  scrollState: ScrollState,
  autoHide: Boolean = SCROLLBAR_AUTO_HIDE_DEFAULT,
): Modifier = scrollbar(
  scrollbarState = remember(scrollState) { ScrollbarState(scrollState, Orientation.Horizontal) },
  autoHide = autoHide,
)

internal fun Modifier.scrollbar(
  scrollbarState: ScrollbarState,
  autoHide: Boolean = SCROLLBAR_AUTO_HIDE_DEFAULT,
): Modifier = this then ScrollbarElement(scrollbarState, autoHide)

private data class ScrollbarElement(
  private val scrollbarState: ScrollbarState,
  private val autoHide: Boolean = SCROLLBAR_AUTO_HIDE_DEFAULT,
) : ModifierNodeElement<ScrollbarNode>() {
  override fun create() = ScrollbarNode(scrollbarState)

  override fun update(node: ScrollbarNode) {
    node.scrollbarState = scrollbarState
    node.autoHide = autoHide
  }

  override fun InspectorInfo.inspectableProperties() = Unit
}

private class ScrollbarNode(
  scrollbarState: ScrollbarState,
  autoHide: Boolean = SCROLLBAR_AUTO_HIDE_DEFAULT,
) : Modifier.Node(),
  DrawModifierNode,
  CompositionLocalConsumerModifierNode,
  ObserverModifierNode,
  PointerInputModifierNode {
  var scrollbarState by mutableStateOf(scrollbarState)
  var autoHide by mutableStateOf(autoHide)

  val scrollbarColor: Color
    get() = currentValueOf(LocalTheme).scrollbar.copy(SCROLLBAR_ALPHA)

  private var fadeJob: Job? = null

  private val alpha = Animatable(1f)

  override fun onAttach() = showAndStateAutoFadeIfEnabled()

  override fun onPointerEvent(pointerEvent: PointerEvent, pass: PointerEventPass, bounds: IntSize) {
    if (pointerEvent.type == PointerEventType.Press && pass == PointerEventPass.Initial) {
      showAndStateAutoFadeIfEnabled()
    }
  }

  override fun onCancelPointerInput() = Unit

  override fun onObservedReadsChanged() = showAndStateAutoFadeIfEnabled()

  private fun showAndStateAutoFadeIfEnabled() {
    fadeJob?.cancel()
    fadeJob = coroutineScope.launch {
      if (autoHide) {
        alpha.animateTo(1f)
        delay(SCROLLBAR_FADE_DELAY)
        alpha.animateTo(0f, animationSpec = spring(stiffness = Spring.StiffnessVeryLow))
      } else {
        alpha.snapTo(1f)
      }
    }
    observeChanges()
  }

  private fun observeChanges() = observeReads {
    autoHide
    scrollbarState.orientation
    scrollbarState.scrollPosition
  }

  override fun ContentDrawScope.draw() {
    drawContent()
    if (scrollbarState.scrollbarSize == 0f) return
    val strokeWidthPx = SCROLLBAR_STROKE_WIDTH.toPx()
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
    drawRect(
      color = scrollbarColor,
      topLeft = scrollbarPosition,
      size = scrollbarSize,
      alpha = alpha.value,
      style = Fill,
    )
  }
}

private val SCROLLBAR_FADE_DELAY = 2.seconds
private val SCROLLBAR_STROKE_WIDTH = 3.dp
private const val SCROLLBAR_ALPHA = 0.52f
private const val SCROLLBAR_AUTO_HIDE_DEFAULT = true

internal data class ScrollbarState(private val delegate: ScrollableDelegate) {
  val orientation: Orientation get() = delegate.orientation

  val scrollPosition: Float by derivedStateOf {
    delegate.scrollPosition divByOrZero delegate.totalSize
  }

  val scrollbarSize: Float by derivedStateOf {
    with(delegate) { if (viewportSize >= totalSize) 0f else viewportSize divByOrZero totalSize }
  }

  constructor(state: LazyListState) :
    this(LazyListStateScrollableDelegate(state))

  constructor(state: ScrollState, orientation: Orientation) :
    this(ScrollStateScrollableDelegate(state, orientation))
}

internal interface ScrollableDelegate : ScrollableState {
  val orientation: Orientation
  val scrollPosition: Int
  val viewportSize: Int
  val totalSize: Int
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
}

private infix fun Int.divByOrZero(other: Int) = if (other == 0) 0f else this / other.toFloat()
