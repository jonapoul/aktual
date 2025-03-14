package actual.budget.sync.ui

import actual.account.model.LoginToken
import actual.budget.sync.res.BudgetSyncStrings
import actual.budget.sync.vm.SyncBudgetViewModel
import actual.budget.sync.vm.SyncState
import actual.budget.sync.vm.bytes
import actual.budget.sync.vm.percent
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewScreen
import actual.core.ui.ScreenPreview
import actual.core.ui.Theme
import actual.core.ui.WavyBackground
import actual.core.ui.transparentTopAppBarColors
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController

@Suppress("unused")
@Composable
fun SyncBudgetScreen(
  navController: NavHostController,
  token: LoginToken,
  viewModel: SyncBudgetViewModel = hiltViewModel(token),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()

  SyncBudgetScaffold(
    state = state,
    onAction = { action ->
      //      when (action) {
      // TBC
      //      }
    },
  )
}

@Composable
private fun hiltViewModel(token: LoginToken) = hiltViewModel<SyncBudgetViewModel, SyncBudgetViewModel.Factory>(
  creationCallback = { factory -> factory.create(token.value) },
)

@Composable
private fun SyncBudgetScaffold(
  state: SyncState,
  onAction: (SyncBudgetAction) -> Unit,
) {
  val theme = LocalTheme.current

  Scaffold(
    topBar = {
      TopAppBar(
        colors = theme.transparentTopAppBarColors(),
        title = {},
      )
    },
  ) { innerPadding ->
    Box {
      WavyBackground()

      ListBudgetsContent(
        modifier = Modifier.padding(innerPadding),
        state = state,
        onAction = onAction,
        theme = theme,
      )
    }
  }
}

@Stable
@Composable
private fun ListBudgetsContent(
  state: SyncState,
  onAction: (SyncBudgetAction) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(30.dp),
    verticalArrangement = Arrangement.Top,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
      text = state.titleString(),
      fontSize = 20.sp,
      fontWeight = FontWeight.W700,
      color = theme.pageText,
    )

    when (state) {
      SyncState.TalkingToServer -> IndeterminateLoadingWheel(theme = theme)

      is SyncState.Downloading -> ContentDownloading(
        fileSize = state.size,
        percent = state.percent,
        theme = theme,
      )

      SyncState.Validating -> IndeterminateLoadingWheel(theme = theme)

      SyncState.Success -> ContentSuccess(theme = theme)

      is SyncState.Failure -> ContentFailure(
        reason = state.reason,
        onClickRetry = { onAction(SyncBudgetAction.Retry) },
        theme = theme,
      )
    }
  }
}

@Composable
private fun IndeterminateLoadingWheel(
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  Box(
    modifier = modifier.padding(CirclePadding),
    contentAlignment = Alignment.Center,
  ) {
    CircularProgressIndicator(
      modifier = Modifier.size(CircleSize),
      color = theme.sliderActiveTrack,
      trackColor = theme.sliderInactiveTrack,
      strokeWidth = StrokeWidth,
    )
  }
}

@Stable
@Composable
@ReadOnlyComposable
private fun SyncState.titleString() = when (this) {
  SyncState.TalkingToServer -> BudgetSyncStrings.syncStateTalkingServer
  is SyncState.Downloading -> BudgetSyncStrings.syncDownloadingTitle
  SyncState.Validating -> BudgetSyncStrings.syncStateValidating
  SyncState.Success -> BudgetSyncStrings.syncSuccess
  is SyncState.Failure -> BudgetSyncStrings.syncFailureDesc
}

@ScreenPreview
@Composable
private fun TalkingToServer() = PreviewScreen {
  SyncBudgetScaffold(
    state = SyncState.TalkingToServer,
    onAction = {},
  )
}

@ScreenPreview
@Composable
private fun Downloading() = PreviewScreen {
  SyncBudgetScaffold(
    state = SyncState.Downloading(100.bytes, 50.percent),
    onAction = {},
  )
}

@ScreenPreview
@Composable
private fun Validating() = PreviewScreen {
  SyncBudgetScaffold(
    state = SyncState.Validating,
    onAction = {},
  )
}

@ScreenPreview
@Composable
private fun Success() = PreviewScreen {
  SyncBudgetScaffold(
    state = SyncState.Success,
    onAction = {},
  )
}

@ScreenPreview
@Composable
private fun Failure() = PreviewScreen {
  SyncBudgetScaffold(
    state = SyncState.Failure(reason = "Something broke"),
    onAction = {},
  )
}
