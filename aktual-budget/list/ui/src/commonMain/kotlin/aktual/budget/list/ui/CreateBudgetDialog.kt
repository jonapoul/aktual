package aktual.budget.list.ui

import aktual.budget.list.vm.BudgetNameError
import aktual.budget.list.vm.CreateBudgetState
import aktual.budget.list.vm.MAX_BUDGET_NAME_LENGTH
import aktual.budget.list.vm.validateBudgetName
import aktual.core.l10n.Strings
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualAlertDialogContent
import aktual.core.ui.AktualTextField
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.DialogTextButton
import aktual.core.ui.Dimens
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.keyboardFocusRequester
import aktual.core.ui.switch
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.window.DialogProperties
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf

@Composable
internal fun CreateBudgetDialog(
  state: CreateBudgetState,
  existingNames: ImmutableSet<String>,
  onCreate: (name: String, syncWithServer: Boolean) -> Unit,
  onDismiss: () -> Unit,
) {
  var syncWithServer by rememberSaveable { mutableStateOf(false) }
  AktualAlertDialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(dismissOnClickOutside = false),
  ) {
    CreateBudgetDialogContent(
      state = state,
      existingNames = existingNames,
      nameState = rememberTextFieldState(),
      syncWithServer = syncWithServer,
      onSyncWithServerChange = { syncWithServer = it },
      onCreate = { name -> onCreate(name, syncWithServer) },
      onDismiss = onDismiss,
    )
  }
}

@Composable
private fun CreateBudgetDialogContent(
  state: CreateBudgetState,
  existingNames: ImmutableSet<String>,
  nameState: TextFieldState,
  syncWithServer: Boolean,
  onSyncWithServerChange: (Boolean) -> Unit,
  onCreate: (String) -> Unit,
  onDismiss: () -> Unit,
) {
  val name = nameState.text.toString()
  val error = validateBudgetName(name, existingNames)
  val isCreating = state is Creating
  val enabled = error == null && !isCreating
  AktualAlertDialogContent(
    title = Strings.budgetCreateTitle,
    buttons = {
      DialogTextButton(text = Strings.budgetCreateCancel, onClick = onDismiss)
      DialogTextButton(
        kind = Primary,
        text = Strings.budgetCreateConfirm,
        isEnabled = enabled,
        onClick = { onCreate(name) },
      )
    },
    content = {
      Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = spacedBy(Dimens.Large)) {
        AktualTextField(
          modifier = Modifier.fillMaxWidth().focusRequester(keyboardFocusRequester()),
          state = nameState,
          placeholderText = Strings.budgetCreatePlaceholder,
          singleLine = true,
          keyboardOptions = KeyboardOptions(capitalization = Sentences, imeAction = Done),
          onKeyboardAction = { if (enabled) onCreate(name) },
        )

        SyncWithServerRow(checked = syncWithServer, onCheckedChange = onSyncWithServerChange)

        // Blank already disables the button, so don't nag about it
        val message =
          when {
            state is Failed -> Strings.budgetCreateErrorFailed
            error != null && error != Blank -> error.message()
            else -> null
          }
        message?.let {
          Text(text = it, style = typography.bodyMedium, color = colors.errorText)
        }
      }
    },
  )
}

@Composable
private fun SyncWithServerRow(checked: Boolean, onCheckedChange: (Boolean) -> Unit) =
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = CenterVertically,
    horizontalArrangement = spacedBy(Dimens.Large),
  ) {
    Column(modifier = Modifier.weight(1f), verticalArrangement = spacedBy(Dimens.Medium)) {
      Text(text = Strings.budgetCreateSync, style = typography.bodyLarge)
      Text(
        text = Strings.budgetCreateSyncHint,
        style = typography.bodySmall,
        color = colors.pageTextSubdued,
      )
    }
    Switch(checked = checked, onCheckedChange = onCheckedChange, colors = colors.switch())
  }

@Composable
private fun BudgetNameError.message(): String =
  when (this) {
    Blank -> Strings.budgetCreateErrorBlank
    TooLong -> Strings.budgetCreateErrorTooLong(MAX_BUDGET_NAME_LENGTH)
    is Taken -> Strings.budgetCreateErrorTaken(name)
  }

@Preview
@Composable
private fun PreviewCreateBudgetDialog(
  @PreviewParameter(CreateBudgetDialogProvider::class) params: ColorParams<PreviewParams>,
) =
  PreviewWithColorParams(params) {
    CreateBudgetDialogContent(
      state = params.data.state,
      existingNames = persistentSetOf("My Finances"),
      nameState = rememberTextFieldState(params.data.name),
      syncWithServer = params.data.syncWithServer,
      onSyncWithServerChange = {},
      onCreate = {},
      onDismiss = {},
    )
  }

private data class PreviewParams(
  val name: String,
  val state: CreateBudgetState,
  val syncWithServer: Boolean = false,
)

private class CreateBudgetDialogProvider :
  ColorParamsProvider<PreviewParams>(
    PreviewParams("My Finances 1", Idle, syncWithServer = true),
    PreviewParams("My Finances", Idle),
    PreviewParams("Household", Failed),
  )
