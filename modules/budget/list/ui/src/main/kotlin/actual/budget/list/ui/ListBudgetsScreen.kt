package actual.budget.list.ui

import actual.budget.list.res.BudgetListStrings
import actual.budget.list.vm.Budget
import actual.budget.list.vm.ListBudgetsState
import actual.budget.list.vm.ListBudgetsViewModel
import actual.core.ui.ActualScreenPreview
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewActualScreen
import actual.core.ui.Theme
import actual.core.ui.UsingServerText
import actual.core.ui.VersionsText
import actual.core.ui.VerticalSpacer
import actual.core.versions.ActualVersions
import actual.url.model.ServerUrl
import alakazam.kotlin.core.exhaustive
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import kotlinx.collections.immutable.persistentListOf

// TODO: Remove suppression
@Suppress("UNUSED_PARAMETER")
@Composable
fun ListBudgetsScreen(
  navController: NavHostController,
  viewModel: ListBudgetsViewModel = hiltViewModel(),
) {
  val versions by viewModel.versions.collectAsStateWithLifecycle()
  val serverUrl by viewModel.serverUrl.collectAsStateWithLifecycle()
  val state by viewModel.state.collectAsStateWithLifecycle()

  DisposableEffect(Unit) {
    onDispose {
      viewModel.clearState()
    }
  }

  var launchBrowser by remember { mutableStateOf(false) }
  val context = LocalContext.current
  LaunchedEffect(launchBrowser) {
    if (launchBrowser) {
      val intent = Intent(Intent.ACTION_VIEW)
      intent.data = Uri.parse(serverUrl.toString())
      context.startActivity(intent, null)
      launchBrowser = false
    }
  }

  var budgetToDelete by remember { mutableStateOf<Budget?>(null) }
  budgetToDelete?.let { budget ->
    DeleteBudgetDialog(
      budget = budget,
      onAction = { action ->
        when (action) {
          DeleteDialogAction.DeleteLocal -> TODO()
          DeleteDialogAction.DeleteRemote -> TODO()
          DeleteDialogAction.Dismiss -> budgetToDelete = null
        }
      },
    )
  }

  ListBudgetsScreenImpl(
    versions = versions,
    serverUrl = serverUrl,
    state = state,
    onAction = { action ->
      when (action) {
        ListBudgetsAction.ChangeServer -> TODO()
        ListBudgetsAction.OpenInBrowser -> launchBrowser = true
        ListBudgetsAction.Reload -> TODO()
        is ListBudgetsAction.Delete -> budgetToDelete = action.budget
        is ListBudgetsAction.Open -> TODO()
      }
    },
  )
}

@Composable
private fun ListBudgetsScreenImpl(
  versions: ActualVersions,
  serverUrl: ServerUrl,
  state: ListBudgetsState,
  onAction: (ListBudgetsAction) -> Unit,
) {
  val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
  val theme = LocalTheme.current
  Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
      TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = theme.mobileHeaderBackground,
          titleContentColor = theme.mobileHeaderText,
        ),
        title = {
          Text(
            text = BudgetListStrings.listBudgetsToolbar,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        },
        scrollBehavior = scrollBehavior,
      )
    },
  ) { innerPadding ->
    Content(
      modifier = Modifier.padding(innerPadding),
      versions = versions,
      serverUrl = serverUrl,
      state = state,
      onAction = onAction,
    )
  }
}

@Stable
@Composable
private fun Content(
  versions: ActualVersions,
  serverUrl: ServerUrl,
  state: ListBudgetsState,
  onAction: (ListBudgetsAction) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  Column(
    modifier = modifier
      .background(theme.pageBackground)
      .fillMaxSize()
      .padding(16.dp),
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
      modifier = Modifier.fillMaxWidth(),
      url = serverUrl,
      onClickChange = { onAction(ListBudgetsAction.ChangeServer) },
    )

    VerticalSpacer()

    VersionsText(
      modifier = Modifier.align(Alignment.End),
      versions = versions,
    )
  }
}

@ActualScreenPreview
@Composable
private fun Success() = PreviewActualScreen {
  ListBudgetsScreenImpl(
    versions = PreviewVersions,
    serverUrl = ServerUrl.Demo,
    state = ListBudgetsState.Success(
      budgets = persistentListOf(PreviewBudgetSynced, PreviewBudgetSyncing, PreviewBudgetBroken),
    ),
    onAction = {},
  )
}

@ActualScreenPreview
@Composable
private fun Loading() = PreviewActualScreen {
  ListBudgetsScreenImpl(
    versions = PreviewVersions,
    serverUrl = ServerUrl.Demo,
    state = ListBudgetsState.Loading,
    onAction = {},
  )
}

@ActualScreenPreview
@Composable
private fun Failure() = PreviewActualScreen {
  ListBudgetsScreenImpl(
    versions = PreviewVersions,
    serverUrl = ServerUrl.Demo,
    state = ListBudgetsState.Failure(reason = "Something broke lol"),
    onAction = {},
  )
}
