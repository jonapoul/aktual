package aktual.account.ui.url

import aktual.core.model.Protocol
import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import aktual.core.ui.AktualExposedDropDownMenu
import aktual.core.ui.AktualTextField
import aktual.core.ui.PreviewWithTheme
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import aktual.core.ui.keyboardFocusRequester
import alakazam.kotlin.parse
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.toImmutableList

@Composable
internal fun InputFields(
  url: String,
  protocol: Protocol,
  onAction: ServerUrlActionHandler,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
    AktualExposedDropDownMenu(
      value = protocol.toString(),
      options = PROTOCOLS,
      string = { it },
      contentPadding = TextFieldDefaults.contentPaddingWithoutLabel(),
      onValueChange = { onAction(SelectProtocol(Protocol::class.parse(it))) },
      theme = theme,
    )

    val keyboard = LocalSoftwareKeyboardController.current

    val textState = rememberTextFieldState(initialText = url)

    // Sync from parent when URL changes externally (async prefs load)
    LaunchedEffect(url) {
      if (textState.text.toString() != url) {
        textState.edit { replace(0, length, url) }
      }
    }

    // Feed user edits to parent
    LaunchedEffect(textState) {
      snapshotFlow { textState.text.toString() }
        .collect { text -> onAction(EnterUrl(text.lowercase())) }
    }

    AktualTextField(
      modifier = Modifier.weight(1f).focusRequester(keyboardFocusRequester(keyboard)),
      state = textState,
      placeholderText = EXAMPLE_URL,
      keyboardOptions =
        KeyboardOptions(
          autoCorrectEnabled = false,
          capitalization = KeyboardCapitalization.None,
          keyboardType = KeyboardType.Uri,
          imeAction = ImeAction.Go,
        ),
      onKeyboardAction = { _ ->
        keyboard?.hide()
        onAction(ConfirmUrl)
      },
    )
  }
}

private val PROTOCOLS = Protocol.entries.map { it.toString() }.toImmutableList()

private const val EXAMPLE_URL = "example.com"

@Preview
@Composable
private fun PreviewInputFields(
  @PreviewParameter(InputFieldsProvider::class) params: ThemedParams<InputFieldsParams>
) =
  PreviewWithTheme(params.theme) {
    InputFields(url = params.data.url, protocol = params.data.protocol, onAction = {})
  }

private data class InputFieldsParams(val url: String, val protocol: Protocol)

private class InputFieldsProvider :
  ThemedParameterProvider<InputFieldsParams>(
    InputFieldsParams(url = "", protocol = Protocol.Http),
    InputFieldsParams(url = "my.server.com:1234/path", protocol = Protocol.Https),
  )
