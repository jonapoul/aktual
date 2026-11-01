package aktual.budget.home.ui

import aktual.budget.home.vm.HomeCardType
import aktual.core.icons.material.ArrowDownward
import aktual.core.icons.material.ArrowUpward
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BareIconButton
import aktual.core.ui.Dimens
import aktual.core.ui.wiggle
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateBounds
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.center
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.toOffset
import androidx.compose.ui.util.fastForEachIndexed
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// Every card stays a child of the same layout, keyed by its type, so a moved card slides to its
// new place, even into the other column. While editing, a long press picks a card up to drag it
@Composable
internal fun ReorderableCards(
  cards: ImmutableList<HomeCardType>,
  columns: Int,
  isEditing: Boolean,
  scrollArea: AutoScrollArea,
  onAction: HomeActionHandler,
  modifier: Modifier = Modifier,
  card: @Composable (HomeCardType) -> Unit,
) {
  val scope = rememberCoroutineScope()
  val haptics = LocalHapticFeedback.current
  val density = LocalDensity.current
  val drag =
    remember(cards) { CardDragState(cards, scope) { order -> onAction(ReorderCards(order)) } }
  val order = drag.order

  // The drag gesture goes when editing stops, without always reporting that it was cancelled
  SideEffect(isEditing) { if (!isEditing) drag.dropHeld() }

  LaunchedEffect(drag, drag.held) {
    val held = drag.held ?: return@LaunchedEffect
    with(density) {
      drag.autoScroll(held, scrollArea, AutoScrollEdge.toPx(), AutoScrollSpeed.toPx())
    }
  }

  LookaheadScope {
    CardColumns(
      modifier = modifier,
      columns = columns,
      spacing = Dimens.ListItemSpacing,
      dragged = order.indexOf(drag.dragged).takeIf { it >= 0 },
      dragPosition = { drag.position.round() },
      onPlaced = { slots, coordinates -> drag.place(order, slots, coordinates) },
    ) {
      order.fastForEachIndexed { index, type ->
        key(type) {
          val isDragged = drag.dragged == type
          val isMovable = isEditing && !isDragged
          Column(
            modifier =
              Modifier.then(
                  if (isMovable) Modifier.animateBounds(this@LookaheadScope) else Modifier,
                )
                .wiggle(enabled = isMovable, index = index)
                .graphicsLayer {
                  val scale = if (isDragged) DRAGGED_SCALE else 1f
                  scaleX = scale
                  scaleY = scale
                },
          ) {
            AnimatedVisibility(
              visible = isEditing,
              enter = expandVertically() + fadeIn(),
              exit = shrinkVertically() + fadeOut(),
            ) {
              ReorderBar(
                modifier = Modifier.padding(bottom = Dimens.Large),
                card = type,
                previous = order.getOrNull(index - 1),
                next = order.getOrNull(index + 1),
                onAction = onAction,
              )
            }

            Box {
              card(type)
              if (isEditing) {
                // Covers the card so its own buttons don't react while editing
                Box(
                  modifier =
                    Modifier.matchParentSize().pointerInput(drag, type) {
                      detectDragGesturesAfterLongPress(
                        onDragStart = { offset ->
                          // The card sits below the reorder bar, so its offset is the difference
                          val grabY = drag.heightOf(type) - size.height + offset.y
                          if (drag.pickUp(type, grabY)) haptics.performHapticFeedback(LongPress)
                        },
                        onDragEnd = { drag.drop(type) },
                        onDragCancel = { drag.drop(type) },
                        onDrag = { change, delta ->
                          change.consume()
                          drag.dragBy(type, delta)
                        },
                      )
                    },
                )
              }
            }
          }
        }
      }
    }
  }
}

// Slots are written during placement, so they're plain fields rather than state
@Stable
private class CardDragState(
  private val cards: ImmutableList<HomeCardType>,
  private val scope: CoroutineScope,
  private val onReorder: (ImmutableList<HomeCardType>) -> Unit,
) {
  var order by mutableStateOf(cards.toPersistentList())
    private set

  var dragged by mutableStateOf<HomeCardType?>(null)
    private set

  // The dragged card while it's under a finger, before it settles into its slot
  var held by mutableStateOf<HomeCardType?>(null)
    private set

  var position by mutableStateOf<Offset>(Zero)
    private set

  private var slots = emptyMap<HomeCardType, IntRect>()
  private var slotsOrder: List<HomeCardType>? = null
  private var coordinates: LayoutCoordinates? = null
  private var grabY = 0f

  fun place(order: List<HomeCardType>, slots: List<IntRect>, coordinates: LayoutCoordinates?) {
    this.slots = order.zip(slots).toMap()
    slotsOrder = order
    if (coordinates != null) this.coordinates = coordinates
  }

  fun heightOf(card: HomeCardType): Int = slots[card]?.height ?: 0

  fun pickUp(card: HomeCardType, grabY: Float): Boolean {
    val slot = slots[card]
    if (dragged != null || slot == null) return false
    position = slot.topLeft.toOffset()
    this.grabY = grabY
    dragged = card
    held = card
    return true
  }

  // A card in the same column has to be passed halfway before they swap, otherwise a short card
  // dragged onto a tall one would swap straight back
  fun dragBy(card: HomeCardType, delta: Offset) {
    if (dragged != card) return
    position += delta
    if (slotsOrder != order) return
    val slot = slots[card] ?: return
    val center = position.round() + slot.size.center
    val target = order.find { it != card && slots[it]?.contains(center) == true } ?: return
    val targetSlot = slots.getValue(target)
    val from = order.indexOf(card)
    val to = order.indexOf(target)
    val isPastMiddle =
      targetSlot.left != slot.left ||
        if (to > from) center.y > targetSlot.center.y else center.y < targetSlot.center.y
    if (isPastMiddle) order = order.removingAt(from).addingAt(to, card)
  }

  // Scrolls faster the further past the edge the finger is. The card moves with the content so it
  // stays under the finger
  suspend fun autoScroll(card: HomeCardType, area: AutoScrollArea, edge: Float, speed: Float) {
    var lastFrame = withFrameNanos { it }
    while (held == card) {
      val frame = withFrameNanos { it }
      val seconds = (frame - lastFrame) / NANOS_PER_SECOND
      lastFrame = frame
      val factor = edgeFactor(area.viewport, edge)
      if (factor != 0f) {
        val scrolled = area.scrollState.scrollBy(factor * speed * seconds)
        dragBy(card, Offset(0f, scrolled))
      }
    }
  }

  private fun edgeFactor(viewport: Rect, edge: Float): Float {
    val coordinates = coordinates?.takeIf { it.isAttached } ?: return 0f
    val y = coordinates.positionInWindow().y + position.y + grabY
    return when {
      y < viewport.top + edge -> -((viewport.top + edge - y) / edge).coerceAtMost(1f)
      y > viewport.bottom - edge -> ((y - viewport.bottom + edge) / edge).coerceAtMost(1f)
      else -> 0f
    }
  }

  fun dropHeld() {
    held?.let(::drop)
  }

  fun drop(card: HomeCardType) {
    if (held != card) return
    held = null
    val slot = slots[card]
    scope.launch {
      if (slot != null) {
        animate(
          typeConverter = Offset.VectorConverter,
          initialValue = position,
          targetValue = slot.topLeft.toOffset(),
          animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        ) { value, _ ->
          position = value
        }
      }
      dragged = null
      if (order != cards) onReorder(order)
    }
  }
}

private const val DRAGGED_SCALE = 1.03f
private const val NANOS_PER_SECOND = 1_000_000_000f
private val AutoScrollEdge = 64.dp
private val AutoScrollSpeed = 1000.dp

// The scrolling area holding the cards, which a dragged card scrolls when held near its edges.
// The viewport is in window coordinates, leaving out any bars drawn over the content
@Stable
internal class AutoScrollArea(val scrollState: ScrollState) {
  var viewport: Rect = Zero
    private set

  fun place(coordinates: LayoutCoordinates, topInset: Float, bottomInset: Float) {
    val bounds = coordinates.boundsInWindow()
    viewport = bounds.copy(top = bounds.top + topInset, bottom = bounds.bottom - bottomInset)
  }
}

@Composable
private fun ReorderBar(
  card: HomeCardType,
  previous: HomeCardType?,
  next: HomeCardType?,
  onAction: HomeActionHandler,
  modifier: Modifier = Modifier,
) {
  val label = card.label()
  Row(
    modifier = modifier.padding(start = CardPadding),
    verticalAlignment = CenterVertically,
  ) {
    Text(
      modifier = Modifier.weight(1f),
      text = label,
      style = typography.titleSmall,
      color = colors.pageTextSubdued,
      maxLines = 1,
      overflow = Ellipsis,
    )

    BareIconButton(
      imageVector = MaterialIcons.ArrowUpward,
      contentDescription = Strings.homeReorderUp(label),
      enabled = previous != null,
      onClick = { if (previous != null) onAction(SwapCards(card, previous)) },
    )

    BareIconButton(
      imageVector = MaterialIcons.ArrowDownward,
      contentDescription = Strings.homeReorderDown(label),
      enabled = next != null,
      onClick = { if (next != null) onAction(SwapCards(card, next)) },
    )
  }
}

@Composable
private fun HomeCardType.label(): String =
  when (this) {
    ThisMonth -> Strings.homeCardThisMonth
    Attention -> Strings.homeAttentionTitle
    Upcoming -> Strings.homeCardUpcoming
    Recent -> Strings.homeRecentTitle
    Accounts -> Strings.homeAccountsTitle
    Reports -> Strings.homeCardReports
  }

// Fills the first column before the second. Empty cards take no space, including their gap. The
// dragged card is drawn wherever it's been dragged to, on top of the others
@Composable
private fun CardColumns(
  columns: Int,
  spacing: Dp,
  dragged: Int?,
  dragPosition: () -> IntOffset,
  onPlaced: (List<IntRect>, LayoutCoordinates?) -> Unit,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  Layout(content = content, modifier = modifier) { measurables, constraints ->
    val gap = spacing.roundToPx()
    val width = (constraints.maxWidth - gap * (columns - 1)) / columns
    val placeables = measurables.map { it.measure(Constraints.fixedWidth(width)) }
    val perColumn = ((placeables.size + columns - 1) / columns).coerceAtLeast(1)
    val heights = IntArray(columns)
    val positions = placeables.mapIndexed { index, placeable ->
      val column = index / perColumn
      val position = IntOffset(x = column * (width + gap), y = heights[column])
      if (placeable.height > 0) heights[column] += placeable.height + gap
      position
    }
    val height = heights.maxOf { (it - gap).coerceAtLeast(0) }

    layout(constraints.maxWidth, height) {
      onPlaced(
        placeables.mapIndexed { index, placeable ->
          IntRect(positions[index], IntSize(placeable.width, placeable.height))
        },
        coordinates,
      )
      placeables.fastForEachIndexed { index, placeable ->
        if (index == dragged) {
          placeable.place(dragPosition(), zIndex = 1f)
        } else {
          placeable.place(positions[index])
        }
      }
    }
  }
}
