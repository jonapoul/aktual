package aktual.budget.navrail.ui

import aktual.core.l10n.Strings
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualAlertDialogContent
import aktual.core.ui.AktualTextField
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.disabled
import aktual.core.ui.keyboardFocusRequester
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.window.DialogProperties

@Composable
internal fun RenameBudgetDialog(
  currentName: String,
  onConfirm: (String) -> Unit,
  onDismiss: () -> Unit,
) =
  AktualAlertDialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(dismissOnClickOutside = false),
  ) {
    RenameBudgetDialogContent(
      currentName = currentName,
      state = rememberTextFieldState(currentName),
      onConfirm = onConfirm,
      onDismiss = onDismiss,
    )
  }

@Composable
private fun RenameBudgetDialogContent(
  currentName: String,
  state: TextFieldState,
  onConfirm: (String) -> Unit,
  onDismiss: () -> Unit,
) {
  val name = state.text.trim().toString()
  val enabled = name.isNotEmpty() && name != currentName
  AktualAlertDialogContent(
    title = Strings.budgetNavMenuRenameBudget,
    buttons = {
      TextButton(onClick = onDismiss) { Text(Strings.budgetNavRenameCancel) }
      TextButton(enabled = enabled, onClick = { onConfirm(name) }) {
        Text(
          text = Strings.budgetNavRenameConfirm,
          fontWeight = if (enabled) Bold else null,
          color = if (enabled) colors.numberPositive else colors.buttonNormalDisabledText.disabled,
        )
      }
    },
    content = {
      AktualTextField(
        modifier = Modifier.fillMaxWidth().focusRequester(keyboardFocusRequester()),
        state = state,
        placeholderText = Strings.budgetNavRenamePlaceholder,
        singleLine = true,
        keyboardOptions = KeyboardOptions(capitalization = Sentences, imeAction = Done),
        onKeyboardAction = { if (enabled) onConfirm(name) },
      )
    },
  )
}

@Preview
@Composable
private fun PreviewRenameBudgetDialog(
  @PreviewParameter(RenameBudgetDialogProvider::class) params: ColoredParams<String>,
) =
  PreviewWithColoredParams(params) {
    RenameBudgetDialogContent(
      currentName = "My Budget",
      state = rememberTextFieldState(params.data),
      onConfirm = {},
      onDismiss = {},
    )
  }

private class RenameBudgetDialogProvider :
  ColoredParameterProvider<String>("My Budget", "Household", "")
