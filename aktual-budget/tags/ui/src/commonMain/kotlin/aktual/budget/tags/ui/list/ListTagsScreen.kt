package aktual.budget.tags.ui.list

import aktual.budget.tags.vm.list.Empty
import aktual.budget.tags.vm.list.Failure
import aktual.budget.tags.vm.list.ListTagsState
import aktual.budget.tags.vm.list.ListTagsViewModel
import aktual.budget.tags.vm.list.Loading
import aktual.budget.tags.vm.list.Success
import aktual.budget.tags.vm.list.TagItem
import aktual.core.icons.material.Add
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Refresh
import aktual.core.icons.material.Search
import aktual.core.icons.material.SearchOff
import aktual.core.l10n.Plurals
import aktual.core.l10n.Strings
import aktual.core.nav.EditTagNavigator
import aktual.core.ui.AktualTextField
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BareIconButton
import aktual.core.ui.BlurredPullToRefreshBox
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.FailureAction
import aktual.core.ui.FailureScreen
import aktual.core.ui.LoadingScreen
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.blurredTopBar
import aktual.core.ui.rememberBlurredTopBarState
import aktual.core.ui.scrollbar
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun ListTagsScreen(
  toEdit: EditTagNavigator,
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
        CreateTag -> toEdit()
        is EditTag -> toEdit(action.id)
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
          BareIconButton(
            imageVector = MaterialIcons.Add,
            contentDescription = Strings.tagsCreate,
            onClick = { onAction(CreateTag) },
          )
        },
      )
    },
  ) { innerPadding ->
    Box(modifier = Modifier.fillMaxSize()) {
      PageBackground()

      BlurredPullToRefreshBox(
        modifier = Modifier.padding(ListTagsDS.listPadding),
        contentAlignment = Alignment.Center,
        onRefresh = { onAction(Reload) },
        isRefreshing = state is Loading,
        blurState = blurState,
        innerPadding = innerPadding,
      ) { padding ->
        ListTagsContent(
          state = state,
          onAction = onAction,
          listState = listState,
          padding = padding,
        )
      }
    }
  }
}

@Composable
private fun ListTagsContent(
  state: ListTagsState,
  onAction: ListTagsActionHandler,
  listState: LazyListState,
  padding: PaddingValues,
) {
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
        background = colors.tableBackground,
        action =
          FailureAction(
            text = { Strings.tagsCreate },
            icon = MaterialIcons.Add,
            onClick = { onAction(CreateTag) },
          ),
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
        TagsList(
          tags = state.tags,
          listState = listState,
          contentPadding = padding,
          onAction = onAction,
        )
      }
  }
}

@Composable
private fun Title(
  isSearchActive: Boolean,
  successState: Success?,
  onAction: ListTagsActionHandler,
  modifier: Modifier = Modifier,
) {
  AnimatedContent(
    modifier = modifier,
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
  contentPadding: PaddingValues,
  onAction: ListTagsActionHandler,
  modifier: Modifier = Modifier,
) {
  LazyColumn(
    modifier = modifier.fillMaxSize().scrollbar(listState),
    state = listState,
    contentPadding = contentPadding,
    verticalArrangement = Arrangement.spacedBy(ListTagsDS.listItemSpacing),
  ) {
    items(tags, key = { it.id.value }) { tag ->
      TagItem(
        modifier = Modifier.animateItem(),
        tag = tag,
        onAction = onAction,
      )
    }
  }
}

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
