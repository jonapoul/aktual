package actual.budget.sync.ui

import actual.account.model.LoginToken
import actual.budget.model.BudgetId
import actual.budget.sync.res.BudgetSyncStrings
import actual.budget.sync.vm.SyncBudgetViewModel
import actual.budget.sync.vm.SyncStep
import actual.budget.sync.vm.SyncStepState
import actual.core.model.Percent
import actual.core.model.percent
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewScreen
import actual.core.ui.PrimaryTextButton
import actual.core.ui.ScreenPreview
import actual.core.ui.Theme
import actual.core.ui.WavyBackground
import actual.core.ui.transparentTopAppBarColors
import alakazam.android.ui.compose.VerticalSpacer
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList

@Composable
fun SyncBudgetScreen(
  navController: NavHostController,
  budgetId: BudgetId,
  token: LoginToken,
  viewModel: SyncBudgetViewModel = hiltViewModel(token, budgetId),
) {
  val stepStates by viewModel.stepStates.collectAsStateWithLifecycle()

  SyncBudgetScaffold(
    stepStates = stepStates,
    onAction = { action ->
      when (action) {
        SyncBudgetAction.Continue -> navController.openBudget(budgetId)
        SyncBudgetAction.Retry -> viewModel.start()
      }
    },
  )
}

@Suppress("unused")
private fun NavHostController.openBudget(budgetId: BudgetId) {
  // TODO: navigate away
  // debugNavigate(ServerUrlNavRoute) { popUpTo(ListBudgetsNavRoute) { inclusive = true } }
}

@Composable
private fun hiltViewModel(
  token: LoginToken,
  budgetId: BudgetId,
) = hiltViewModel<SyncBudgetViewModel, SyncBudgetViewModel.Factory>(
  creationCallback = { factory -> factory.create(SyncBudgetViewModel.Inputs(token, budgetId)) },
)

@Composable
private fun SyncBudgetScaffold(
  stepStates: ImmutableMap<SyncStep, SyncStepState>,
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
        stepStates = stepStates,
        onAction = onAction,
        theme = theme,
      )
    }
  }
}

@Stable
@Composable
private fun ListBudgetsContent(
  stepStates: ImmutableMap<SyncStep, SyncStepState>,
  onAction: (SyncBudgetAction) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  val stateList = remember(stepStates) { stepStates.toList().toImmutableList() }
  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(30.dp),
    verticalArrangement = Arrangement.Top,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    LazyColumn(
      modifier = Modifier
        .padding(horizontal = 20.dp)
        .fillMaxWidth(),
    ) {
      items(stateList) { (step, state) ->
        StepStateRow(
          step = step,
          state = state,
          theme = theme,
        )
      }
    }

    VerticalSpacer(1f)

    PrimaryTextButton(
      modifier = Modifier
        .padding(ButtonPadding)
        .fillMaxWidth(),
      text = BudgetSyncStrings.syncButtonContinue,
      onClick = { onAction(SyncBudgetAction.Continue) },
      contentPadding = ButtonContentPadding,
      isEnabled = stepStates.allSuccess(),
    )

    PrimaryTextButton(
      modifier = Modifier
        .padding(ButtonPadding)
        .fillMaxWidth(),
      text = BudgetSyncStrings.syncButtonRetry,
      onClick = { onAction(SyncBudgetAction.Retry) },
      contentPadding = ButtonContentPadding,
      isEnabled = stepStates.anyFailure(),
    )
  }
}

@Stable
private fun ImmutableMap<*, SyncStepState>.allSuccess() = values.all { it is SyncStepState.Succeeded }

@Stable
private fun ImmutableMap<*, SyncStepState>.anyFailure() = values.any { it is SyncStepState.Failed }

@Composable
private fun StepStateRow(
  step: SyncStep,
  state: SyncStepState,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) = Row(
  modifier = modifier.padding(vertical = 10.dp),
  verticalAlignment = Alignment.Top,
) {
  val stateText = state.string()
  val stateColor = state.color(theme)

  Column(
    modifier = Modifier
      .padding(vertical = 10.dp)
      .weight(1f),
    verticalArrangement = Arrangement.Center,
  ) {
    Text(
      modifier = Modifier.fillMaxWidth(),
      text = step.string(),
      fontWeight = FontWeight.Bold,
    )

    Text(
      modifier = Modifier.fillMaxWidth(),
      text = stateText,
      color = stateColor,
    )
  }

  StateLeadingIcon(
    state = state,
    text = stateText,
    color = stateColor,
    theme = theme,
  )
}

@Composable
private fun StateLeadingIcon(
  state: SyncStepState,
  text: String,
  color: Color,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  Box(
    modifier = modifier,
    contentAlignment = Alignment.Center,
  ) {
    when (state) {
      is SyncStepState.InProgress.Definite -> DeterminateWheel(state.progress, theme)
      SyncStepState.InProgress.Indefinite -> IndeterminateWheel(theme)
      SyncStepState.NotStarted -> StateIcon(text, color, Icons.Filled.Timer)
      is SyncStepState.Failed -> StateIcon(text, color, Icons.Filled.Block)
      SyncStepState.Succeeded -> StateIcon(text, color, Icons.Filled.Check)
    }
  }
}

@Composable
private fun DeterminateWheel(
  percent: Percent,
  theme: Theme,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier.padding(IconPadding),
    contentAlignment = Alignment.Center,
  ) {
    val animatedProgress by animateFloatAsState(
      targetValue = percent.value / 100f,
      animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
    )

    CircularProgressIndicator(
      modifier = Modifier.size(IconSize),
      color = theme.sliderActiveTrack,
      trackColor = theme.sliderInactiveTrack,
      strokeWidth = ProgressWheelStrokeWidth,
      progress = { animatedProgress },
    )
  }
}

@Composable
private fun IndeterminateWheel(
  theme: Theme,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier.padding(IconPadding),
    contentAlignment = Alignment.Center,
  ) {
    CircularProgressIndicator(
      modifier = Modifier.size(IconSize),
      color = theme.sliderActiveTrack,
      trackColor = theme.sliderInactiveTrack,
      strokeWidth = ProgressWheelStrokeWidth,
    )
  }
}

@Composable
private fun StateIcon(
  text: String,
  color: Color,
  imageVector: ImageVector,
  modifier: Modifier = Modifier,
) = Icon(
  modifier = modifier
    .padding(IconPadding)
    .size(IconSize),
  imageVector = imageVector,
  contentDescription = text,
  tint = color,
)

@Composable
@ReadOnlyComposable
private fun SyncStep.string(): String = when (this) {
  SyncStep.FetchingFileInfo -> BudgetSyncStrings.syncStepFetchingInfo
  SyncStep.StartingDatabaseDownload -> BudgetSyncStrings.syncStepStartingDownload
  SyncStep.DownloadingDatabase -> BudgetSyncStrings.syncStepDownloading
  SyncStep.ValidatingDatabase -> BudgetSyncStrings.syncStepValidating
}

@Composable
@ReadOnlyComposable
private fun SyncStepState.string(): String = when (this) {
  is SyncStepState.InProgress.Definite -> BudgetSyncStrings.syncStateDefinite(progress.value)
  SyncStepState.InProgress.Indefinite -> BudgetSyncStrings.syncStateIndefinite
  SyncStepState.NotStarted -> BudgetSyncStrings.syncStateNotStarted
  is SyncStepState.Failed -> BudgetSyncStrings.syncStateFailed(moreInfo)
  SyncStepState.Succeeded -> BudgetSyncStrings.syncStateSucceeded
}

@Stable
private fun SyncStepState.color(theme: Theme): Color = when (this) {
  is SyncStepState.InProgress -> theme.pageText
  SyncStepState.NotStarted -> theme.buttonNormalDisabledText
  is SyncStepState.Failed -> theme.errorText
  SyncStepState.Succeeded -> theme.successText
}

@ScreenPreview
@Composable
private fun NotStarted() = PreviewScreen {
  SyncBudgetScaffold(
    onAction = {},
    stepStates = persistentMapOf(
      SyncStep.FetchingFileInfo to SyncStepState.NotStarted,
      SyncStep.StartingDatabaseDownload to SyncStepState.NotStarted,
      SyncStep.DownloadingDatabase to SyncStepState.NotStarted,
      SyncStep.ValidatingDatabase to SyncStepState.NotStarted,
    ),
  )
}

@ScreenPreview
@Composable
private fun Downloading() = PreviewScreen {
  SyncBudgetScaffold(
    onAction = {},
    stepStates = persistentMapOf(
      SyncStep.FetchingFileInfo to SyncStepState.InProgress.Indefinite,
      SyncStep.StartingDatabaseDownload to SyncStepState.Succeeded,
      SyncStep.DownloadingDatabase to SyncStepState.InProgress.Definite(50.percent),
      SyncStep.ValidatingDatabase to SyncStepState.NotStarted,
    ),
  )
}

@ScreenPreview
@Composable
private fun AllSucceeded() = PreviewScreen {
  SyncBudgetScaffold(
    onAction = {},
    stepStates = persistentMapOf(
      SyncStep.FetchingFileInfo to SyncStepState.Succeeded,
      SyncStep.StartingDatabaseDownload to SyncStepState.Succeeded,
      SyncStep.DownloadingDatabase to SyncStepState.Succeeded,
      SyncStep.ValidatingDatabase to SyncStepState.Succeeded,
    ),
  )
}

@ScreenPreview
@Composable
private fun AllFailed() = PreviewScreen {
  SyncBudgetScaffold(
    onAction = {},
    stepStates = persistentMapOf(
      SyncStep.FetchingFileInfo to SyncStepState.Failed("Some error"),
      SyncStep.StartingDatabaseDownload to SyncStepState.Failed("Some other error"),
      SyncStep.DownloadingDatabase to SyncStepState.Failed("Whatever"),
      SyncStep.ValidatingDatabase to SyncStepState.Failed(
        "Another error but this one's a lot longer, to see how it handles wrapping text",
      ),
    ),
  )
}
