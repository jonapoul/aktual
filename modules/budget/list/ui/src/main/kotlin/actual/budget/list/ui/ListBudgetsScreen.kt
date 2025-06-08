package actual.budget.list.ui

import actual.account.model.LoginToken
import actual.budget.list.res.Strings
import actual.budget.list.vm.ListBudgetsState
import actual.budget.list.vm.ListBudgetsViewModel
import actual.budget.model.Budget
import actual.core.icons.ActualIcons
import actual.core.icons.Refresh
import actual.core.model.ActualVersions
import actual.core.model.ServerUrl
import actual.core.ui.BasicIconButton
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewScreen
import actual.core.ui.ScreenPreview
import actual.core.ui.Theme
import actual.core.ui.UsingServerText
import actual.core.ui.VersionsText
import actual.core.ui.WavyBackground
import actual.core.ui.normalIconButton
import actual.core.ui.transparentTopAppBarColors
import alakazam.android.ui.compose.VerticalSpacer
import alakazam.kotlin.core.exhaustive
import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlinx.collections.immutable.persistentListOf

@Composable
fun ListBudgetsScreen(
  nav: ListBudgetsNavigator,
  token: LoginToken,
  viewModel: ListBudgetsViewModel = hiltViewModel(token),
) {
  val versions by viewModel.versions.collectAsStateWithLifecycle()
  val serverUrl by viewModel.serverUrl.collectAsStateWithLifecycle()
  val state by viewModel.state.collectAsStateWithLifecycle()

  var launchBrowser by remember { mutableStateOf(false) }
  val context = LocalContext.current
  LaunchedEffect(launchBrowser) {
    if (launchBrowser) {
      val intent = Intent(Intent.ACTION_VIEW)
      intent.data = serverUrl?.toString()?.toUri()
      context.startActivity(intent, null)
      launchBrowser = false
    }
  }

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
    versions = versions,
    state = state,
    url = serverUrl,
    onAction = { action ->
      when (action) {
        ListBudgetsAction.ChangeServer -> nav.toUrl()
        ListBudgetsAction.ChangePassword -> nav.toChangePassword()
        ListBudgetsAction.OpenSettings -> nav.toSettings()
        ListBudgetsAction.OpenInBrowser -> launchBrowser = true
        ListBudgetsAction.Reload -> viewModel.retry()
        is ListBudgetsAction.Delete -> budgetToDelete = action.budget
        is ListBudgetsAction.Open -> nav.toSyncBudget(token, action.budget.cloudFileId)
      }
    },
  )
}

@Composable
private fun hiltViewModel(token: LoginToken) = hiltViewModel<ListBudgetsViewModel, ListBudgetsViewModel.Factory>(
  creationCallback = { factory -> factory.create(token.value) },
)

@Composable
private fun ListBudgetsScaffold(
  versions: ActualVersions,
  state: ListBudgetsState,
  url: ServerUrl?,
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
        actions = { TopBarActions(state, onAction) },
      )
    },
  ) { innerPadding ->
    Box {
      val hazeState = remember { HazeState() }

      WavyBackground(
        modifier = Modifier.hazeSource(hazeState),
      )

      ListBudgetsContent(
        modifier = Modifier.padding(innerPadding),
        versions = versions,
        state = state,
        url = url,
        hazeState = hazeState,
        onAction = onAction,
        theme = theme,
      )
    }
  }
}

@Composable
private inline fun TopBarActions(
  state: ListBudgetsState,
  crossinline onAction: (ListBudgetsAction) -> Unit,
) {
  Row {
    if (state is ListBudgetsState.Success) {
      BasicIconButton(
        modifier = Modifier.padding(horizontal = 5.dp),
        onClick = { onAction(ListBudgetsAction.Reload) },
        imageVector = ActualIcons.Refresh,
        contentDescription = Strings.budgetFailureRetry,
        colors = { theme, isPressed -> theme.normalIconButton(isPressed) },
      )
    }

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
      val passwordText = Strings.listBudgetsChangePassword
      DropdownMenuItem(
        text = { Text(passwordText) },
        onClick = {
          showMenu = false
          onAction(ListBudgetsAction.ChangePassword)
        },
        leadingIcon = { Icon(Icons.Filled.Key, contentDescription = passwordText) },
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
private fun ListBudgetsContent(
  versions: ActualVersions,
  state: ListBudgetsState,
  url: ServerUrl?,
  hazeState: HazeState,
  onAction: (ListBudgetsAction) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Box(
      modifier = Modifier.weight(1f),
      contentAlignment = Alignment.Center,
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
      }.exhaustive
    }

    VerticalSpacer(20.dp)

    UsingServerText(
      hazeState = hazeState,
      url = url,
      onClickChange = { onAction(ListBudgetsAction.ChangeServer) },
    )

    VerticalSpacer()

    VersionsText(
      modifier = Modifier.align(Alignment.End),
      versions = versions,
    )
  }
}

@ScreenPreview
@Composable
private fun Success() = PreviewScreen {
  ListBudgetsScaffold(
    versions = PreviewVersions,
    url = ServerUrl.Demo,
    state = ListBudgetsState.Success(
      budgets = persistentListOf(PreviewBudgetSynced, PreviewBudgetSyncing, PreviewBudgetBroken),
    ),
    onAction = {},
  )
}

@ScreenPreview
@Composable
private fun Loading() = PreviewScreen {
  ListBudgetsScaffold(
    versions = PreviewVersions,
    url = ServerUrl.Demo,
    state = ListBudgetsState.Loading,
    onAction = {},
  )
}

@ScreenPreview
@Composable
private fun Failure() = PreviewScreen {
  ListBudgetsScaffold(
    versions = PreviewVersions,
    url = ServerUrl.Demo,
    state = ListBudgetsState.Failure(reason = "Something broke lol"),
    onAction = {},
  )
}
