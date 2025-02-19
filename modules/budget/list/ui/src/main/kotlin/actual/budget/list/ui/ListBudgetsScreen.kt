package actual.budget.list.ui

import actual.budget.list.res.BudgetListStrings
import actual.budget.list.vm.Budget
import actual.budget.list.vm.ListBudgetsState
import actual.budget.list.vm.ListBudgetsViewModel
import actual.core.colorscheme.ColorSchemeType
import actual.core.icons.ActualIcons
import actual.core.icons.Refresh
import actual.core.ui.BasicIconButton
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewScreen
import actual.core.ui.ScreenPreview
import actual.core.ui.Theme
import actual.core.ui.UsingServerText
import actual.core.ui.VersionsText
import actual.core.ui.VerticalSpacer
import actual.core.ui.WavyBackground
import actual.core.ui.debugNavigate
import actual.core.ui.topAppBarColors
import actual.core.ui.topAppBarIconButton
import actual.core.versions.ActualVersions
import actual.login.model.LoginToken
import actual.login.nav.LoginNavRoute
import actual.url.model.ServerUrl
import actual.url.nav.ServerUrlNavRoute
import alakazam.kotlin.core.exhaustive
import android.content.Intent
import android.net.Uri
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

@Composable
fun ListBudgetsScreen(
  navController: NavHostController,
  token: LoginToken,
  viewModel: ListBudgetsViewModel = hiltViewModel<ListBudgetsViewModel, ListBudgetsViewModel.Factory>(
    creationCallback = { factory -> factory.create(token.value) },
  ),
) {
  val versions by viewModel.versions.collectAsStateWithLifecycle()
  val serverUrl by viewModel.serverUrl.collectAsStateWithLifecycle()
  val themeType by viewModel.themeType.collectAsStateWithLifecycle()
  val state by viewModel.state.collectAsStateWithLifecycle()

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

  ListBudgetsScaffold(
    versions = versions,
    state = state,
    url = serverUrl,
    themeType = themeType,
    onAction = { action ->
      when (action) {
        ListBudgetsAction.ChangeServer -> navController.openUrlScreen()
        ListBudgetsAction.OpenInBrowser -> launchBrowser = true
        ListBudgetsAction.Reload -> viewModel.retry()
        is ListBudgetsAction.Delete -> budgetToDelete = action.budget
        is ListBudgetsAction.Open -> TODO()
      }
    },
  )
}

private fun NavHostController.openUrlScreen() =
  debugNavigate(ServerUrlNavRoute) { popUpTo(LoginNavRoute) { inclusive = true } }

@Composable
private fun ListBudgetsScaffold(
  versions: ActualVersions,
  state: ListBudgetsState,
  url: ServerUrl,
  themeType: ColorSchemeType,
  onAction: (ListBudgetsAction) -> Unit,
) {
  val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
  val theme = LocalTheme.current
  Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
      TopAppBar(
        colors = theme.topAppBarColors(),
        title = { ScaffoldTitle() },
        scrollBehavior = scrollBehavior,
        actions = { TopAppBarActions(state, onAction) }
      )
    },
  ) { innerPadding ->
    Box {
      WavyBackground(themeType)

      ListBudgetsContent(
        modifier = Modifier.padding(innerPadding),
        versions = versions,
        state = state,
        url = url,
        onAction = onAction,
        theme = theme,
      )
    }
  }
}

@Composable
private fun TopAppBarActions(
  state: ListBudgetsState,
  onAction: (ListBudgetsAction) -> Unit,
  modifier: Modifier = Modifier,
) {
  if (state is ListBudgetsState.Success) {
    BasicIconButton(
      modifier = modifier,
      onClick = { onAction(ListBudgetsAction.Reload) },
      imageVector = ActualIcons.Refresh,
      contentDescription = BudgetListStrings.budgetFailureRetry,
      colors = { scheme, isPressed -> scheme.topAppBarIconButton(isPressed) },
    )
  }
}

@Stable
@Composable
private fun ScaffoldTitle() = Text(
  text = BudgetListStrings.listBudgetsToolbar,
  maxLines = 1,
  overflow = TextOverflow.Ellipsis,
)

@Stable
@Composable
private fun ListBudgetsContent(
  versions: ActualVersions,
  state: ListBudgetsState,
  url: ServerUrl,
  onAction: (ListBudgetsAction) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  Column(
    modifier = modifier
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
      modifier = Modifier.fillMaxWidth(),
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
private fun Success() = PreviewScreen { type ->
  ListBudgetsScaffold(
    versions = PreviewVersions,
    themeType = type,
    url = ServerUrl.Demo,
    state = ListBudgetsState.Success(
      budgets = persistentListOf(PreviewBudgetSynced, PreviewBudgetSyncing, PreviewBudgetBroken),
    ),
    onAction = {},
  )
}

@ScreenPreview
@Composable
private fun Loading() = PreviewScreen { type ->
  ListBudgetsScaffold(
    versions = PreviewVersions,
    themeType = type,
    url = ServerUrl.Demo,
    state = ListBudgetsState.Loading,
    onAction = {},
  )
}

@ScreenPreview
@Composable
private fun Failure() = PreviewScreen { type ->
  ListBudgetsScaffold(
    versions = PreviewVersions,
    themeType = type,
    url = ServerUrl.Demo,
    state = ListBudgetsState.Failure(reason = "Something broke lol"),
    onAction = {},
  )
}
