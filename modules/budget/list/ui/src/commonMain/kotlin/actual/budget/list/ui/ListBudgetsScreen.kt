/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package actual.budget.list.ui

import actual.budget.list.ui.ListBudgetsAction.ChangePassword
import actual.budget.list.ui.ListBudgetsAction.ChangeServer
import actual.budget.list.ui.ListBudgetsAction.Delete
import actual.budget.list.ui.ListBudgetsAction.Open
import actual.budget.list.ui.ListBudgetsAction.OpenAbout
import actual.budget.list.ui.ListBudgetsAction.OpenInBrowser
import actual.budget.list.ui.ListBudgetsAction.OpenSettings
import actual.budget.list.ui.ListBudgetsAction.Reload
import actual.budget.list.vm.ListBudgetsState
import actual.budget.list.vm.ListBudgetsViewModel
import actual.budget.model.Budget
import actual.core.model.LoginToken
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
private inline fun TopBarActions(
  crossinline onAction: (ListBudgetsAction) -> Unit,
) = Row {
  BasicIconButton(
    modifier = Modifier.padding(horizontal = 5.dp),
    onClick = { onAction(OpenSettings) },
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
        onAction(ChangeServer)
      },
      leadingIcon = { Icon(Icons.Filled.Cloud, contentDescription = serverText) },
    )

    val passwordText = Strings.listBudgetsChangePassword
    DropdownMenuItem(
      text = { Text(passwordText) },
      onClick = {
        showMenu = false
        onAction(ChangePassword)
      },
      leadingIcon = { Icon(Icons.Filled.Key, contentDescription = passwordText) },
    )

    val aboutText = Strings.listBudgetsAbout
    DropdownMenuItem(
      text = { Text(aboutText) },
      onClick = {
        showMenu = false
        onAction(OpenAbout)
      },
      leadingIcon = { Icon(Icons.Filled.Info, contentDescription = aboutText) },
    )
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
