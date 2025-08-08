@file:OptIn(ExperimentalMaterial3Api::class)

package actual.budget.sync.ui

import actual.account.model.LoginToken
import actual.budget.model.BudgetId
import actual.budget.sync.vm.KeyPasswordState
import actual.budget.sync.vm.SyncBudgetViewModel
import actual.budget.sync.vm.SyncStep
import actual.budget.sync.vm.SyncStepState
import actual.core.model.Percent
import actual.core.ui.BackHandler
import actual.core.ui.LocalTheme
import actual.core.ui.PrimaryTextButton
import actual.core.ui.Theme
import actual.core.ui.WavyBackground
import actual.core.ui.assistedMetroViewModel
import actual.core.ui.transparentTopAppBarColors
import actual.l10n.Strings
import alakazam.kotlin.compose.VerticalSpacer
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList

@Composable
fun SyncBudgetScreen(
  nav: SyncBudgetNavigator,
  budgetId: BudgetId,
  token: LoginToken,
  viewModel: SyncBudgetViewModel = assistedViewModel(token, budgetId),
) {
  val stepStates by viewModel.stepStates.collectAsStateWithLifecycle()
  val passwordState by viewModel.passwordState.collectAsStateWithLifecycle()
  val budgetIsLoaded by viewModel.budgetIsLoaded.collectAsStateWithLifecycle()

  BackHandler(enabled = budgetIsLoaded) {
    viewModel.clearBudget()
    nav.back()
  }

  SyncBudgetScaffold(
    stepStates = stepStates,
    passwordState = passwordState,
    onAction = { action ->
      when (action) {
        SyncBudgetAction.Continue -> nav.toBudget(token, budgetId)
        SyncBudgetAction.Retry -> viewModel.start()
        SyncBudgetAction.ConfirmKeyPassword -> viewModel.confirmKeyPassword()
        is SyncBudgetAction.EnterKeyPassword -> viewModel.enterKeyPassword(action.input)
        SyncBudgetAction.DismissPasswordDialog -> viewModel.dismissKeyPasswordDialog()
        SyncBudgetAction.LearnMore -> viewModel.learnMore()
      }
    },
  )
}

@Composable
private fun assistedViewModel(
  token: LoginToken,
  budgetId: BudgetId,
) = assistedMetroViewModel<SyncBudgetViewModel, SyncBudgetViewModel.Factory> {
  create(token, budgetId)
}

@Composable
internal fun SyncBudgetScaffold(
  stepStates: ImmutableMap<SyncStep, SyncStepState>,
  passwordState: KeyPasswordState,
  onAction: (SyncBudgetAction) -> Unit,
) {
  val theme = LocalTheme.current

  if (passwordState is KeyPasswordState.Active) {
    EnterKeyPasswordDialog(
      input = passwordState.input,
      onAction = onAction,
    )
  }

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
      text = Strings.syncButtonContinue,
      onClick = { onAction(SyncBudgetAction.Continue) },
      contentPadding = ButtonContentPadding,
      isEnabled = stepStates.allSuccess(),
    )

    PrimaryTextButton(
      modifier = Modifier
        .padding(ButtonPadding)
        .fillMaxWidth(),
      text = Strings.syncButtonRetry,
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
      targetValue = percent.floatValue / 100f,
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
private fun SyncStep.string(): String = when (this) {
  SyncStep.FetchingFileInfo -> Strings.syncStepFetchingInfo
  SyncStep.DownloadingDatabase -> Strings.syncStepDownloading
  SyncStep.ValidatingDatabase -> Strings.syncStepValidating
}

@Composable
private fun SyncStepState.string(): String = when (this) {
  is SyncStepState.InProgress.Definite -> Strings.syncStateDefinite(progress.intValue)
  SyncStepState.InProgress.Indefinite -> Strings.syncStateIndefinite
  SyncStepState.NotStarted -> Strings.syncStateNotStarted
  is SyncStepState.Failed -> Strings.syncStateFailed(moreInfo)
  SyncStepState.Succeeded -> Strings.syncStateSucceeded
}

@Stable
private fun SyncStepState.color(theme: Theme): Color = when (this) {
  is SyncStepState.InProgress -> theme.pageText
  SyncStepState.NotStarted -> theme.buttonNormalDisabledText
  is SyncStepState.Failed -> theme.errorText
  SyncStepState.Succeeded -> theme.successText
}
