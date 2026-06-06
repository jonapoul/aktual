package aktual.budget.list.ui

import aktual.budget.list.vm.ListBudgetsEvent
import aktual.budget.list.vm.ListBudgetsState
import aktual.budget.list.vm.ListBudgetsViewModel
import aktual.budget.model.Budget
import aktual.budget.model.BudgetId
import aktual.budget.model.directoryId
import aktual.budget.sync.ui.SyncBudgetDialog
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Refresh
import aktual.core.l10n.Strings
import aktual.core.nav.BudgetNavRailNavigator
import aktual.core.nav.ChangePasswordNavigator
import aktual.core.nav.InfoNavigator
import aktual.core.nav.MetricsNavigator
import aktual.core.nav.ServerUrlNavigator
import aktual.core.nav.SettingsNavigator
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.BlurredPullToRefreshBox
import aktual.core.ui.BottomSpacing
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.FailureAction
import aktual.core.ui.FailureScreen
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.WavyBackground
import aktual.core.ui.blurredTopBar
import aktual.core.ui.rememberBlurredTopBarState
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.collections.immutable.toImmutableList

@Composable
fun ListBudgetsScreen(
  toInfo: InfoNavigator,
  toChangePassword: ChangePasswordNavigator,
  toSettings: SettingsNavigator,
  toMetrics: MetricsNavigator,
  logOut: ServerUrlNavigator,
  toBudget: BudgetNavRailNavigator,
  viewModel: ListBudgetsViewModel = metroViewModel(),
) {
  val serverUrl by viewModel.serverUrl.collectAsStateWithLifecycle()
  val state by viewModel.state.collectAsStateWithLifecycle()

  LaunchedEffect(viewModel.event) {
    viewModel.event.collect { event ->
      when (event) {
        ListBudgetsEvent.NavToBudget -> toBudget()
        ListBudgetsEvent.LogOut -> logOut()
      }
    }
  }

  var budgetToDelete by remember { mutableStateOf<Budget?>(null) }
  budgetToDelete?.let { budget ->
    val deletingState by viewModel.deletingState.collectAsStateWithLifecycle()

    DeleteBudgetDialog(
      budget = budget,
      deletingState = deletingState,
      localFileExists = budget !is Budget.Remote,
      onAction = { action ->
        when (action) {
          DeleteLocal -> {
            viewModel.deleteLocal(budget.directoryId)
          }

          DeleteRemote -> {
            if (budget is Budget.Cloud) viewModel.deleteRemote(budget.cloudFileId)
          }

          Dismiss -> {
            budgetToDelete = null
            viewModel.clearDeletingState()
          }
        }
      },
    )
  }

  LaunchedEffect(Unit) {
    viewModel.closeDialog.collect { closeDialog ->
      if (closeDialog) {
        budgetToDelete = null
      }
    }
  }

  var budgetToSync by remember { mutableStateOf<BudgetId?>(null) }
  budgetToSync?.let { id ->
    SyncBudgetDialog(
      budgetId = id,
      onSyncComplete = { viewModel.onSyncComplete(id) },
      onDismissRequest = { budgetToSync = null },
    )
  }

  ListBudgetsScaffold(
    state = state,
    onAction = { action ->
      when (action) {
        LogOut -> viewModel.logOut()
        ChangePassword -> toChangePassword()
        OpenAbout -> toInfo()
        OpenSettings -> toSettings()
        OpenServerMetrics -> toMetrics()
        OpenInBrowser -> viewModel.open(serverUrl)
        Reload -> viewModel.retry()
        is Delete -> budgetToDelete = action.budget
        is Open -> budgetToSync = action.budget.directoryId
      }
    },
  )
}

@Composable
internal fun ListBudgetsScaffold(state: ListBudgetsState, onAction: ListBudgetsActionHandler) {
  val blurState = rememberBlurredTopBarState()
  val listState = rememberLazyListState()

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        modifier = Modifier.blurredTopBar(blurState, isScrolled = listState.canScrollBackward),
        colors = colors.transparentTopAppBarColors(),
        title = { ScaffoldTitle() },
        actions = { TopBarActions(onAction) },
      )
    },
  ) { innerPadding ->
    Box {
      WavyBackground()

      BlurredPullToRefreshBox(
        modifier = Modifier.padding(horizontal = 8.dp),
        onRefresh = { onAction(Reload) },
        isRefreshing = state is ListBudgetsState.Loading,
        contentAlignment = Alignment.Center,
        blurState = blurState,
        innerPadding = innerPadding,
      ) { padding ->
        StateContent(
          state = state,
          onAction = onAction,
          listState = listState,
          contentPadding = padding,
        )
      }
    }
  }
}

@Composable
private fun ScaffoldTitle() {
  Text(
    text = Strings.listBudgetsToolbar,
    maxLines = 1,
    overflow = TextOverflow.Ellipsis,
    color = colors.pageText,
  )
}

@Composable
private fun StateContent(
  state: ListBudgetsState,
  onAction: ListBudgetsActionHandler,
  listState: LazyListState,
  contentPadding: PaddingValues,
) {
  if (state is ListBudgetsState.Success && state.budgets.isNotEmpty()) {
    // Scroll-under path: LazyColumn fills the full PullToRefreshBox (which extends behind
    // the top bar), and applies contentPadding at the top so items rest below the bar but
    // can scroll up through it.
    ContentSuccess(
      budgets = state.budgets,
      listState = listState,
      contentPadding = contentPadding,
      onClickOpen = { budget -> onAction(Open(budget)) },
      onClickDelete = { budget -> onAction(Delete(budget)) },
    )
  } else {
    Column(
      modifier = Modifier.fillMaxSize().padding(contentPadding),
      verticalArrangement = Arrangement.Top,
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      when (state) {
        is ListBudgetsState.Loading -> {
          ShimmerBudgetList(state.numLoadingItems)
        }

        is ListBudgetsState.Failure -> {
          FailureScreen(
            title = Strings.budgetFailureMessage,
            reason = state.reason ?: Strings.budgetFailureDefaultMessage,
            background = colors.tableBackground,
            action =
              FailureAction(
                text = { Strings.budgetFailureRetry },
                onClick = { onAction(Reload) },
                icon = MaterialIcons.Refresh,
              ),
          )
        }

        is ListBudgetsState.Success -> {
          // Empty-budgets case
          ContentEmpty(onCreateBudgetInBrowser = { onAction(OpenInBrowser) })
        }
      }

      BottomSpacing()
    }
  }
}

@PortraitPreview
@Composable
private fun PreviewListBudgetsScaffold(
  @PreviewParameter(ListBudgetsScaffoldProvider::class) params: ColoredParams<ListBudgetsState>
) = PreviewWithColors(params.colors) { ListBudgetsScaffold(state = params.data, onAction = {}) }

private val PREVIEW_ITEMS =
  List(size = 5) { PreviewBudgetSynced } +
    List(size = 5) { PreviewBudgetRemote } +
    List(size = 5) { PreviewBudgetBroken }

private class ListBudgetsScaffoldProvider :
  ColoredParameterProvider<ListBudgetsState>(
    ListBudgetsState.Success(PREVIEW_ITEMS.toImmutableList()),
    ListBudgetsState.Loading(numLoadingItems = 3),
    ListBudgetsState.Failure(reason = "Something broke lol"),
  )
