package aktual.budget.apikeys.ui

import aktual.budget.apikeys.vm.ApiKeysDialog
import aktual.budget.apikeys.vm.CreateKey
import aktual.budget.apikeys.vm.CreatedKey
import aktual.budget.apikeys.vm.RevokeKey
import aktual.core.l10n.Strings
import aktual.core.model.Token
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualAlertDialogContent
import aktual.core.ui.AktualTextField
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.textSizes
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.disabled
import aktual.core.ui.disambiguated
import aktual.core.ui.keyboardFocusRequester
import aktual.core.ui.plainTextClipEntry
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch

@Composable
internal fun ApiKeysDialogs(
  dialog: ApiKeysDialog?,
  onCreate: (String) -> Unit,
  onConfirmRevoke: () -> Unit,
  onDismiss: () -> Unit,
) {
  when (dialog) {
    null -> Unit
    is CreateKey -> CreateKeyDialog(dialog, onCreate, onDismiss)
    is CreatedKey -> CreatedKeyDialog(dialog.key, onDismiss)
    is RevokeKey -> RevokeKeyDialog(dialog, onConfirmRevoke, onDismiss)
  }
}

@Composable
private fun CreateKeyDialog(dialog: CreateKey, onCreate: (String) -> Unit, onDismiss: () -> Unit) =
  AktualAlertDialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(dismissOnClickOutside = false),
  ) {
    CreateKeyDialogContent(
      dialog = dialog,
      state = rememberTextFieldState(),
      onCreate = onCreate,
      onDismiss = onDismiss,
    )
  }

@Composable
private fun CreateKeyDialogContent(
  dialog: CreateKey,
  state: TextFieldState,
  onCreate: (String) -> Unit,
  onDismiss: () -> Unit,
) {
  val name = state.text.trim().toString()
  val enabled = name.isNotEmpty() && name.length <= MAX_NAME_LENGTH && !dialog.isBusy
  AktualAlertDialogContent(
    title = Strings.apiKeysCreateTitle,
    buttons = {
      TextButton(onClick = onDismiss) { Text(Strings.apiKeysCancel) }
      TextButton(enabled = enabled, onClick = { onCreate(name) }) {
        Text(
          text = Strings.apiKeysCreate,
          fontWeight = if (enabled) Bold else null,
          color = if (enabled) colors.numberPositive else colors.buttonNormalDisabledText.disabled,
        )
      }
    },
    content = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AktualTextField(
          modifier = Modifier.fillMaxWidth().focusRequester(keyboardFocusRequester()),
          state = state,
          isEnabled = !dialog.isBusy,
          placeholderText = Strings.apiKeysNamePlaceholder,
          singleLine = true,
          keyboardOptions = KeyboardOptions(capitalization = Sentences, imeAction = Done),
          onKeyboardAction = { if (enabled) onCreate(name) },
        )
        if (dialog.hasFailed) {
          Text(
            text = Strings.apiKeysCreateFailed,
            style = typography.bodySmall,
            color = colors.errorText,
          )
        }
      }
    },
  )
}

@Composable
private fun CreatedKeyDialog(key: Token, onDismiss: () -> Unit) {
  val clipboard = LocalClipboard.current
  val scope = rememberCoroutineScope()
  AktualAlertDialog(
    title = Strings.apiKeysCreatedTitle,
    onDismissRequest = onDismiss,
    properties = DialogProperties(dismissOnClickOutside = false),
    buttons = {
      TextButton(
        onClick = { scope.launch { clipboard.setClipEntry(plainTextClipEntry(key.value)) } },
      ) {
        Text(Strings.apiKeysCopy)
      }
      TextButton(onClick = onDismiss) { Text(Strings.apiKeysDone) }
    },
  ) {
    Text(text = Strings.apiKeysCreatedMessage, style = typography.bodyMedium)
    SelectionContainer {
      Text(
        text = key.value,
        style = LocalTextStyle.current.disambiguated(),
        fontSize = textSizes.small,
      )
    }
  }
}

@Composable
private fun RevokeKeyDialog(dialog: RevokeKey, onConfirm: () -> Unit, onDismiss: () -> Unit) =
  AktualAlertDialog(
    title = Strings.apiKeysRevokeTitle,
    onDismissRequest = onDismiss,
    buttons = {
      TextButton(onClick = onDismiss) { Text(Strings.apiKeysCancel) }
      TextButton(enabled = !dialog.isBusy, onClick = onConfirm) {
        Text(Strings.apiKeysRevoke, color = colors.errorText)
      }
    },
  ) {
    Text(Strings.apiKeysRevokeMessage(dialog.key.name))
    if (dialog.hasFailed) {
      Text(
        text = Strings.apiKeysRevokeFailed,
        style = typography.bodySmall,
        color = colors.errorText,
      )
    }
  }

private const val MAX_NAME_LENGTH = 100

@Preview
@Composable
private fun PreviewCreateKeyDialog(
  @PreviewParameter(CreateKeyProvider::class) params: ColorParams<CreateKey>,
) =
  PreviewWithColorParams(params) {
    CreateKeyDialogContent(
      dialog = this,
      state = rememberTextFieldState("Transaction importer"),
      onCreate = {},
      onDismiss = {},
    )
  }

private class CreateKeyProvider :
  ColorParamsProvider<CreateKey>(CreateKey(), CreateKey(isBusy = true), CreateKey(hasFailed = true))
