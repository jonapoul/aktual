package aktual.budget.list.ui

import aktual.budget.list.vm.BudgetNameError
import aktual.budget.list.vm.CreateBudgetState
import aktual.budget.list.vm.MAX_BUDGET_NAME_LENGTH
import aktual.budget.list.vm.uniqueBudgetName
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
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.window.DialogProperties
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf

@Composable
internal fun CreateBudgetDialog(
  state: CreateBudgetState,
  existingNames: ImmutableSet<String>,
  onCreate: (String) -> Unit,
  onDismiss: () -> Unit,
) =
  AktualAlertDialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(dismissOnClickOutside = false),
  ) {
    CreateBudgetDialogContent(
      state = state,
      existingNames = existingNames,
      nameState = rememberSelectedTextFieldState(uniqueBudgetName(existingNames)),
      onCreate = onCreate,
      onDismiss = onDismiss,
    )
  }

@Composable
private fun CreateBudgetDialogContent(
  state: CreateBudgetState,
  existingNames: ImmutableSet<String>,
  nameState: TextFieldState,
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

        // Blank already disables the button, so don't nag about it
        val message =
          when {
            state is Failed -> Strings.budgetCreateErrorFailed
            error != null && error != Blank -> error.message(name)
            else -> null
          }
        message?.let {
          Text(text = it, style = typography.bodyMedium, color = colors.errorText)
        }
      }
    },
  )
}

// Selected so typing replaces the suggested name
@Composable
private fun rememberSelectedTextFieldState(initialText: String): TextFieldState =
  rememberTextFieldState(initialText, initialSelection = TextRange(0, initialText.length))

@Composable
private fun BudgetNameError.message(name: String): String =
  when (this) {
    Blank -> Strings.budgetCreateErrorBlank
    TooLong -> Strings.budgetCreateErrorTooLong(MAX_BUDGET_NAME_LENGTH)
    is Taken -> Strings.budgetCreateErrorTaken(name.trim(), suggestion)
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
      onCreate = {},
      onDismiss = {},
    )
  }

private data class PreviewParams(val name: String, val state: CreateBudgetState)

private class CreateBudgetDialogProvider :
  ColorParamsProvider<PreviewParams>(
    PreviewParams("My Finances 1", Idle),
    PreviewParams("My Finances", Idle),
    PreviewParams("Household", Failed),
  )
