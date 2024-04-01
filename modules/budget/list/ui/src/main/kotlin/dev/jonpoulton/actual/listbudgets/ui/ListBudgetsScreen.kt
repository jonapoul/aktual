package dev.jonpoulton.actual.listbudgets.ui

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
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.jonpoulton.actual.core.model.ActualVersions
import dev.jonpoulton.actual.core.model.ServerUrl
import dev.jonpoulton.actual.core.ui.ActualColorScheme
import dev.jonpoulton.actual.core.ui.ActualScreenPreview
import dev.jonpoulton.actual.core.ui.LocalActualColorScheme
import dev.jonpoulton.actual.core.ui.PreviewActualScreen
import dev.jonpoulton.actual.core.ui.UsingServerText
import dev.jonpoulton.actual.core.ui.VersionsText
import dev.jonpoulton.actual.core.ui.VerticalSpacer
import dev.jonpoulton.actual.listbudgets.vm.Budget
import dev.jonpoulton.actual.listbudgets.vm.ListBudgetsState
import dev.jonpoulton.actual.listbudgets.vm.ListBudgetsViewModel
import kotlinx.collections.immutable.persistentListOf
import dev.jonpoulton.actual.core.res.R as ResR

@Composable
fun ListBudgetsScreen(
  navigator: ListBudgetsNavigator,
  viewModel: ListBudgetsViewModel = hiltViewModel(),
) {
  val versions by viewModel.versions.collectAsStateWithLifecycle()
  val serverUrl by viewModel.serverUrl.collectAsStateWithLifecycle()
  val state by viewModel.state.collectAsStateWithLifecycle()

  var launchBrowser by remember { mutableStateOf(false) }
  if (launchBrowser) {
    val context = LocalContext.current
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = Uri.parse(serverUrl.toString())
    ContextCompat.startActivity(context, intent, null)
    launchBrowser = false
  }

  var budgetToDelete by remember { mutableStateOf<Budget?>(null) }
  budgetToDelete?.let { budget ->
    DeleteBudgetDialog(
      budget = budget,
      onDeleteLocal = { /* TODO */ },
      onDeleteRemote = { /* TODO */ },
      onDismiss = { budgetToDelete = null },
    )
  }

  ListBudgetsScreenImpl(
    versions = versions,
    serverUrl = serverUrl,
    state = state,
    onClickChangeServer = { /* TODO */ },
    onClickReload = { /* TODO */ },
    onCreateBudgetInBrowser = { launchBrowser = true },
    onClickOpen = { /* TODO */ },
    onClickDelete = { budgetToDelete = it },
  )
}

@Composable
private fun ListBudgetsScreenImpl(
  versions: ActualVersions,
  serverUrl: ServerUrl,
  state: ListBudgetsState,
  onClickChangeServer: () -> Unit,
  onClickReload: () -> Unit,
  onCreateBudgetInBrowser: () -> Unit,
  onClickOpen: (Budget) -> Unit,
  onClickDelete: (Budget) -> Unit,
) {
  val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
  val colors = LocalActualColorScheme.current
  Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
      TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = colors.mobileHeaderBackground,
          titleContentColor = colors.mobileHeaderText,
        ),
        title = {
          Text(
            text = stringResource(id = ResR.string.list_budgets_toolbar),
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
      onClickChangeServer = onClickChangeServer,
      onClickReload = onClickReload,
      onCreateBudgetInBrowser = onCreateBudgetInBrowser,
      onClickOpen = onClickOpen,
      onClickDelete = onClickDelete,
    )
  }
}

@Stable
@Composable
private fun Content(
  versions: ActualVersions,
  serverUrl: ServerUrl,
  state: ListBudgetsState,
  onClickChangeServer: () -> Unit,
  onClickReload: () -> Unit,
  onCreateBudgetInBrowser: () -> Unit,
  onClickOpen: (Budget) -> Unit,
  onClickDelete: (Budget) -> Unit,
  modifier: Modifier = Modifier,
  colors: ActualColorScheme = LocalActualColorScheme.current,
) {
  Column(
    modifier = modifier
      .background(colors.pageBackground)
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
            colors = colors,
          )
        }

        is ListBudgetsState.Failure -> {
          ContentFailure(
            modifier = Modifier.fillMaxSize(),
            reason = state.reason,
            onClickRetry = onClickReload,
            colors = colors,
          )
        }

        is ListBudgetsState.Success -> {
          if (state.budgets.isEmpty()) {
            ContentEmpty(
              modifier = Modifier.fillMaxSize(),
              onCreateBudgetInBrowser = onCreateBudgetInBrowser,
            )
          } else {
            ContentSuccess(
              modifier = Modifier.fillMaxSize(),
              budgets = state.budgets,
              colors = colors,
              onClickOpen = onClickOpen,
              onClickDelete = onClickDelete,
            )
          }
        }
      }.exhaustive
    }

    VerticalSpacer(20.dp)

    UsingServerText(
      modifier = Modifier.fillMaxWidth(),
      url = serverUrl,
      onClickChange = onClickChangeServer,
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
private fun Loading() = PreviewActualScreen {
  ListBudgetsScreenImpl(
    versions = PreviewVersions,
    serverUrl = ServerUrl.Demo,
    state = ListBudgetsState.Loading,
    onClickChangeServer = {},
    onClickReload = {},
    onCreateBudgetInBrowser = {},
    onClickOpen = {},
    onClickDelete = {},
  )
}

@ActualScreenPreview
@Composable
private fun Failure() = PreviewActualScreen {
  ListBudgetsScreenImpl(
    versions = PreviewVersions,
    serverUrl = ServerUrl.Demo,
    state = ListBudgetsState.Failure(reason = "Something broke lol"),
    onClickChangeServer = {},
    onClickReload = {},
    onCreateBudgetInBrowser = {},
    onClickOpen = {},
    onClickDelete = {},
  )
}

@ActualScreenPreview
@Composable
private fun Success() = PreviewActualScreen {
  ListBudgetsScreenImpl(
    versions = PreviewVersions,
    serverUrl = ServerUrl.Demo,
    state = ListBudgetsState.Success(budgets = persistentListOf(PreviewBudget, PreviewBudget, PreviewBudget)),
    onClickChangeServer = {},
    onClickReload = {},
    onCreateBudgetInBrowser = {},
    onClickOpen = {},
    onClickDelete = {},
  )
}
