package aktual.budget.tags.ui.list

import aktual.budget.tags.vm.list.TagItem
import aktual.core.icons.AktualIcons
import aktual.core.icons.CheveronRight
import aktual.core.icons.Trash
import aktual.core.l10n.Plurals
import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.CardShape
import aktual.core.ui.Chip
import aktual.core.ui.ChipDefaults
import aktual.core.ui.ChipSize
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParameters
import aktual.core.ui.ColoredParams
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.RowShape
import aktual.core.ui.SwipeAction
import aktual.core.ui.SwipeToReveal
import aktual.core.ui.TagChip
import aktual.core.ui.contrastingTextColor
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
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
          icon = AktualIcons.Trash,
          background = deleteBackground,
          foreground = deleteBackground.contrastingTextColor(),
          onClick = { onAction(DeleteTag(tag.id)) },
        ),
      ),
    isOpen = isOpen,
    onOpenChange = onOpenChange,
    modifier = modifier,
    shape = RowShape,
  ) {
    TagItemRow(tag = tag, onAction = onAction)
  }
}

/** Keep in sync with [TagItemRow] */
@Composable
internal fun ShimmerTagItem(modifier: Modifier = Modifier) {
  Column(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(RowShape)
        .background(colors.tableBackground, RowShape)
        .border(Hairline, colors.tableBorder, RowShape)
        .padding(ListTagsDS.itemPadding)
        .shimmer(rememberShimmer(Window)),
    verticalArrangement = Arrangement.spacedBy(ListTagsDS.itemContentSpacing),
  ) {
    Box(
      modifier =
        Modifier.width(ListTagsDS.shimmerChipWidth)
          .background(colors.pageText, ChipSize.Regular.shape)
          .padding(ChipSize.Regular.contentPadding),
    ) {
      Text(text = "", style = typography.bodyMedium, maxLines = 1)
    }
    Box(
      modifier =
        Modifier.fillMaxWidth(fraction = 0.7f)
          .height(ListTagsDS.shimmerTextHeight)
          .background(colors.pageText, CardShape),
    )
  }
}

/** Keep in sync with [ShimmerTagItem] */
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
        Chip(
          text = Plurals.tagsNumTransactions(tag.numTransactions, tag.numTransactions),
          colors = ChipDefaults.colors(background = colors.pillBackgroundSelected),
          size = Small,
          textStyle = typography.bodySmall,
          onClick = { onAction(ViewTransactions(tag.id)) },
          trailingIcon = AktualIcons.CheveronRight,
        )
      }
    }
  }
}

private class TagItemProvider : ColoredParameterProvider<TagItem>(TagsPreview.all)

@Preview
@Composable
private fun PreviewTagItem(
  @PreviewParameter(TagItemProvider::class) params: ColoredParams<TagItem>,
) =
  PreviewWithColoredParams(params) {
    TagItem(tag = this, isOpen = false, onOpenChange = {}, onAction = {})
  }

@Preview
@Composable
private fun PreviewShimmerTagItem(@PreviewParameter(ColoredParameters::class) colors: Colors) =
  PreviewWithColors(colors) { ShimmerTagItem() }
