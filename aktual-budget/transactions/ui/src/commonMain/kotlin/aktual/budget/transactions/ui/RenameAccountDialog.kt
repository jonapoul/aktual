package aktual.budget.transactions.ui

import aktual.core.l10n.Strings
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualAlertDialogContent
import aktual.core.ui.AktualTextField
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.disabled
import aktual.core.ui.keyboardFocusRequester
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf

@Composable
internal fun RenameAccountDialog(
  currentName: String,
  otherNames: ImmutableSet<String>,
  onConfirm: (String) -> Unit,
  onDismiss: () -> Unit,
) =
  AktualAlertDialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(dismissOnClickOutside = false),
  ) {
    RenameAccountDialogContent(
      currentName = currentName,
      otherNames = otherNames,
      state = rememberTextFieldState(currentName),
      onConfirm = onConfirm,
      onDismiss = onDismiss,
    )
  }

@Composable
private fun RenameAccountDialogContent(
  currentName: String,
  otherNames: ImmutableSet<String>,
  state: TextFieldState,
  onConfirm: (String) -> Unit,
  onDismiss: () -> Unit,
) {
  val name = state.text.trim().toString()
  val isDuplicate = name in otherNames
  val enabled = name.isNotEmpty() && name != currentName && !isDuplicate
  AktualAlertDialogContent(
    title = Strings.transactionsRenameAccount,
    buttons = {
      TextButton(onClick = onDismiss) { Text(Strings.transactionsRenameAccountCancel) }
      TextButton(enabled = enabled, onClick = { onConfirm(name) }) {
        Text(
          text = Strings.transactionsRenameAccountConfirm,
          fontWeight = if (enabled) Bold else null,
          color = if (enabled) colors.numberPositive else colors.buttonNormalDisabledText.disabled,
        )
      }
    },
    content = {
      Column {
        AktualTextField(
          modifier = Modifier.fillMaxWidth().focusRequester(keyboardFocusRequester()),
          state = state,
          placeholderText = Strings.transactionsRenameAccountPlaceholder,
          singleLine = true,
          keyboardOptions = KeyboardOptions(capitalization = Words, imeAction = Done),
          onKeyboardAction = { if (enabled) onConfirm(name) },
        )
        if (isDuplicate) {
          Text(
            modifier = Modifier.padding(top = 8.dp),
            text = Strings.transactionsRenameAccountDuplicate(name),
            style = typography.bodySmall,
            color = colors.errorText,
          )
        }
      }
    },
  )
}

@Preview
@Composable
private fun PreviewRenameAccountDialog(
  @PreviewParameter(RenameAccountDialogProvider::class) params: ColorParams<String>,
) =
  PreviewWithColorParams(params) {
    RenameAccountDialogContent(
      currentName = "Amex",
      otherNames = persistentSetOf("Barclays"),
      state = rememberTextFieldState(params.data),
      onConfirm = {},
      onDismiss = {},
    )
  }

private class RenameAccountDialogProvider :
  ColorParamsProvider<String>("Amex", "Savings", "Barclays", "")
