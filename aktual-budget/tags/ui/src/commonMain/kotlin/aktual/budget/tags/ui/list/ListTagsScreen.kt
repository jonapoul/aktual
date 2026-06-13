package aktual.budget.tags.ui.list

import aktual.budget.tags.vm.list.Empty
import aktual.budget.tags.vm.list.Failure
import aktual.budget.tags.vm.list.ListTagsState
import aktual.budget.tags.vm.list.ListTagsViewModel
import aktual.budget.tags.vm.list.Loading
import aktual.budget.tags.vm.list.Success
import aktual.budget.tags.vm.list.TagItem
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Refresh
import aktual.core.icons.material.Search
import aktual.core.icons.material.SearchOff
import aktual.core.l10n.Plurals
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTextField
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BareIconButton
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.FailureAction
import aktual.core.ui.FailureScreen
import aktual.core.ui.LoadingScreen
import aktual.core.ui.NormalIconButton
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.RowShape
import aktual.core.ui.blurredTopBar
import aktual.core.ui.rememberBlurredTopBarState
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun ListTagsScreen(
  modifier: Modifier = Modifier,
  viewModel: ListTagsViewModel = metroViewModel(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()

  ListTagsScaffold(
    modifier = modifier,
    state = state,
    onAction = { action ->
      when (action) {
        Reload -> viewModel.reload()
        OpenSearch -> viewModel.openSearch()
        is EditFilterText -> viewModel.setFilterText(action.text)
        ClearFilter -> viewModel.clearFilter()
      }
    },
  )
}

@Composable
private fun ListTagsScaffold(
  state: ListTagsState,
  onAction: ListTagsActionHandler,
  modifier: Modifier = Modifier,
) {
  val blurState = rememberBlurredTopBarState()
  val listState = rememberLazyListState()
  val successState = state as? Success

  val isSearchActive = successState?.isSearchActive == true

  Scaffold(
    modifier = modifier.fillMaxSize().imePadding(),
    topBar = {
      TopAppBar(
        modifier = Modifier.blurredTopBar(blurState, listState),
        colors = colors.transparentTopAppBarColors(),
        title = { Title(isSearchActive, successState, onAction) },
        actions = {
          BareIconButton(
            imageVector = if (isSearchActive) MaterialIcons.SearchOff else MaterialIcons.Search,
            contentDescription = Strings.tagsFilter,
            onClick = { onAction(if (isSearchActive) ClearFilter else OpenSearch) },
          )
        },
      )
    },
  ) { innerPadding ->
    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
      PageBackground()

      when (state) {
        Loading -> LoadingScreen()

        is Failure ->
          FailureScreen(
            title = Strings.tagsFailurePrefix,
            reason = state.cause ?: Strings.tagsFailureDefaultMessage,
            background = colors.tableBackground,
            action =
              FailureAction(
                text = { Strings.syncRetry },
                icon = MaterialIcons.Refresh,
                onClick = { onAction(Reload) },
              ),
          )

        Empty ->
          FailureScreen(
            title = Strings.tagsEmpty,
            reason = null,
            icon = null,
            action = null,
            background = colors.tableBackground,
          )

        is Success ->
          if (state.tags.isEmpty()) {
            FailureScreen(
              title = Strings.tagsNoResults,
              reason = null,
              icon = null,
              background = colors.tableBackground,
              action =
                FailureAction(
                  text = { Strings.tagsFilterClear },
                  icon = MaterialIcons.SearchOff,
                  onClick = { onAction(ClearFilter) },
                ),
            )
          } else {
            TagsList(tags = state.tags, listState = listState)
          }
      }
    }
  }
}

@Composable
private fun Title(
  isSearchActive: Boolean,
  successState: Success?,
  onAction: ListTagsActionHandler,
) {
  AnimatedContent(
    targetState = isSearchActive,
    transitionSpec = { fadeIn() togetherWith fadeOut() },
  ) { searching ->
    if (searching) {
      CompositionLocalProvider(LocalTextStyle provides typography.bodyLarge) {
        FilterInput(successState?.filterText, successState?.tags?.size, onAction)
      }
    } else {
      Text(text = Strings.tagsTitle)
    }
  }
}

@Composable
private fun FilterInput(
  filterText: String?,
  numResults: Int?,
  onAction: ListTagsActionHandler,
  modifier: Modifier = Modifier,
) {
  val state = rememberTextFieldState(initialText = filterText.orEmpty())
  val focusRequester = remember { FocusRequester() }

  LaunchedEffect(Unit) { focusRequester.requestFocus() }

  LaunchedEffect(state) {
    snapshotFlow { state.text.toString() }.collect { filter -> onAction(EditFilterText(filter)) }
  }

  AktualTextField(
    modifier = modifier.focusRequester(focusRequester).fillMaxWidth(),
    state = state,
    singleLine = true,
    placeholderText = Strings.tagsFilterPlaceholder,
    showBorder = false,
    supportingText = numResults?.let { n -> { Text(text = Plurals.tagsNumResults(n, n)) } },
  )
}

@Composable
private fun TagsList(
  tags: ImmutableList<TagItem>,
  listState: LazyListState,
  modifier: Modifier = Modifier,
) {
  LazyColumn(
    modifier = modifier.fillMaxSize(),
    state = listState,
    contentPadding = ListTagsDS.listContentPadding,
    verticalArrangement = Arrangement.spacedBy(ListTagsDS.listItemSpacing),
  ) {
    items(tags, key = { it.id.value }) { tag ->
      TagItem(
        modifier = Modifier.animateItem(),
        tag = tag,
      )
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

    NormalIconButton(
      modifier = Modifier.alpha(contentAlpha),
      imageVector = MaterialIcons.Search,
      contentDescription = Strings.tagsViewTransactions,
      onClick = onViewTransactions,
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

private class ListTagsStateProvider :
  ColoredParameterProvider<ListTagsState>(
    Success(tags = TagsPreview.all, filterText = "", isSearchActive = false),
    Success(tags = TagsPreview.all, filterText = "gro", isSearchActive = true),
    Success(tags = persistentListOf(), filterText = "xyz", isSearchActive = true),
    Empty,
    Loading,
    Failure("Database connection lost"),
  )

@PortraitPreview
@Composable
private fun PreviewListTagsScaffold(
  @PreviewParameter(ListTagsStateProvider::class) params: ColoredParams<ListTagsState>
) = PreviewWithColoredParams(params) { ListTagsScaffold(state = this, onAction = {}) }
