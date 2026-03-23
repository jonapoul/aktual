package aktual.budget.list.ui

import aktual.budget.list.ui.ListBudgetsAction.ChangePassword
import aktual.budget.list.ui.ListBudgetsAction.ChangeServer
import aktual.budget.list.ui.ListBudgetsAction.Delete
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
import aktual.core.ui.BlurredTopBarSpacing
import aktual.core.ui.BottomNavBarSpacing
import aktual.core.ui.BottomStatusBarSpacing
import aktual.core.ui.FailureScreen
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColorScheme
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import aktual.core.ui.WavyBackground
import aktual.core.ui.blurredTopBar
import aktual.core.ui.blurredTopBarContent
import aktual.core.ui.rememberBlurredTopBarState
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import kotlinx.collections.immutable.persistentListOf

@Composable
fun ListBudgetsScreen(
  nav: ListBudgetsNavigator,
  token: Token,
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
      onSyncComplete = { nav.toBudget(token, id) },
      onDismissRequest = { budgetToSync = null },
    )
  }

  ListBudgetsScaffold(
    state = state,
    onAction = { action ->
      when (action) {
        ChangeServer -> nav.toUrl()
        ChangePassword -> nav.toChangePassword()
        OpenAbout -> nav.toAbout()
        OpenSettings -> nav.toSettings()
        OpenServerMetrics -> nav.toMetrics()
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

      Column(modifier = Modifier.blurredTopBarContent(blurState, innerPadding)) {
        BlurredTopBarSpacing(blurState, innerPadding)
        PullToRefreshBox(
          modifier = Modifier.padding(8.dp),
          contentAlignment = Alignment.Center,
          onRefresh = { onAction(Reload) },
          isRefreshing = state is ListBudgetsState.Loading,
          content = { StateContent(state, onAction, listState, theme) },
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
  theme: Theme = LocalTheme.current,
) {
  Column(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.Top,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    when (state) {
      is ListBudgetsState.Loading -> {
        ShimmerBudgetList(theme = theme)
      }

      is ListBudgetsState.Failure -> {
        FailureScreen(
          title = Strings.budgetFailureMessage,
          reason = state.reason ?: Strings.budgetFailureDefaultMessage,
          retryText = Strings.budgetFailureRetry,
          onClickRetry = { onAction(Reload) },
          theme = theme,
        )
      }

      is ListBudgetsState.Success -> {
        if (state.budgets.isEmpty()) {
          ContentEmpty(theme = theme, onCreateBudgetInBrowser = { onAction(OpenInBrowser) })
        } else {
          ContentSuccess(
            budgets = state.budgets,
            listState = listState,
            theme = theme,
            onClickOpen = { budget -> onAction(Open(budget)) },
            onClickDelete = { budget -> onAction(Delete(budget)) },
          )
        }
      }
    }

    BottomStatusBarSpacing()
    BottomNavBarSpacing()
  }
}

@PortraitPreview
@Composable
private fun PreviewListBudgetsScaffold(
  @PreviewParameter(ListBudgetsScaffoldProvider::class) params: ThemedParams<ListBudgetsState>
) = PreviewWithColorScheme(params.theme) { ListBudgetsScaffold(state = params.data, onAction = {}) }

private class ListBudgetsScaffoldProvider :
  ThemedParameterProvider<ListBudgetsState>(
    ListBudgetsState.Success(
      persistentListOf(PreviewBudgetSynced, PreviewBudgetSyncing, PreviewBudgetBroken)
    ),
    ListBudgetsState.Loading,
    ListBudgetsState.Failure(reason = "Something broke lol"),
  )
