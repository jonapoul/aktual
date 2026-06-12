package aktual.budget.tags.ui.list

import aktual.budget.tags.vm.list.ListTagsViewModel
import aktual.budget.tags.vm.list.TagItem
import aktual.core.icons.material.ArrowRight
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.CardShape
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParameters
import aktual.core.ui.ColoredParams
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.RowShape
import aktual.core.ui.blurredTopBar
import aktual.core.ui.rememberBlurredTopBarState
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun ListTagsScreen(
  modifier: Modifier = Modifier,
  @Suppress("UNUSED_PARAMETER") viewModel: ListTagsViewModel = metroViewModel(),
) {
  val tags by viewModel.tags.collectAsStateWithLifecycle()

  ListTagsScaffold(
    modifier = modifier,
    tags = tags,
  )
}

@Composable
private fun ListTagsScaffold(
  tags: ImmutableList<TagItem>,
  modifier: Modifier = Modifier,
) {
  val blurState = rememberBlurredTopBarState()
  val listState = rememberLazyListState()

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        modifier = Modifier.blurredTopBar(blurState, listState),
        colors = colors.transparentTopAppBarColors(),
        title = { Text(text = Strings.tagsTitle) },
      )
    },
  ) { innerPadding ->
    Box(modifier = Modifier.fillMaxSize()) {
      PageBackground()

      LazyColumn(
        modifier = Modifier.fillMaxSize().padding(innerPadding),
        contentPadding = ListTagsDS.listContentPadding,
        verticalArrangement = Arrangement.spacedBy(ListTagsDS.listItemSpacing),
        state = listState,
      ) {
        items(tags, key = { it.id.value }) { tag ->
          TagItem(
            modifier = Modifier.animateItem(),
            tag = tag,
          )
        }
      }
    }
  }
}

@Composable
private fun TagItem(
  tag: TagItem,
  modifier: Modifier = Modifier,
  onViewTransactions: () -> Unit = {},
) {
  val contentAlpha = if (tag.hidden) ListTagsDS.HIDDEN_ALPHA else 1f

  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(RowShape)
        .background(colors.tableBackground, RowShape)
        .border(Dp.Hairline, colors.tableBorder, RowShape)
        .clickable(onClick = onViewTransactions)
        .padding(ListTagsDS.itemPadding),
    horizontalArrangement = Arrangement.spacedBy(ListTagsDS.itemHorizontalSpacing),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(
      modifier = Modifier.weight(1f).alpha(contentAlpha),
      verticalArrangement = Arrangement.spacedBy(ListTagsDS.itemContentSpacing),
    ) {
      TagChip(text = tag.tag, color = tag.color)

      Text(
        text = tag.description.ifEmpty { Strings.tagsNoDescription },
        style = typography.bodySmall,
        color = if (tag.description.isEmpty()) colors.tableTextLight else colors.tableText,
        fontStyle = if (tag.description.isEmpty()) FontStyle.Italic else FontStyle.Normal,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
      )
    }

    ViewTransactionsButton(
      modifier = Modifier.alpha(contentAlpha),
      onClick = onViewTransactions,
    )
  }
}

@Composable
private fun TagChip(
  text: String,
  color: Color,
  modifier: Modifier = Modifier,
) {
  Text(
    text = "#$text",
    modifier =
      modifier
        .clip(ListTagsDS.chipShape)
        .background(color, ListTagsDS.chipShape)
        .padding(ListTagsDS.chipPadding),
    style = typography.bodyMedium,
    fontWeight = FontWeight.SemiBold,
    color = color.contrastingTextColor(),
    maxLines = 1,
    overflow = TextOverflow.Ellipsis,
  )
}

@Composable
private fun ViewTransactionsButton(
  modifier: Modifier = Modifier,
  onClick: () -> Unit = {},
) {
  Row(
    modifier =
      modifier
        .clip(CardShape)
        .background(colors.noticeBackground, CardShape)
        .clickable(onClick = onClick)
        .padding(ListTagsDS.buttonPadding),
    horizontalArrangement = Arrangement.spacedBy(ListTagsDS.buttonContentSpacing),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = Strings.tagsViewTransactions,
      style = typography.labelMedium,
      color = colors.noticeTextDark,
      maxLines = 1,
    )
    Icon(
      imageVector = MaterialIcons.ArrowRight,
      contentDescription = null,
      tint = colors.noticeTextDark,
      modifier = Modifier.size(ListTagsDS.buttonIconSize),
    )
  }
}

// pick black or white text for legibility on [this], using the brightness formula from
// https://www.w3.org/TR/AERT/#color-contrast — adapted from upstream's getTagCSSColors in
// packages/desktop-client/src/hooks/useTagCSS.ts
@Suppress("MagicNumber")
private fun Color.contrastingTextColor(): Color {
  val brightness = (red * 299 + green * 587 + blue * 114) * 255 / 1000
  return if (brightness >= 125) Color.Black else Color.White
}

private class TagItemProvider : ColoredParameterProvider<TagItem>(TagsPreview.all)

@Preview
@Composable
private fun PreviewTagItem(
  @PreviewParameter(TagItemProvider::class) params: ColoredParams<TagItem>
) = PreviewWithColoredParams(params) { TagItem(tag = this) }

@PortraitPreview
@Composable
private fun PreviewListTagsScaffold(@PreviewParameter(ColoredParameters::class) colors: Colors) =
  PreviewWithColors(colors) { ListTagsScaffold(tags = TagsPreview.all) }
