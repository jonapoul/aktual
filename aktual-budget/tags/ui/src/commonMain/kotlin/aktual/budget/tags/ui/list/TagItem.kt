package aktual.budget.tags.ui.list

import aktual.budget.tags.ui.contrastingTextColor
import aktual.budget.tags.vm.list.TagItem
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp

@Composable
internal fun TagItem(
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
) = PreviewWithColoredParams(params) { TagItem(tag = this, onAction = {}) }
