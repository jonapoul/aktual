package aktual.budget.tags.ui.list

import aktual.budget.tags.ui.contrastingTextColor
import aktual.budget.tags.vm.list.TagItem
import aktual.core.icons.material.Delete
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.RowShape
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt

// the two resting positions of a swipeable row: closed, or swiped left to reveal the delete button
private enum class SwipeState {
  Closed,
  Open,
}

@Composable
internal fun TagItem(
  tag: TagItem,
  isOpen: Boolean,
  onOpenChange: (Boolean) -> Unit,
  onAction: ListTagsActionHandler,
  modifier: Modifier = Modifier,
) {
  val buttonWidthPx = with(LocalDensity.current) { ListTagsDS.deleteButtonWidth.toPx() }
  val swipeState =
    remember(buttonWidthPx) {
      AnchoredDraggableState(initialValue = SwipeState.Closed).apply {
        updateAnchors(
          DraggableAnchors {
            SwipeState.Closed at 0f
            SwipeState.Open at -buttonWidthPx
          }
        )
      }
    }

  // tell the parent when this row settles open or closed, so it can keep only one row open
  val currentOnOpenChange by rememberUpdatedState(onOpenChange)
  LaunchedEffect(swipeState) {
    snapshotFlow { swipeState.settledValue }
      .collect { settled -> currentOnOpenChange(settled == SwipeState.Open) }
  }

  // claim the "open" slot the moment a drag begins, so any other open row closes straight
  // away rather than waiting for this drag to finish
  val interactionSource = remember { MutableInteractionSource() }
  LaunchedEffect(interactionSource) {
    interactionSource.interactions.collect { interaction ->
      if (interaction is DragInteraction.Start) currentOnOpenChange(true)
    }
  }

  // when another row becomes the open one, slide this row shut
  LaunchedEffect(isOpen) {
    if (!isOpen && swipeState.currentValue != SwipeState.Closed) {
      swipeState.animateTo(SwipeState.Closed)
    }
  }

  Box(modifier = modifier.fillMaxWidth().clip(RowShape)) {
    // the red delete button sits behind the row, revealed as it's dragged left
    Row(modifier = Modifier.matchParentSize(), horizontalArrangement = Arrangement.End) {
      DeleteButton(
        modifier = Modifier.fillMaxHeight().width(ListTagsDS.deleteButtonWidth),
        onClick = { onAction(DeleteTag(tag.id)) },
      )
    }

    TagItemRow(
      tag = tag,
      onAction = onAction,
      modifier =
        Modifier.offset {
            val x = swipeState.offset
            IntOffset(x = if (x.isNaN()) 0 else x.roundToInt(), y = 0)
          }
          .anchoredDraggable(
            state = swipeState,
            orientation = Orientation.Horizontal,
            interactionSource = interactionSource,
          ),
    )
  }
}

@Composable
private fun TagItemRow(
  tag: TagItem,
  onAction: ListTagsActionHandler,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(RowShape)
        .background(colors.tableBackground, RowShape)
        .border(Dp.Hairline, colors.tableBorder, RowShape)
        .clickable { onAction(EditTag(tag.id)) }
        .padding(ListTagsDS.itemPadding),
    horizontalArrangement = Arrangement.spacedBy(ListTagsDS.itemHorizontalSpacing),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(
      modifier = Modifier.weight(1f).alpha(if (tag.hidden) ListTagsDS.HIDDEN_ALPHA else 1f),
      verticalArrangement = Arrangement.spacedBy(ListTagsDS.itemContentSpacing),
    ) {
      TagChip(text = tag.tag, color = tag.color)

      Text(
        text = tag.description.ifEmpty { Strings.tagsNoDescription },
        style = typography.bodySmall,
        color = if (tag.description.isEmpty()) colors.tableTextLight else colors.tableText,
        fontStyle = if (tag.description.isEmpty()) FontStyle.Italic else FontStyle.Normal,
        maxLines = 5,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
private fun DeleteButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val background = colors.errorText
  Box(
    modifier = modifier.background(background).clickable(onClick = onClick),
    contentAlignment = Alignment.Center,
  ) {
    Icon(
      imageVector = MaterialIcons.Delete,
      contentDescription = Strings.tagsDelete,
      tint = background.contrastingTextColor(),
    )
  }
}

@Composable
private fun TagChip(
  text: String,
  color: Color?,
  modifier: Modifier = Modifier,
) {
  // upstream falls back to the theme's note-tag colors when a tag has no explicit color
  val background = color ?: colors.noteTagBackground
  val textColor = color?.contrastingTextColor() ?: colors.noteTagText

  Text(
    text = "#$text",
    modifier =
      modifier
        .clip(ListTagsDS.chipShape)
        .background(background, ListTagsDS.chipShape)
        .padding(ListTagsDS.chipPadding),
    style = typography.bodyMedium,
    fontWeight = FontWeight.SemiBold,
    color = textColor,
    maxLines = 1,
    overflow = TextOverflow.Ellipsis,
  )
}

private class TagItemProvider : ColoredParameterProvider<TagItem>(TagsPreview.all)

@Preview
@Composable
private fun PreviewTagItem(
  @PreviewParameter(TagItemProvider::class) params: ColoredParams<TagItem>
) =
  PreviewWithColoredParams(params) {
    TagItem(tag = this, isOpen = false, onOpenChange = {}, onAction = {})
  }
