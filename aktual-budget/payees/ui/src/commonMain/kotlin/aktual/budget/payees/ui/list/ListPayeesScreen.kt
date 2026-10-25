package aktual.budget.payees.ui.list

import aktual.budget.model.PayeeId
import aktual.budget.payees.ui.openRules
import aktual.budget.payees.vm.list.Empty
import aktual.budget.payees.vm.list.Failure
import aktual.budget.payees.vm.list.ListPayeesState
import aktual.budget.payees.vm.list.ListPayeesViewModel
import aktual.budget.payees.vm.list.Loading
import aktual.budget.payees.vm.list.PayeeItem
import aktual.budget.payees.vm.list.Success
import aktual.core.icons.material.Bookmark
import aktual.core.icons.material.Delete
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Refresh
import aktual.core.icons.material.Search
import aktual.core.icons.material.SelectAll
import aktual.core.l10n.Plurals
import aktual.core.l10n.Strings
import aktual.core.nav.EditPayeeNavigator
import aktual.core.nav.EditRuleNavigator
import aktual.core.nav.SearchPayeesNavigator
import aktual.core.nav.SearchRulesNavigator
import aktual.core.theme.Colors
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.BackHandler
import aktual.core.ui.BareIconButton
import aktual.core.ui.BottomSpacing
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParameters
import aktual.core.ui.ColoredParams
import aktual.core.ui.FailureAction
import aktual.core.ui.FailureScreen
import aktual.core.ui.LocalSnackbars
import aktual.core.ui.NavDrawerIconButton
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.SelectionTopAppBar
import aktual.core.ui.hazedTopBar
import aktual.core.ui.hazedTopBarContent
import aktual.core.ui.hazedTopBarContentPadding
import aktual.core.ui.rememberHazedTopBarState
import aktual.core.ui.scrollbar
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf

@Composable
internal fun ListPayeesScreen(
  toSearch: SearchPayeesNavigator,
  toEdit: EditPayeeNavigator,
  editRule: EditRuleNavigator,
  searchRules: SearchRulesNavigator,
  modifier: Modifier = Modifier,
  viewModel: ListPayeesViewModel = metroViewModel(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val selected by viewModel.selected.collectAsStateWithLifecycle()
  val snackbars = LocalSnackbars.current

  LaunchedEffect(viewModel) {
    viewModel.events.collect { event ->
      when (event) {
        is Deleted -> snackbars.show { showDeleted(event, onUndo = viewModel::undoDelete) }
        DeleteFailed,
        RestoreFailed,
        UpdateFailed -> snackbars.show { showFailure(event) }
      }
    }
  }

  ListPayeesScaffold(
    modifier = modifier,
    state = state,
    selected = selected,
    onAction = { action ->
      when (action) {
        Retry -> viewModel.retry()
        OpenSearch -> toSearch()
        is OpenPayee -> toEdit(action.id)
        is OpenRules -> openRules(action.payee, editRule, searchRules)
        is ToggleSelected -> viewModel.toggleSelected(action.id)
        SelectAll -> viewModel.selectAll()
        ClearSelection -> viewModel.clearSelection()
        DeleteSelected -> viewModel.deleteSelected()
        ToggleFavoriteSelected -> viewModel.toggleFavoriteSelected()
      }
    },
  )
}

@Composable
private fun ListPayeesScaffold(
  state: ListPayeesState,
  selected: ImmutableSet<PayeeId>,
  onAction: ListPayeesActionHandler,
  modifier: Modifier = Modifier,
) {
  val hazeState = rememberHazedTopBarState()
  val listState = rememberLazyListState()
  var showDeleteDialog by remember { mutableStateOf(false) }
  val payees = (state as? Success)?.payees.orEmpty()

  BackHandler(enabled = selected.isNotEmpty()) { onAction(ClearSelection) }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      if (selected.isEmpty()) {
        TopAppBar(
          modifier = Modifier.hazedTopBar(hazeState, listState),
          colors = colors.transparentTopAppBarColors(),
          navigationIcon = { NavDrawerIconButton() },
          title = { Text(text = Strings.payeesTitle) },
          actions = {
            if (state is Success) {
              BareIconButton(
                imageVector = MaterialIcons.Search,
                contentDescription = Strings.payeesSearch,
                onClick = { onAction(OpenSearch) },
              )
            }
          },
        )
      } else {
        val allFavorites = payees.filter { it.id in selected }.all { it.isFavorite }
        SelectionTopAppBar(
          modifier = Modifier.hazedTopBar(hazeState, listState),
          numSelected = selected.size,
          onClear = { onAction(ClearSelection) },
        ) {
          BareIconButton(
            imageVector = MaterialIcons.SelectAll,
            contentDescription = Strings.payeesSelectAll,
            onClick = { onAction(SelectAll) },
          )
          BareIconButton(
            imageVector = MaterialIcons.Bookmark,
            contentDescription =
              if (allFavorites) Strings.payeesUnfavorite else Strings.payeesFavorite,
            onClick = { onAction(ToggleFavoriteSelected) },
          )
          BareIconButton(
            imageVector = MaterialIcons.Delete,
            contentDescription = Strings.payeesDelete,
            onClick = {
              // Only worth confirming when transactions will lose their payee
              val isUsed = payees.any { it.id in selected && it.numTransactions > 0 }
              if (isUsed) showDeleteDialog = true else onAction(DeleteSelected)
            },
          )
        }
      }
    },
  ) { innerPadding ->
    Box(modifier = Modifier.fillMaxSize()) {
      PageBackground()

      Box(
        modifier =
          Modifier.padding(ListPayeesDS.listPadding).hazedTopBarContent(hazeState, innerPadding),
        contentAlignment = Center,
      ) {
        ListPayeesContent(
          state = state,
          selected = selected,
          onAction = onAction,
          listState = listState,
          padding = hazedTopBarContentPadding(hazeState, innerPadding),
        )
      }

      if (showDeleteDialog) {
        DeletePayeesDialog(
          count = selected.size,
          onConfirm = {
            showDeleteDialog = false
            onAction(DeleteSelected)
          },
          onCancel = { showDeleteDialog = false },
        )
      }
    }
  }
}

@Composable
private fun DeletePayeesDialog(count: Int, onConfirm: () -> Unit, onCancel: () -> Unit) {
  AktualAlertDialog(
    title = Plurals.payeesDeleteConfirmTitle(count, count),
    highlight = colors.errorText,
    onDismissRequest = onCancel,
    buttons = {
      TextButton(onClick = onCancel) { Text(Strings.payeesDeleteCancel) }
      TextButton(onClick = onConfirm) {
        Text(Strings.payeesDeleteConfirm, color = colors.errorText)
      }
    },
    content = { Text(Strings.payeesDeleteConfirmMessage) },
  )
}

@Composable
private fun ListPayeesContent(
  state: ListPayeesState,
  selected: ImmutableSet<PayeeId>,
  onAction: ListPayeesActionHandler,
  listState: LazyListState,
  padding: PaddingValues,
) {
  when (state) {
    Loading ->
      Column(
        modifier = Modifier.fillMaxSize().padding(padding),
        verticalArrangement = Arrangement.spacedBy(ListPayeesDS.listItemSpacing),
      ) {
        repeat(ListPayeesDS.SHIMMER_ITEMS) { ShimmerPayeeItem() }
      }

    is Failure ->
      FailureScreen(
        title = Strings.payeesFailurePrefix,
        reason = state.cause ?: Strings.payeesFailureDefaultMessage,
        background = colors.tableBackground,
        action =
          FailureAction(
            text = { Strings.syncRetry },
            icon = MaterialIcons.Refresh,
            onClick = { onAction(Retry) },
          ),
      )

    Empty ->
      FailureScreen(
        title = Strings.payeesEmpty,
        reason = null,
        action = null,
        icon = null,
        background = colors.tableBackground,
      )

    is Success ->
      PayeesList(
        payees = state.payees,
        selected = selected,
        listState = listState,
        contentPadding = padding,
        onAction = onAction,
      )
  }
}

@Composable
private fun PayeesList(
  payees: ImmutableList<PayeeItem>,
  selected: ImmutableSet<PayeeId>,
  listState: LazyListState,
  contentPadding: PaddingValues,
  onAction: ListPayeesActionHandler,
  modifier: Modifier = Modifier,
) {
  LazyColumn(
    modifier = modifier.fillMaxSize().scrollbar(listState),
    state = listState,
    contentPadding = contentPadding,
    verticalArrangement = Arrangement.spacedBy(ListPayeesDS.listItemSpacing),
  ) {
    items(payees, key = { it.id.value }) { payee ->
      val isSelecting = selected.isNotEmpty()
      PayeeItem(
        modifier = Modifier.animateItem(),
        payee = payee,
        isSelected = if (isSelecting) payee.id in selected else null,
        onClick = {
          onAction(if (isSelecting) ToggleSelected(payee.id) else OpenPayee(payee.id))
        },
        onLongClick = { onAction(ToggleSelected(payee.id)) },
        onClickRules = { onAction(OpenRules(payee)) },
      )
    }

    item { BottomSpacing() }
  }
}

private class ListPayeesStateProvider :
  ColoredParameterProvider<ListPayeesState>(
    Success(PayeesPreview.all),
    Empty,
    Loading,
    Failure("Database connection lost"),
  )

@PortraitPreview
@Composable
private fun PreviewListPayeesScaffold(
  @PreviewParameter(ListPayeesStateProvider::class) params: ColoredParams<ListPayeesState>,
) =
  PreviewWithColoredParams(params) {
    ListPayeesScaffold(state = this, selected = persistentSetOf(), onAction = {})
  }

@PortraitPreview
@Composable
private fun PreviewListPayeesSelecting(@PreviewParameter(ColoredParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    ListPayeesScaffold(
      state = Success(PayeesPreview.all),
      selected = persistentSetOf(PayeesPreview.tesco.id, PayeesPreview.argos.id),
      onAction = {},
    )
  }
