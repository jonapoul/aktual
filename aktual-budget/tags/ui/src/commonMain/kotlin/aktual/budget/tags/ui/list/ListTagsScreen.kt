package aktual.budget.tags.ui.list

import aktual.budget.model.TagId
import aktual.budget.model.TagSort
import aktual.budget.tags.vm.list.Empty
import aktual.budget.tags.vm.list.Failure
import aktual.budget.tags.vm.list.ListTagsState
import aktual.budget.tags.vm.list.ListTagsViewModel
import aktual.budget.tags.vm.list.Loading
import aktual.budget.tags.vm.list.Success
import aktual.budget.tags.vm.list.TagItem
import aktual.core.icons.Add
import aktual.core.icons.AktualIcons
import aktual.core.icons.DotsHorizontalTriple
import aktual.core.icons.Refresh
import aktual.core.icons.SearchAlternate
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Sort
import aktual.core.l10n.Strings
import aktual.core.nav.EditTagNavigator
import aktual.core.nav.SearchTagsNavigator
import aktual.core.nav.TransactionsNavigator
import aktual.core.theme.Colors
import aktual.core.ui.AktualDropdownMenu
import aktual.core.ui.AktualDropdownMenuItem
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.BareIconButton
import aktual.core.ui.BottomSpacing
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParameters
import aktual.core.ui.ColoredParams
import aktual.core.ui.Dimens
import aktual.core.ui.FailureAction
import aktual.core.ui.FailureScreen
import aktual.core.ui.LocalSnackbars
import aktual.core.ui.NavDrawerIconButton
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.SortDialog
import aktual.core.ui.hazedTopBar
import aktual.core.ui.hazedTopBarContent
import aktual.core.ui.hazedTopBarContentPadding
import aktual.core.ui.rememberHazedTopBarState
import aktual.core.ui.scrollbar
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun ListTagsScreen(
  toEdit: EditTagNavigator,
  toTransactions: TransactionsNavigator,
  toSearch: SearchTagsNavigator,
  modifier: Modifier = Modifier,
  viewModel: ListTagsViewModel = metroViewModel(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val snackbars = LocalSnackbars.current

  LaunchedEffect(viewModel) {
    viewModel.events.collect { event ->
      when (event) {
        is Deleted -> snackbars.show { showDeleted(event, onUndo = viewModel::undoDelete) }
        is DeleteFailed -> snackbars.show { showDeleteFailed(event) }
        is RestoreFailed -> snackbars.show { showRestoreFailed(event) }
      }
    }
  }

  ListTagsScaffold(
    modifier = modifier,
    state = state,
    onAction = { action ->
      when (action) {
        Retry -> viewModel.retry()
        OpenSearch -> toSearch()
        CreateTag -> toEdit()
        is EditTag -> toEdit(action.id)
        is DeleteTag -> viewModel.delete(action.id)
        is ViewTransactions -> toTransactions(action.id)
        is SetSort -> viewModel.setSort(action.sort)
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
  val hazeState = rememberHazedTopBarState()
  val listState = rememberLazyListState()
  val successState = state as? Success
  var showSortDialog by remember { mutableStateOf(false) }

  if (showSortDialog) {
    val sort = successState?.sort ?: Default
    SortDialog(
      fields = TagSort.Field.Entries,
      field = sort.field,
      direction = sort.direction,
      fieldLabel = { it.label() },
      onConfirm = { field, direction ->
        onAction(SetSort(TagSort(field, direction)))
        showSortDialog = false
      },
      onDismiss = { showSortDialog = false },
    )
  }

  Scaffold(
    modifier = modifier.fillMaxSize().imePadding(),
    topBar = {
      TopAppBar(
        modifier = Modifier.hazedTopBar(hazeState, listState),
        colors = colors.transparentTopAppBarColors(),
        navigationIcon = { NavDrawerIconButton() },
        title = { Text(text = Strings.tagsTitle) },
        actions = {
          if (successState != null) {
            BareIconButton(
              imageVector = AktualIcons.SearchAlternate,
              contentDescription = Strings.tagsSearch,
              onClick = { onAction(OpenSearch) },
            )
          }
          BareIconButton(
            imageVector = AktualIcons.Add,
            contentDescription = Strings.tagsCreate,
            onClick = { onAction(CreateTag) },
          )
          if (successState != null) {
            ListTagsMenu(onSort = { showSortDialog = true })
          }
        },
      )
    },
  ) { innerPadding ->
    Box(modifier = Modifier.fillMaxSize()) {
      PageBackground()

      Box(
        modifier =
          Modifier.padding(horizontal = Dimens.ScreenPadding)
            .hazedTopBarContent(hazeState, innerPadding),
        contentAlignment = Center,
      ) {
        ListTagsContent(
          state = state,
          onAction = onAction,
          listState = listState,
          padding = hazedTopBarContentPadding(hazeState, innerPadding),
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
    Loading ->
      Column(
        modifier = Modifier.fillMaxSize().padding(padding),
        verticalArrangement = Arrangement.spacedBy(Dimens.ListItemSpacing),
      ) {
        repeat(ListTagsDS.SHIMMER_ITEMS) { ShimmerTagItem() }
      }

    is Failure ->
      FailureScreen(
        title = Strings.tagsFailurePrefix,
        reason = state.cause ?: Strings.tagsFailureDefaultMessage,
        background = colors.tableBackground,
        action =
          FailureAction(
            text = { Strings.syncRetry },
            icon = AktualIcons.Refresh,
            onClick = { onAction(Retry) },
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
            icon = AktualIcons.Add,
            onClick = { onAction(CreateTag) },
          ),
      )

    is Success ->
      TagsList(
        tags = state.tags,
        listState = listState,
        contentPadding = padding,
        onAction = onAction,
      )
  }
}

@Composable
private fun TagsList(
  tags: ImmutableList<TagItem>,
  listState: LazyListState,
  contentPadding: PaddingValues,
  onAction: ListTagsActionHandler,
  modifier: Modifier = Modifier,
) {
  // only one row may be swiped open at a time - opening another closes the previous one
  var openTagId by remember { mutableStateOf<TagId?>(null) }

  LazyColumn(
    modifier = modifier.fillMaxSize().scrollbar(listState),
    state = listState,
    contentPadding = contentPadding,
    verticalArrangement = Arrangement.spacedBy(Dimens.ListItemSpacing),
  ) {
    items(tags, key = { it.id.value }) { tag ->
      TagItem(
        modifier = Modifier.animateItem(),
        tag = tag,
        isOpen = openTagId == tag.id,
        onOpenChange = { open ->
          openTagId =
            when {
              open -> tag.id
              openTagId == tag.id -> null
              else -> openTagId
            }
        },
        onAction = onAction,
      )
    }

    item { BottomSpacing() }
  }
}

@Composable
private fun ListTagsMenu(onSort: () -> Unit, modifier: Modifier = Modifier) {
  var expanded by remember { mutableStateOf(false) }

  Box(modifier = modifier) {
    BareIconButton(
      imageVector = AktualIcons.DotsHorizontalTriple,
      contentDescription = Strings.tagsMenu,
      onClick = { expanded = true },
    )

    AktualDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
      ListTagsMenuItems(
        onSort = {
          expanded = false
          onSort()
        },
      )
    }
  }
}

@Composable
private fun ListTagsMenuItems(onSort: () -> Unit) {
  AktualDropdownMenuItem(text = Strings.sort, leadingIcon = MaterialIcons.Sort, onClick = onSort)
}

@Composable
private fun TagSort.Field.label(): String =
  when (this) {
    Name -> Strings.tagsSortFieldName
    Usage -> Strings.tagsSortFieldUsage
  }

private class ListTagsStateProvider :
  ColoredParameterProvider<ListTagsState>(
    Success(tags = TagsPreview.all, sort = Default),
    Empty,
    Loading,
    Failure("Database connection lost"),
  )

@Preview
@Composable
private fun PreviewListTagsMenuItems(@PreviewParameter(ColoredParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    Column(Modifier.width(IntrinsicSize.Max).background(colors.menuBackground)) {
      ListTagsMenuItems(onSort = {})
    }
  }

@PortraitPreview
@Composable
private fun PreviewListTagsScaffold(
  @PreviewParameter(ListTagsStateProvider::class) params: ColoredParams<ListTagsState>,
) = PreviewWithColoredParams(params) { ListTagsScaffold(state = this, onAction = {}) }
