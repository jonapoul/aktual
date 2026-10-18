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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateBounds
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.center
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
  onAction: HomeActionHandler,
  modifier: Modifier = Modifier,
  card: @Composable (HomeCardType) -> Unit,
) {
  val scope = rememberCoroutineScope()
  val haptics = LocalHapticFeedback.current
  val drag =
    remember(cards) { CardDragState(cards, scope) { order -> onAction(ReorderCards(order)) } }
  val order = drag.order

  LookaheadScope {
    CardColumns(
      modifier = modifier,
      columns = columns,
      spacing = Dimens.VeryLarge,
      dragged = order.indexOf(drag.dragged).takeIf { it >= 0 },
      dragPosition = { drag.position.round() },
      onPlaced = { slots -> drag.place(order, slots) },
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
                        onDragStart = {
                          if (drag.pickUp(type)) haptics.performHapticFeedback(LongPress)
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

  var position by mutableStateOf<Offset>(Zero)
    private set

  private var slots = emptyMap<HomeCardType, IntRect>()
  private var slotsOrder: List<HomeCardType>? = null

  fun place(order: List<HomeCardType>, slots: List<IntRect>) {
    this.slots = order.zip(slots).toMap()
    slotsOrder = order
  }

  fun pickUp(card: HomeCardType): Boolean {
    val slot = slots[card]
    if (dragged != null || slot == null) return false
    position = slot.topLeft.toOffset()
    dragged = card
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

  fun drop(card: HomeCardType) {
    if (dragged != card) return
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

@Composable
private fun Modifier.wiggle(enabled: Boolean, index: Int): Modifier {
  if (!enabled) return this
  val transition = rememberInfiniteTransition()
  val degrees by
    transition.animateFloat(
      initialValue = -WIGGLE_DEGREES,
      targetValue = WIGGLE_DEGREES,
      animationSpec =
        infiniteRepeatable(
          animation = tween(WIGGLE_MILLIS),
          repeatMode = Reverse,
          initialStartOffset = StartOffset(index * WIGGLE_STAGGER_MILLIS),
        ),
    )
  return graphicsLayer { rotationZ = degrees }
}

private const val DRAGGED_SCALE = 1.03f
private const val WIGGLE_DEGREES = 0.5f
private const val WIGGLE_MILLIS = 140
private const val WIGGLE_STAGGER_MILLIS = 60

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
    Accounts -> Strings.homeAccountsTitle
  }

// Fills the first column before the second. Empty cards take no space, including their gap. The
// dragged card is drawn wherever it's been dragged to, on top of the others
@Composable
private fun CardColumns(
  columns: Int,
  spacing: Dp,
  dragged: Int?,
  dragPosition: () -> IntOffset,
  onPlaced: (List<IntRect>) -> Unit,
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
