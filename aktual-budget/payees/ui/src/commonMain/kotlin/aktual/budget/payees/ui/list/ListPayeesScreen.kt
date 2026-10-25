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
import aktual.core.icons.material.Search
import aktual.core.l10n.Strings
import aktual.core.nav.SearchPayeesNavigator
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.BareIconButton
import aktual.core.ui.BottomSpacing
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.FailureAction
import aktual.core.ui.FailureScreen
import aktual.core.ui.NavDrawerIconButton
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColoredParams
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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun ListPayeesScreen(
  toSearch: SearchPayeesNavigator,
  modifier: Modifier = Modifier,
  viewModel: ListPayeesViewModel = metroViewModel(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()

  ListPayeesScaffold(
    modifier = modifier,
    state = state,
    onAction = { action ->
      when (action) {
        Retry -> viewModel.retry()
        OpenSearch -> toSearch()
      }
    },
  )
}

@Composable
private fun ListPayeesScaffold(
  state: ListPayeesState,
  onAction: ListPayeesActionHandler,
  modifier: Modifier = Modifier,
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
          onAction = onAction,
          listState = listState,
          padding = hazedTopBarContentPadding(hazeState, innerPadding),
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
