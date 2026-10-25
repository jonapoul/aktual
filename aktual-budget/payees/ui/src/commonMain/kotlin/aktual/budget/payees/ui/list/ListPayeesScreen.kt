package aktual.budget.payees.ui.list

import aktual.budget.payees.vm.list.Empty
import aktual.budget.payees.vm.list.Failure
import aktual.budget.payees.vm.list.ListPayeesState
import aktual.budget.payees.vm.list.ListPayeesViewModel
import aktual.budget.payees.vm.list.Loading
import aktual.budget.payees.vm.list.PayeeItem
import aktual.budget.payees.vm.list.Success
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Refresh
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.BottomSpacing
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.FailureAction
import aktual.core.ui.FailureScreen
import aktual.core.ui.HazedPullToRefreshBox
import aktual.core.ui.NavDrawerIconButton
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.hazedTopBar
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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun ListPayeesScreen(
  modifier: Modifier = Modifier,
  viewModel: ListPayeesViewModel = metroViewModel(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

  // refresh on return so the list reflects changes made elsewhere
  @Suppress("ComposeViewModelForwarding")
  LifecycleResumeEffect(viewModel) {
    viewModel.reload(showLoading = false)
    onPauseOrDispose {}
  }

  ListPayeesScaffold(
    modifier = modifier,
    state = state,
    isRefreshing = isRefreshing,
    onAction = { action ->
      when (action) {
        Reload -> viewModel.reload()
        Refresh -> viewModel.refresh()
      }
    },
  )
}

@Composable
private fun ListPayeesScaffold(
  state: ListPayeesState,
  onAction: ListPayeesActionHandler,
  modifier: Modifier = Modifier,
  isRefreshing: Boolean = false,
) {
  val hazeState = rememberHazedTopBarState()
  val listState = rememberLazyListState()

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        modifier = Modifier.hazedTopBar(hazeState, listState),
        colors = colors.transparentTopAppBarColors(),
        navigationIcon = { NavDrawerIconButton() },
        title = { Text(text = Strings.payeesTitle) },
      )
    },
  ) { innerPadding ->
    Box(modifier = Modifier.fillMaxSize()) {
      PageBackground()

      HazedPullToRefreshBox(
        modifier = Modifier.padding(ListPayeesDS.listPadding),
        contentAlignment = Center,
        onRefresh = { onAction(Refresh) },
        isRefreshing = isRefreshing,
        hazeState = hazeState,
        innerPadding = innerPadding,
      ) { padding ->
        ListPayeesContent(
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
private fun ListPayeesContent(
  state: ListPayeesState,
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
            onClick = { onAction(Reload) },
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

    is Success -> PayeesList(payees = state.payees, listState = listState, contentPadding = padding)
  }
}

@Composable
private fun PayeesList(
  payees: ImmutableList<PayeeItem>,
  listState: LazyListState,
  contentPadding: PaddingValues,
  modifier: Modifier = Modifier,
) {
  LazyColumn(
    modifier = modifier.fillMaxSize().scrollbar(listState),
    state = listState,
    contentPadding = contentPadding,
    verticalArrangement = Arrangement.spacedBy(ListPayeesDS.listItemSpacing),
  ) {
    items(payees, key = { it.id.value }) { payee ->
      PayeeItem(modifier = Modifier.animateItem(), payee = payee)
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
) = PreviewWithColoredParams(params) { ListPayeesScaffold(state = this, onAction = {}) }
