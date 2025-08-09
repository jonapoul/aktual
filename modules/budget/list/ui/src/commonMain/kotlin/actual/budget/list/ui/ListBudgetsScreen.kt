@file:OptIn(ExperimentalMaterial3Api::class)

package actual.budget.list.ui

import actual.account.model.LoginToken
import actual.budget.list.vm.ListBudgetsState
import actual.budget.list.vm.ListBudgetsViewModel
import actual.budget.model.Budget
import actual.core.ui.BasicIconButton
import actual.core.ui.LocalTheme
import actual.core.ui.Theme
import actual.core.ui.WavyBackground
import actual.core.ui.assistedMetroViewModel
import actual.core.ui.normalIconButton
import actual.core.ui.transparentTopAppBarColors
import actual.l10n.Strings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.persistentListOf

@Composable
fun ListBudgetsScreen(
  nav: ListBudgetsNavigator,
  token: LoginToken,
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
          DeleteDialogAction.DeleteLocal -> viewModel.deleteLocal(budget.cloudFileId)
          DeleteDialogAction.DeleteRemote -> viewModel.deleteRemote(budget.cloudFileId)
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

  ListBudgetsScaffold(
    state = state,
    onAction = { action ->
      when (action) {
        ListBudgetsAction.ChangeServer -> nav.toUrl()
        ListBudgetsAction.ChangePassword -> nav.toChangePassword()
        ListBudgetsAction.OpenAbout -> nav.toAbout()
        ListBudgetsAction.OpenSettings -> nav.toSettings()
        ListBudgetsAction.OpenInBrowser -> viewModel.open(serverUrl)
        ListBudgetsAction.Reload -> viewModel.retry()
        is ListBudgetsAction.Delete -> budgetToDelete = action.budget
        is ListBudgetsAction.Open -> nav.toSyncBudget(token, action.budget.cloudFileId)
      }
    },
  )
}

@Composable
private fun metroViewModel(token: LoginToken) =
  assistedMetroViewModel<ListBudgetsViewModel, ListBudgetsViewModel.Factory> {
    create(token)
  }

@Composable
internal fun ListBudgetsScaffold(
  state: ListBudgetsState,
  onAction: (ListBudgetsAction) -> Unit,
) {
  val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
  val theme = LocalTheme.current

  Scaffold(
    modifier = Modifier
      .fillMaxSize()
      .nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
      TopAppBar(
        colors = theme.transparentTopAppBarColors(),
        title = { ScaffoldTitle(theme) },
        scrollBehavior = scrollBehavior,
        actions = { TopBarActions(onAction) },
      )
    },
  ) { innerPadding ->
    Box {
      WavyBackground()

      Content(
        modifier = Modifier.padding(innerPadding),
        state = state,
        onAction = onAction,
        theme = theme,
      )
    }
  }
}

@Composable
private inline fun TopBarActions(
  crossinline onAction: (ListBudgetsAction) -> Unit,
) {
  Row {
    BasicIconButton(
      modifier = Modifier.padding(horizontal = 5.dp),
      onClick = { onAction(ListBudgetsAction.OpenSettings) },
      imageVector = Icons.Filled.Settings,
      contentDescription = Strings.listBudgetsSettings,
      colors = { theme, isPressed -> theme.normalIconButton(isPressed) },
    )

    var showMenu by remember { mutableStateOf(false) }
    BasicIconButton(
      modifier = Modifier.padding(horizontal = 5.dp),
      onClick = { showMenu = !showMenu },
      imageVector = Icons.Filled.MoreVert,
      contentDescription = Strings.listBudgetsMenu,
      colors = { theme, isPressed -> theme.normalIconButton(isPressed) },
    )

    DropdownMenu(
      expanded = showMenu,
      onDismissRequest = { showMenu = false },
    ) {
      val serverText = Strings.listBudgetsChangeServer
      DropdownMenuItem(
        text = { Text(serverText) },
        onClick = {
          showMenu = false
          onAction(ListBudgetsAction.ChangeServer)
        },
        leadingIcon = { Icon(Icons.Filled.Cloud, contentDescription = serverText) },
      )

      val passwordText = Strings.listBudgetsChangePassword
      DropdownMenuItem(
        text = { Text(passwordText) },
        onClick = {
          showMenu = false
          onAction(ListBudgetsAction.ChangePassword)
        },
        leadingIcon = { Icon(Icons.Filled.Key, contentDescription = passwordText) },
      )

      val aboutText = Strings.listBudgetsAbout
      DropdownMenuItem(
        text = { Text(aboutText) },
        onClick = {
          showMenu = false
          onAction(ListBudgetsAction.OpenAbout)
        },
        leadingIcon = { Icon(Icons.Filled.Info, contentDescription = aboutText) },
      )
    }
  }
}

@Stable
@Composable
private fun ScaffoldTitle(theme: Theme) = Text(
  text = Strings.listBudgetsToolbar,
  maxLines = 1,
  overflow = TextOverflow.Ellipsis,
  color = theme.pageText,
)

@Stable
@Composable
private fun Content(
  state: ListBudgetsState,
  onAction: (ListBudgetsAction) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  PullToRefreshBox(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp),
    contentAlignment = Alignment.Center,
    onRefresh = { onAction(ListBudgetsAction.Reload) },
    isRefreshing = state is ListBudgetsState.Loading,
  ) {
    StateContent(state, onAction, theme)
  }
}

@Composable
private fun StateContent(
  state: ListBudgetsState,
  onAction: (ListBudgetsAction) -> Unit,
  theme: Theme = LocalTheme.current,
) {
  when (state) {
    is ListBudgetsState.Loading -> {
      ContentLoading(
        modifier = Modifier.fillMaxSize(),
        theme = theme,
      )
    }

    is ListBudgetsState.Failure -> {
      ContentFailure(
        modifier = Modifier.fillMaxSize(),
        reason = state.reason,
        onClickRetry = { onAction(ListBudgetsAction.Reload) },
        theme = theme,
      )
    }

    is ListBudgetsState.Success -> {
      if (state.budgets.isEmpty()) {
        ContentEmpty(
          modifier = Modifier.fillMaxSize(),
          theme = theme,
          onCreateBudgetInBrowser = { onAction(ListBudgetsAction.OpenInBrowser) },
        )
      } else {
        ContentSuccess(
          modifier = Modifier.fillMaxSize(),
          budgets = state.budgets,
          theme = theme,
          onClickOpen = { budget -> onAction(ListBudgetsAction.Open(budget)) },
          onClickDelete = { budget -> onAction(ListBudgetsAction.Delete(budget)) },
        )
      }
    }
  }
}
