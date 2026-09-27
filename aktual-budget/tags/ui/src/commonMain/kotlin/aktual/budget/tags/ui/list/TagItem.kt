package aktual.budget.tags.ui.list

import aktual.budget.tags.vm.list.TagItem
import aktual.core.icons.material.Delete
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Plurals
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.CardShape
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.RowShape
import aktual.core.ui.SwipeAction
import aktual.core.ui.SwipeToReveal
import aktual.core.ui.contrastingTextColor
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun TagItem(
  tag: TagItem,
  isOpen: Boolean,
  onOpenChange: (Boolean) -> Unit,
  onAction: ListTagsActionHandler,
  modifier: Modifier = Modifier,
) {
  val deleteBackground = colors.errorText
  SwipeToReveal(
    actions =
      persistentListOf(
        SwipeAction(
          text = Strings.tagsDelete,
          icon = MaterialIcons.Delete,
          background = deleteBackground,
          foreground = deleteBackground.contrastingTextColor(),
          onClick = { onAction(DeleteTag(tag.id)) },
        )
      ),
    isOpen = isOpen,
    onOpenChange = onOpenChange,
    modifier = modifier,
    shape = RowShape,
  ) {
    TagItemRow(tag = tag, onAction = onAction)
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
        .border(Hairline, colors.tableBorder, RowShape)
        .clickable { onAction(EditTag(tag.id)) }
        .padding(ListTagsDS.itemPadding),
    horizontalArrangement = Arrangement.spacedBy(ListTagsDS.itemHorizontalSpacing),
    verticalAlignment = CenterVertically,
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
        fontStyle = if (tag.description.isEmpty()) Italic else Normal,
        maxLines = 5,
        overflow = Ellipsis,
      )

      if (tag.numTransactions > 0) {
        Box(
          modifier =
            Modifier.background(colors.pillBackgroundSelected, CardShape)
              .clickable { onAction(ViewTransactions(tag.id)) }
              .padding(ListTagsDS.numTransactionsPadding),
          contentAlignment = Center,
        ) {
          Text(
            text = Plurals.tagsNumTransactions(tag.numTransactions, tag.numTransactions),
            style = typography.bodySmall,
            color = colors.pillText,
          )
        }
      }
    }
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
    fontWeight = SemiBold,
    color = textColor,
    maxLines = 1,
    overflow = Ellipsis,
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
