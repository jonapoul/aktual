package aktual.budget.apikeys.ui

import aktual.api.model.apikeys.ApiKeyId
import aktual.budget.apikeys.vm.ApiKeyItem
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
import aktual.core.ui.ColorParameters
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.DialogTextButton
import aktual.core.ui.Dimens
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.disabledIf
import aktual.core.ui.disambiguated
import aktual.core.ui.keyboardFocusRequester
import aktual.core.ui.plainTextClipEntry
import aktual.core.ui.switch
import aktual.theme.Colors
import alakazam.compose.VerticalSpacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

@Composable
internal fun ApiKeysDialogs(
  dialog: ApiKeysDialog?,
  onCreate: (name: String, canSyncSharedBanks: Boolean) -> Unit,
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
private fun CreateKeyDialog(
  dialog: CreateKey,
  onCreate: (name: String, canSyncSharedBanks: Boolean) -> Unit,
  onDismiss: () -> Unit,
) {
  var canSyncSharedBanks by rememberSaveable { mutableStateOf(false) }
  AktualAlertDialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(dismissOnClickOutside = false),
  ) {
    CreateKeyDialogContent(
      dialog = dialog,
      state = rememberTextFieldState(),
      canSyncSharedBanks = canSyncSharedBanks,
      onCanSyncSharedBanksChange = { canSyncSharedBanks = it },
      onCreate = onCreate,
      onDismiss = onDismiss,
    )
  }
}

@Composable
private fun CreateKeyDialogContent(
  dialog: CreateKey,
  state: TextFieldState,
  canSyncSharedBanks: Boolean,
  onCanSyncSharedBanksChange: (Boolean) -> Unit,
  onCreate: (name: String, canSyncSharedBanks: Boolean) -> Unit,
  onDismiss: () -> Unit,
) {
  val name = state.text.trim().toString()
  val enabled = name.isNotEmpty() && name.length <= MAX_NAME_LENGTH && !dialog.isBusy
  AktualAlertDialogContent(
    title = Strings.apiKeysCreateTitle,
    buttons = {
      DialogTextButton(text = Strings.apiKeysCancel, onClick = onDismiss)
      DialogTextButton(
        kind = Primary,
        text = Strings.apiKeysCreate,
        isEnabled = enabled,
        onClick = { onCreate(name, canSyncSharedBanks) },
      )
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
          onKeyboardAction = { if (enabled) onCreate(name, canSyncSharedBanks) },
        )
        SharedBankSyncRow(
          checked = canSyncSharedBanks,
          enabled = !dialog.isBusy,
          onCheckedChange = onCanSyncSharedBanksChange,
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
private fun SharedBankSyncRow(
  checked: Boolean,
  enabled: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
) =
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .toggleable(
          value = checked,
          enabled = enabled,
          role = Role.Switch,
          onValueChange = onCheckedChange,
        ),
    verticalAlignment = CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = Strings.apiKeysSharedBankSync,
        style = typography.bodyMedium,
        color = colors.pageText.disabledIf(!enabled),
      )
      Text(
        text = Strings.apiKeysSharedBankSyncMessage,
        style = typography.bodySmall,
        color = colors.pageTextSubdued.disabledIf(!enabled),
      )
    }

    Switch(checked = checked, onCheckedChange = null, colors = colors.switch(), enabled = enabled)
  }

@Composable
private fun CreatedKeyDialog(key: Token, onDismiss: () -> Unit) {
  val clipboard = LocalClipboard.current
  val scope = rememberCoroutineScope()
  AktualAlertDialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(dismissOnClickOutside = false),
  ) {
    CreatedKeyDialogContent(
      key = key,
      onCopy = { scope.launch { clipboard.setClipEntry(plainTextClipEntry(key.value)) } },
      onDismiss = onDismiss,
    )
  }
}

@Composable
private fun CreatedKeyDialogContent(key: Token, onCopy: () -> Unit, onDismiss: () -> Unit) =
  AktualAlertDialogContent(
    title = Strings.apiKeysCreatedTitle,
    buttons = {
      DialogTextButton(text = Strings.apiKeysCopy, onClick = onCopy)
      DialogTextButton(kind = Primary, text = Strings.apiKeysDone, onClick = onDismiss)
    },
  ) {
    Text(text = Strings.apiKeysCreatedMessage, style = typography.bodyMedium)
    VerticalSpacer(Dimens.Medium)
    SelectionContainer {
      Text(
        text = key.value,
        style = LocalTextStyle.current.disambiguated(),
        fontSize = textSizes.small,
      )
    }
  }

@Composable
private fun RevokeKeyDialog(dialog: RevokeKey, onConfirm: () -> Unit, onDismiss: () -> Unit) =
  AktualAlertDialog(onDismissRequest = onDismiss) {
    RevokeKeyDialogContent(dialog = dialog, onConfirm = onConfirm, onDismiss = onDismiss)
  }

@Composable
private fun RevokeKeyDialogContent(
  dialog: RevokeKey,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
) =
  AktualAlertDialogContent(
    title = Strings.apiKeysRevokeTitle,
    buttons = {
      DialogTextButton(text = Strings.apiKeysCancel, onClick = onDismiss)
      DialogTextButton(
        kind = Destructive,
        text = Strings.apiKeysRevoke,
        isEnabled = !dialog.isBusy,
        onClick = onConfirm,
      )
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
      canSyncSharedBanks = true,
      onCanSyncSharedBanksChange = {},
      onCreate = { _, _ -> },
      onDismiss = {},
    )
  }

private class CreateKeyProvider :
  ColorParamsProvider<CreateKey>(CreateKey(), CreateKey(isBusy = true), CreateKey(hasFailed = true))

@Preview
@Composable
private fun PreviewCreatedKeyDialog(@PreviewParameter(ColorParameters::class) params: Colors) =
  PreviewWithColors(params) {
    CreatedKeyDialogContent(
      key = Token("actual_q7Xc2mVb9LpR4tYw8NzK3hJf6Gd1Sa5Ue0Io-_TnBvM"),
      onCopy = {},
      onDismiss = {},
    )
  }

@Preview
@Composable
private fun PreviewRevokeKeyDialog(
  @PreviewParameter(RevokeKeyProvider::class) params: ColorParams<RevokeKey>,
) =
  PreviewWithColorParams(params) {
    RevokeKeyDialogContent(dialog = this, onConfirm = {}, onDismiss = {})
  }

private val PREVIEW_KEY = ApiKeyItem(ApiKeyId("1"), "Transaction importer", LocalDate(2026, 11, 2))

private class RevokeKeyProvider :
  ColorParamsProvider<RevokeKey>(
    RevokeKey(PREVIEW_KEY),
    RevokeKey(PREVIEW_KEY, isBusy = true),
    RevokeKey(PREVIEW_KEY, hasFailed = true),
  )
