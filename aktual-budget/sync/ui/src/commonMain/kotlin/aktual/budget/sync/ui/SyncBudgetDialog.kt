package aktual.budget.sync.ui

import aktual.budget.model.BudgetId
import aktual.budget.sync.vm.KeyPasswordState
import aktual.budget.sync.vm.SyncBudgetViewModel
import aktual.budget.sync.vm.SyncOverallState
import aktual.budget.sync.vm.SyncStep
import aktual.budget.sync.vm.SyncStepState
import aktual.core.icons.material.Error
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Strings
import aktual.core.model.Password
import aktual.core.model.Token
import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import aktual.core.ui.AlertDialog
import aktual.core.ui.DialogContent
import aktual.core.ui.MY_PHONE_WIDTH_DP
import aktual.core.ui.PreviewWithThemedParams
import aktual.core.ui.TextField
import aktual.core.ui.ThemedParams
import aktual.core.ui.checkbox
import aktual.core.ui.keyboardFocusRequester
import alakazam.compose.VerticalSpacer
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList

@Composable
fun SyncBudgetDialog(
  budgetId: BudgetId,
  token: Token,
  onSyncComplete: () -> Unit,
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
  viewModel: SyncBudgetViewModel = syncBudgetViewModel(token, budgetId),
) {
  val invalidateAndDismiss = {
    viewModel.clearBudget()
    onDismissRequest()
  }

  val stepStates by viewModel.stepStates.collectAsStateWithLifecycle()
  val passwordState by viewModel.passwordState.collectAsStateWithLifecycle()
  val overallState by viewModel.overallState.collectAsStateWithLifecycle()

  SyncBudgetDialog(
    modifier = modifier,
    theme = theme,
    overallState = overallState,
    stepStates = stepStates,
    passwordState = passwordState,
    onAction = { action ->
      when (action) {
        SyncBudgetAction.Continue -> onSyncComplete()
        SyncBudgetAction.Retry -> viewModel.start()
        SyncBudgetAction.ConfirmKeyPassword -> viewModel.confirmKeyPassword()
        is SyncBudgetAction.EnterKeyPassword -> viewModel.enterKeyPassword(action.input)
        SyncBudgetAction.LearnMore -> viewModel.learnMore()
        SyncBudgetAction.Cancel -> invalidateAndDismiss()
      }
    },
  )
}

@Composable
private fun SyncBudgetDialog(
  overallState: SyncOverallState,
  stepStates: ImmutableMap<SyncStep, SyncStepState>,
  passwordState: KeyPasswordState,
  onAction: (SyncBudgetAction) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  AlertDialog(
    modifier = modifier,
    onDismissRequest = { onAction(SyncBudgetAction.Cancel) },
    properties =
      DialogProperties(
        dismissOnBackPress = false,
        dismissOnClickOutside = false,
        usePlatformDefaultWidth = true,
      ),
  ) {
    DialogContent(
      theme = theme,
      title = null,
      content = {
        SyncBudgetDialogContent(
          stepStates = stepStates,
          passwordState = passwordState,
          theme = theme,
          onAction = onAction,
        )
      },
      buttons = {
        TextButton(
          onClick = { onAction(SyncBudgetAction.Cancel) },
          content = { Text(text = Strings.syncCancel) },
        )
        when {
          passwordState is KeyPasswordState.Active -> {
            val enabled = passwordState.input.isNotEmpty()
            val color = if (enabled) theme.buttonPrimaryText else theme.buttonNormalDisabledText
            TextButton(
              enabled = enabled,
              onClick = { onAction(SyncBudgetAction.ConfirmKeyPassword) },
              content = { Text(text = Strings.syncPasswordConfirm, color = color) },
            )
          }
          overallState == SyncOverallState.Failed -> {
            TextButton(
              onClick = { onAction(SyncBudgetAction.Retry) },
              content = { Text(text = Strings.syncRetry) },
            )
          }
          else -> {
            TextButton(
              enabled = overallState == SyncOverallState.Succeeded,
              onClick = { onAction(SyncBudgetAction.Continue) },
              content = { Text(text = Strings.syncOpen) },
            )
          }
        }
      },
    )
  }
}

@Composable
private fun syncBudgetViewModel(token: Token, budgetId: BudgetId): SyncBudgetViewModel =
  assistedMetroViewModel<SyncBudgetViewModel, SyncBudgetViewModel.Factory> {
    create(token, budgetId)
  }

@Composable
@Suppress("UnusedReceiverParameter")
private fun ColumnScope.SyncBudgetDialogContent(
  stepStates: ImmutableMap<SyncStep, SyncStepState>,
  passwordState: KeyPasswordState,
  onAction: (SyncBudgetAction) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  val stateList = remember(stepStates) { stepStates.toList().toImmutableList() }

  Row(
    modifier = modifier.padding(12.dp).fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterHorizontally),
    verticalAlignment = Alignment.Top,
  ) {
    stateList.fastForEach { (_, state) ->
      SyncStepState(modifier = Modifier.weight(1f), state = state, theme = theme)
    }
  }

  val activeStep =
    remember(stepStates) {
      stepStates.entries.lastOrNull { it.value !is SyncStepState.NotStarted }?.key
    }

  if (activeStep != null && passwordState !is KeyPasswordState.Active) {
    Text(
      text = activeStep.label(),
      style = MaterialTheme.typography.labelSmall,
      color = theme.pageTextSubdued,
      textAlign = TextAlign.Center,
      modifier = Modifier.fillMaxWidth(),
    )
  }

  if (passwordState is KeyPasswordState.Active) {
    PasswordEntryLayout(password = passwordState.input, onAction = onAction, theme = theme)
  }
}

private val ITEM_SIZE = 24.dp

@Composable
private fun SyncStepState(state: SyncStepState, theme: Theme, modifier: Modifier = Modifier) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Top,
  ) {
    when (state) {
      is SyncStepState.NotStarted -> {
        TintedDot(size = 6.dp, color = theme.reportsNumberNeutral)
      }

      is SyncStepState.InProgress.Indefinite -> {
        CircularProgressIndicator(modifier = Modifier.size(ITEM_SIZE), color = theme.reportsBlue)
      }

      is SyncStepState.InProgress.Definite -> {
        CircularProgressIndicator(
          progress = { state.progress.floatValue / 100f },
          modifier = Modifier.size(ITEM_SIZE),
          color = theme.reportsBlue,
        )
      }

      is SyncStepState.Succeeded -> {
        TintedDot(size = 12.dp, color = theme.reportsNumberPositive)
      }

      is SyncStepState.Failed -> {
        Icon(
          imageVector = MaterialIcons.Error,
          contentDescription = state.moreInfo,
          modifier = Modifier.size(ITEM_SIZE),
          tint = theme.reportsNumberNegative,
        )
      }
    }
  }
}

@Composable
private fun TintedDot(size: Dp, color: Color) {
  Box(modifier = Modifier.size(ITEM_SIZE), contentAlignment = Alignment.Center) {
    Box(modifier = Modifier.size(size).clip(CircleShape).background(color, CircleShape))
  }
}

@Composable
private fun SyncStep.label(): String =
  when (this) {
    SyncStep.FetchingFileInfo -> Strings.syncStepFetchingInfo
    SyncStep.DownloadingDatabase -> Strings.syncStepDownloading
    SyncStep.ValidatingDatabase -> Strings.syncStepValidating
  }

@Composable
private fun PasswordEntryLayout(
  password: Password,
  onAction: (SyncBudgetAction) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
      modifier = Modifier.padding(horizontal = 20.dp),
      text = buildPasswordText(theme, onAction),
      fontSize = 14.sp,
    )

    VerticalSpacer(4.dp)

    val keyboard = LocalSoftwareKeyboardController.current
    var passwordVisible by remember { mutableStateOf(false) }

    TextField(
      modifier =
        Modifier.padding(horizontal = 20.dp)
          .fillMaxWidth()
          .focusRequester(keyboardFocusRequester(keyboard)),
      value = password.value,
      onValueChange = { value -> onAction(SyncBudgetAction.EnterKeyPassword(Password(value))) },
      placeholderText = Strings.syncPasswordPlaceholder,
      singleLine = true,
      visualTransformation =
        if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
      keyboardOptions =
        KeyboardOptions(
          autoCorrectEnabled = false,
          capitalization = KeyboardCapitalization.None,
          keyboardType = KeyboardType.Password,
          imeAction = ImeAction.Go,
        ),
      keyboardActions =
        KeyboardActions(
          onGo = {
            keyboard?.hide()
            onAction(SyncBudgetAction.ConfirmKeyPassword)
          }
        ),
    )

    Row(
      modifier = Modifier.clickable { passwordVisible = !passwordVisible },
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Checkbox(
        modifier = Modifier.minimumInteractiveComponentSize(),
        checked = passwordVisible,
        onCheckedChange = null,
        colors = theme.checkbox(),
      )
      Text(text = Strings.syncPasswordShowPassword)
    }
  }
}

@Stable
@Composable
private fun buildPasswordText(theme: Theme, onAction: (SyncBudgetAction) -> Unit): AnnotatedString =
  buildAnnotatedString {
    append(Strings.syncPasswordText)
    append(" ")

    val style = SpanStyle(color = theme.pageTextLink, textDecoration = TextDecoration.Underline)
    val link =
      LinkAnnotation.Clickable(
        tag = Tags.KeyPasswordDialogLearnMore,
        linkInteractionListener = { onAction(SyncBudgetAction.LearnMore) },
      )
    withStyle(style) { withLink(link) { append(Strings.syncPasswordLearnMore) } }
  }

@Preview(widthDp = MY_PHONE_WIDTH_DP, heightDp = 500)
@Composable
private fun PreviewSyncBudgetDialog(
  @PreviewParameter(SyncBudgetDialogProvider::class) params: ThemedParams<SyncBudgetDialogParams>
) {
  PreviewWithThemedParams(params) {
    SyncBudgetDialog(
      overallState = overallState,
      stepStates = stepStates,
      passwordState = passwordState,
      onAction = {},
    )
  }
}
