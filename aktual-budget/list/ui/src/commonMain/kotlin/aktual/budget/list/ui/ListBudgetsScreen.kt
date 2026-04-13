package aktual.budget.list.ui

import aktual.app.nav.BudgetNavRailNavigator
import aktual.app.nav.ChangePasswordNavigator
import aktual.app.nav.InfoNavigator
import aktual.app.nav.MetricsNavigator
import aktual.app.nav.ServerUrlNavigator
import aktual.app.nav.SettingsNavigator
import aktual.budget.list.ui.ListBudgetsAction.ChangePassword
import aktual.budget.list.ui.ListBudgetsAction.Delete
import aktual.budget.list.ui.ListBudgetsAction.LogOut
import aktual.budget.list.ui.ListBudgetsAction.Open
import aktual.budget.list.ui.ListBudgetsAction.OpenAbout
import aktual.budget.list.ui.ListBudgetsAction.OpenInBrowser
import aktual.budget.list.ui.ListBudgetsAction.OpenServerMetrics
import aktual.budget.list.ui.ListBudgetsAction.OpenSettings
import aktual.budget.list.ui.ListBudgetsAction.Reload
import aktual.budget.list.vm.ListBudgetsState
import aktual.budget.list.vm.ListBudgetsViewModel
import aktual.budget.model.Budget
import aktual.budget.model.BudgetId
import aktual.budget.sync.ui.SyncBudgetDialog
import aktual.core.l10n.Strings
import aktual.core.model.Token
import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import aktual.core.ui.BlurredPullToRefreshBox
import aktual.core.ui.BottomNavBarSpacing
import aktual.core.ui.BottomStatusBarSpacing
import aktual.core.ui.CardShape
import aktual.core.ui.FailureScreen
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithTheme
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import aktual.core.ui.WavyBackground
import aktual.core.ui.blurredTopBar
import aktual.core.ui.rememberBlurredTopBarState
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.foundation.background
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
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import kotlinx.collections.immutable.toImmutableList

@Composable
fun ListBudgetsScreen(
  token: Token,
  toInfo: InfoNavigator,
  toChangePassword: ChangePasswordNavigator,
  toSettings: SettingsNavigator,
  toMetrics: MetricsNavigator,
  logOut: ServerUrlNavigator,
  toBudget: BudgetNavRailNavigator,
  viewModel: ListBudgetsViewModel = metroViewModel(token),
) {
  val serverUrl by viewModel.serverUrl.collectAsStateWithLifecycle()
  val state by viewModel.state.collectAsStateWithLifecycle()

  var budgetToDelete by remember { mutableStateOf<Budget?>(null) }
  budgetToDelete?.let { budget ->
    val deletingState by viewModel.deletingState.collectAsStateWithLifecycle()
    val localFilesExist by viewModel.localFilesExist.collectAsStateWithLifecycle()
    val thisFileExists = localFilesExist.getOrElse(budget.cloudFileId) { false }

    DeleteBudgetDialog(
      budget = budget,
      deletingState = deletingState,
      localFileExists = thisFileExists,
      onAction = { action ->
        when (action) {
          DeleteDialogAction.DeleteLocal -> {
            viewModel.deleteLocal(budget.cloudFileId)
          }

          DeleteDialogAction.DeleteRemote -> {
            viewModel.deleteRemote(budget.cloudFileId)
          }

          DeleteDialogAction.Dismiss -> {
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
      token = token,
      onSyncComplete = { toBudget(token, id) },
      onDismissRequest = { budgetToSync = null },
    )
  }

  ListBudgetsScaffold(
    state = state,
    onAction = { action ->
      when (action) {
        LogOut -> logOut()
        ChangePassword -> toChangePassword()
        OpenAbout -> toInfo()
        OpenSettings -> toSettings()
        OpenServerMetrics -> toMetrics()
        OpenInBrowser -> viewModel.open(serverUrl)
        Reload -> viewModel.retry()
        is Delete -> budgetToDelete = action.budget
        is Open -> budgetToSync = action.budget.cloudFileId
      }
    },
  )
}

@Composable
private fun metroViewModel(token: Token) =
  assistedMetroViewModel<ListBudgetsViewModel, ListBudgetsViewModel.Factory> { create(token) }

@Composable
internal fun ListBudgetsScaffold(state: ListBudgetsState, onAction: (ListBudgetsAction) -> Unit) {
  val theme = LocalTheme.current
  val blurState = rememberBlurredTopBarState()
  val listState = rememberLazyListState()

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        modifier = Modifier.blurredTopBar(blurState, isScrolled = listState.canScrollBackward),
        colors = theme.transparentTopAppBarColors(),
        title = { ScaffoldTitle(theme) },
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
private fun ScaffoldTitle(theme: Theme) =
  Text(
    text = Strings.listBudgetsToolbar,
    maxLines = 1,
    overflow = TextOverflow.Ellipsis,
    color = theme.pageText,
  )

@Composable
private fun StateContent(
  state: ListBudgetsState,
  onAction: (ListBudgetsAction) -> Unit,
  listState: LazyListState,
  contentPadding: PaddingValues,
  theme: Theme = LocalTheme.current,
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
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            FailureScreen(
              modifier = Modifier.background(theme.tableBackground, CardShape),
              title = Strings.budgetFailureMessage,
              reason = state.reason ?: Strings.budgetFailureDefaultMessage,
              retryText = Strings.budgetFailureRetry,
              onClickRetry = { onAction(Reload) },
            )
          }
        }

        is ListBudgetsState.Success -> {
          // Empty-budgets case
          ContentEmpty(onCreateBudgetInBrowser = { onAction(OpenInBrowser) })
        }
      }

      BottomStatusBarSpacing()
      BottomNavBarSpacing()
    }
  }
}

@PortraitPreview
@Composable
private fun PreviewListBudgetsScaffold(
  @PreviewParameter(ListBudgetsScaffoldProvider::class) params: ThemedParams<ListBudgetsState>
) = PreviewWithTheme(params.theme) { ListBudgetsScaffold(state = params.data, onAction = {}) }

private val PREVIEW_ITEMS =
  List(size = 5) { PreviewBudgetSynced } +
    List(size = 5) { PreviewBudgetSyncing } +
    List(size = 5) { PreviewBudgetBroken }

private class ListBudgetsScaffoldProvider :
  ThemedParameterProvider<ListBudgetsState>(
    ListBudgetsState.Success(PREVIEW_ITEMS.toImmutableList()),
    ListBudgetsState.Loading(numLoadingItems = 3),
    ListBudgetsState.Failure(reason = "Something broke lol"),
  )
