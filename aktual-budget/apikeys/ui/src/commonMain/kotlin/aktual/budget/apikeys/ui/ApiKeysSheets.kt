package aktual.budget.apikeys.ui

import aktual.api.model.apikeys.ApiKeyId
import aktual.budget.apikeys.vm.ApiKeyItem
import aktual.budget.apikeys.vm.ApiKeysSheet
import aktual.budget.apikeys.vm.CreateKey
import aktual.budget.apikeys.vm.CreatedKey
import aktual.budget.apikeys.vm.RevokeKey
import aktual.core.l10n.Strings
import aktual.core.model.Token
import aktual.core.ui.AktualButton
import aktual.core.ui.AktualTextField
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.textSizes
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.ColorParameters
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.HostedSheet
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.SheetHost
import aktual.core.ui.disabledIf
import aktual.core.ui.disambiguated
import aktual.core.ui.keyboardFocusRequester
import aktual.core.ui.plainTextClipEntry
import aktual.core.ui.switch
import aktual.core.ui.verticalScrollWithBar
import aktual.theme.Colors
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

// Creating a key then showing it swaps pages inside one sheet
@Composable
internal fun ApiKeysSheets(
  sheet: ApiKeysSheet?,
  onCreate: (name: String, canSyncSharedBanks: Boolean) -> Unit,
  onConfirmRevoke: () -> Unit,
  onDismiss: () -> Unit,
) {
  // Keeps the last sheet on screen while it slides away after the VM clears it, e.g. on revoking
  var lastShown by remember { mutableStateOf(sheet) }
  SideEffect { if (sheet != null) lastShown = sheet }
  val current = sheet ?: lastShown ?: return

  SheetHost(
    onDismiss = {
      lastShown = null
      onDismiss()
    },
    // The server shows a new key only once, and a stray swipe shouldn't lose a typed name either
    isDismissible = current is RevokeKey,
  ) { host ->
    if (sheet == null) {
      DisposableEffect(host) {
        host.dismiss()
        onDispose {}
      }
    }

    when (current) {
      is CreateKey ->
        HostedSheet(onDismiss = host.dismiss) {
          CreateKeySheet(current, onCreate, onCancel = { close() })
        }

      is CreatedKey ->
        HostedSheet(onDismiss = host.dismiss) {
          CreatedKeySheet(current.key, onDone = { close() })
        }

      is RevokeKey ->
        HostedSheet(onDismiss = host.dismiss) {
          RevokeKeySheetContent(current, onConfirmRevoke, onCancel = { close() })
        }
    }
  }
}

@Composable
private fun CreateKeySheet(
  sheet: CreateKey,
  onCreate: (name: String, canSyncSharedBanks: Boolean) -> Unit,
  onCancel: () -> Unit,
) {
  var canSyncSharedBanks by rememberSaveable { mutableStateOf(false) }
  CreateKeySheetContent(
    sheet = sheet,
    state = rememberTextFieldState(),
    canSyncSharedBanks = canSyncSharedBanks,
    onCanSyncSharedBanksChange = { canSyncSharedBanks = it },
    onCreate = onCreate,
    onCancel = onCancel,
  )
}

@Composable
private fun CreateKeySheetContent(
  sheet: CreateKey,
  state: TextFieldState,
  canSyncSharedBanks: Boolean,
  onCanSyncSharedBanksChange: (Boolean) -> Unit,
  onCreate: (name: String, canSyncSharedBanks: Boolean) -> Unit,
  onCancel: () -> Unit,
) {
  val name = state.text.trim().toString()
  val enabled = name.isNotEmpty() && name.length <= MAX_NAME_LENGTH && !sheet.isBusy
  ApiKeysSheetPage(
    title = Strings.apiKeysCreateTitle,
    buttons = {
      AktualButton(
        modifier = Modifier.weight(1f),
        text = Strings.apiKeysCancel,
        variant = Secondary,
        onClick = onCancel,
      )
      AktualButton(
        modifier = Modifier.weight(1f),
        kind = Primary,
        text = Strings.apiKeysCreate,
        isEnabled = enabled,
        isLoading = sheet.isBusy,
        onClick = { onCreate(name, canSyncSharedBanks) },
      )
    },
  ) {
    AktualTextField(
      modifier = Modifier.fillMaxWidth().focusRequester(keyboardFocusRequester()),
      state = state,
      isEnabled = !sheet.isBusy,
      placeholderText = Strings.apiKeysNamePlaceholder,
      singleLine = true,
      keyboardOptions = KeyboardOptions(capitalization = Sentences, imeAction = Done),
      onKeyboardAction = { if (enabled) onCreate(name, canSyncSharedBanks) },
    )
    SharedBankSyncRow(
      checked = canSyncSharedBanks,
      enabled = !sheet.isBusy,
      onCheckedChange = onCanSyncSharedBanksChange,
    )
    when (sheet.error) {
      Failed -> ErrorText(Strings.apiKeysCreateFailed)
      NoBudgetAccess -> ErrorText(Strings.apiKeysCreateNoBudgetAccess)
      null -> Unit
    }
  }
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
        color = colors.text.disabledIf(!enabled),
      )
      Text(
        text = Strings.apiKeysSharedBankSyncMessage,
        style = typography.bodySmall,
        color = colors.textTertiary.disabledIf(!enabled),
      )
    }

    Switch(checked = checked, onCheckedChange = null, colors = colors.switch(), enabled = enabled)
  }

@Composable
private fun CreatedKeySheet(key: Token, onDone: () -> Unit) {
  val clipboard = LocalClipboard.current
  val scope = rememberCoroutineScope()
  CreatedKeySheetContent(
    key = key,
    onCopy = { scope.launch { clipboard.setClipEntry(plainTextClipEntry(key.value)) } },
    onDone = onDone,
  )
}

@Composable
private fun CreatedKeySheetContent(key: Token, onCopy: () -> Unit, onDone: () -> Unit) =
  ApiKeysSheetPage(
    title = Strings.apiKeysCreatedTitle,
    buttons = {
      AktualButton(
        modifier = Modifier.weight(1f),
        text = Strings.apiKeysCopy,
        variant = Secondary,
        onClick = onCopy,
      )
      AktualButton(
        modifier = Modifier.weight(1f),
        kind = Primary,
        text = Strings.apiKeysDone,
        onClick = onDone,
      )
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

@Composable
private fun RevokeKeySheetContent(sheet: RevokeKey, onConfirm: () -> Unit, onCancel: () -> Unit) =
  ApiKeysSheetPage(
    title = Strings.apiKeysRevokeTitle,
    buttons = {
      AktualButton(
        modifier = Modifier.weight(1f),
        text = Strings.apiKeysCancel,
        variant = Secondary,
        onClick = onCancel,
      )
      AktualButton(
        modifier = Modifier.weight(1f),
        kind = Destructive,
        text = Strings.apiKeysRevoke,
        isLoading = sheet.isBusy,
        onClick = onConfirm,
      )
    },
  ) {
    Text(text = Strings.apiKeysRevokeMessage(sheet.key.name), style = typography.bodyMedium)
    if (sheet.hasFailed) ErrorText(Strings.apiKeysRevokeFailed)
  }

@Composable
private fun ApiKeysSheetPage(
  title: String,
  buttons: @Composable RowScope.() -> Unit,
  content: @Composable ColumnScope.() -> Unit,
) =
  Column(
    modifier =
      Modifier.fillMaxWidth()
        .verticalScrollWithBar()
        .padding(horizontal = ApiKeysSheetDS.padding)
        .padding(bottom = ApiKeysSheetDS.padding),
    verticalArrangement = Arrangement.spacedBy(ApiKeysSheetDS.spacing),
  ) {
    Text(text = title, style = typography.headlineSmall)
    content()
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(ApiKeysSheetDS.buttonSpacing),
      content = buttons,
    )
  }

@Composable
private fun ErrorText(text: String) =
  Text(text = text, style = typography.bodySmall, color = colors.errorText)

private object ApiKeysSheetDS {
  val padding = 20.dp
  val spacing = 16.dp
  val buttonSpacing = 12.dp
}

private const val MAX_NAME_LENGTH = 100

@Preview
@Composable
private fun PreviewCreateKeySheet(
  @PreviewParameter(CreateKeyProvider::class) params: ColorParams<CreateKey>,
) =
  PreviewWithColorParams(params) {
    CreateKeySheetContent(
      sheet = this,
      state = rememberTextFieldState("Transaction importer"),
      canSyncSharedBanks = true,
      onCanSyncSharedBanksChange = {},
      onCreate = { _, _ -> },
      onCancel = {},
    )
  }

private class CreateKeyProvider :
  ColorParamsProvider<CreateKey>(
    CreateKey(),
    CreateKey(isBusy = true),
    CreateKey(error = Failed),
    CreateKey(error = NoBudgetAccess),
  )

@Preview
@Composable
private fun PreviewCreatedKeySheet(@PreviewParameter(ColorParameters::class) params: Colors) =
  PreviewWithColors(params) {
    CreatedKeySheetContent(
      key = Token("actual_q7Xc2mVb9LpR4tYw8NzK3hJf6Gd1Sa5Ue0Io-_TnBvM"),
      onCopy = {},
      onDone = {},
    )
  }

@Preview
@Composable
private fun PreviewRevokeKeySheet(
  @PreviewParameter(RevokeKeyProvider::class) params: ColorParams<RevokeKey>,
) =
  PreviewWithColorParams(params) {
    RevokeKeySheetContent(sheet = this, onConfirm = {}, onCancel = {})
  }

private val PREVIEW_KEY = ApiKeyItem(ApiKeyId("1"), "Transaction importer", LocalDate(2026, 11, 2))

private class RevokeKeyProvider :
  ColorParamsProvider<RevokeKey>(
    RevokeKey(PREVIEW_KEY),
    RevokeKey(PREVIEW_KEY, isBusy = true),
    RevokeKey(PREVIEW_KEY, hasFailed = true),
  )
