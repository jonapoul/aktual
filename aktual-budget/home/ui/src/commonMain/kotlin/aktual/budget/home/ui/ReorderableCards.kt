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
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.util.fastForEachIndexed
import kotlinx.collections.immutable.ImmutableList

// Every card stays a child of the same layout, keyed by its type, so a moved card slides to its
// new place, even into the other column
@Composable
internal fun ReorderableCards(
  cards: ImmutableList<HomeCardType>,
  columns: Int,
  isEditing: Boolean,
  onAction: HomeActionHandler,
  modifier: Modifier = Modifier,
  card: @Composable (HomeCardType) -> Unit,
) {
  LookaheadScope {
    CardColumns(modifier = modifier, columns = columns, spacing = Dimens.VeryLarge) {
      cards.fastForEachIndexed { index, type ->
        key(type) {
          Column(
            modifier = if (isEditing) Modifier.animateBounds(this@LookaheadScope) else Modifier
          ) {
            AnimatedVisibility(
              visible = isEditing,
              enter = expandVertically() + fadeIn(),
              exit = shrinkVertically() + fadeOut(),
            ) {
              ReorderBar(
                modifier = Modifier.padding(bottom = Dimens.Large),
                card = type,
                previous = cards.getOrNull(index - 1),
                next = cards.getOrNull(index + 1),
                onAction = onAction,
              )
            }
            card(type)
          }
        }
      }
    }
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
    Accounts -> Strings.homeAccountsTitle
  }

// Fills the first column before the second. Empty cards take no space, including their gap
@Composable
private fun CardColumns(
  columns: Int,
  spacing: Dp,
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
      placeables.fastForEachIndexed { index, placeable -> placeable.place(positions[index]) }
    }
  }
}
