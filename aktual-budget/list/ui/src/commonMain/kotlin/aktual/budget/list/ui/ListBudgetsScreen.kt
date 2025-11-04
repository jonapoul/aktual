/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.budget.list.ui

import aktual.budget.list.ui.ListBudgetsAction.ChangePassword
import aktual.budget.list.ui.ListBudgetsAction.ChangeServer
import aktual.budget.list.ui.ListBudgetsAction.Delete
import aktual.budget.list.ui.ListBudgetsAction.Open
import aktual.budget.list.ui.ListBudgetsAction.OpenAbout
import aktual.budget.list.ui.ListBudgetsAction.OpenInBrowser
import aktual.budget.list.ui.ListBudgetsAction.OpenSettings
import aktual.budget.list.ui.ListBudgetsAction.Reload
import aktual.budget.list.vm.ListBudgetsState
import aktual.budget.list.vm.ListBudgetsViewModel
import aktual.budget.model.Budget
import aktual.core.model.LoginToken
import aktual.core.ui.BasicIconButton
import aktual.core.ui.LocalTheme
import aktual.core.ui.Theme
import aktual.core.ui.WavyBackground
import aktual.core.ui.assistedMetroViewModel
import aktual.core.ui.normalIconButton
import aktual.core.ui.transparentTopAppBarColors
import aktual.l10n.Strings
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
        ChangeServer -> nav.toUrl()
        ChangePassword -> nav.toChangePassword()
        OpenAbout -> nav.toAbout()
        OpenSettings -> nav.toSettings()
        OpenInBrowser -> viewModel.open(serverUrl)
        Reload -> viewModel.retry()
        is Delete -> budgetToDelete = action.budget
        is Open -> nav.toSyncBudget(token, action.budget.cloudFileId)
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
private fun ScaffoldTitle(theme: Theme) = Text(
  text = Strings.listBudgetsToolbar,
  maxLines = 1,
  overflow = TextOverflow.Ellipsis,
  color = theme.pageText,
)

@Composable
private fun Content(
  state: ListBudgetsState,
  onAction: (ListBudgetsAction) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) = PullToRefreshBox(
  modifier = modifier
    .fillMaxSize()
    .padding(16.dp),
  contentAlignment = Alignment.Center,
  onRefresh = { onAction(Reload) },
  isRefreshing = state is ListBudgetsState.Loading,
  content = { StateContent(state, onAction, theme) },
)

@Composable
private fun StateContent(
  state: ListBudgetsState,
  onAction: (ListBudgetsAction) -> Unit,
  theme: Theme = LocalTheme.current,
) = when (state) {
  is ListBudgetsState.Loading -> {
    // Empty content - we've already got the pull-to-refresh indicator
    Box(
      modifier = Modifier.fillMaxSize(),
    )
  }

  is ListBudgetsState.Failure -> {
    ContentFailure(
      modifier = Modifier.fillMaxSize(),
      reason = state.reason,
      onClickRetry = { onAction(Reload) },
      theme = theme,
    )
  }

  is ListBudgetsState.Success -> {
    if (state.budgets.isEmpty()) {
      ContentEmpty(
        modifier = Modifier.fillMaxSize(),
        theme = theme,
        onCreateBudgetInBrowser = { onAction(OpenInBrowser) },
      )
    } else {
      ContentSuccess(
        modifier = Modifier.fillMaxSize(),
        budgets = state.budgets,
        theme = theme,
        onClickOpen = { budget -> onAction(Open(budget)) },
        onClickDelete = { budget -> onAction(Delete(budget)) },
      )
    }
  }
}
