package aktual.account.ui.url

import aktual.core.model.Protocol
import aktual.core.ui.ExposedDropDownMenu
import aktual.core.ui.LocalTheme
import aktual.core.ui.PreviewWithColorScheme
import aktual.core.ui.TextField
import aktual.core.ui.Theme
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import aktual.core.ui.keyboardFocusRequester
import alakazam.kotlin.parse
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
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
  onAction: (ServerUrlAction) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(5.dp),
  ) {
    ExposedDropDownMenu(
      modifier = Modifier.width(110.dp),
      value = protocol.toString(),
      options = PROTOCOLS,
      onValueChange = { onAction(ServerUrlAction.SelectProtocol(Protocol::class.parse(it))) },
      theme = theme,
    )

    val keyboard = LocalSoftwareKeyboardController.current

    TextField(
      modifier = Modifier
        .weight(1f)
        .focusRequester(keyboardFocusRequester(keyboard)),
      value = url,
      onValueChange = { onAction(ServerUrlAction.EnterUrl(it.lowercase())) },
      placeholderText = EXAMPLE_URL,
      keyboardOptions = KeyboardOptions(
        autoCorrectEnabled = false,
        capitalization = KeyboardCapitalization.None,
        keyboardType = KeyboardType.Uri,
        imeAction = ImeAction.Go,
      ),
      keyboardActions = KeyboardActions(
        onGo = {
          keyboard?.hide()
          onAction(ServerUrlAction.ConfirmUrl)
        },
      ),
    )
  }
}

private val PROTOCOLS = Protocol
  .entries
  .map { it.toString() }
  .toImmutableList()

private const val EXAMPLE_URL = "example.com"

@Preview
@Composable
private fun PreviewInputFields(
  @PreviewParameter(InputFieldsProvider::class) params: ThemedParams<InputFieldsParams>,
) = PreviewWithColorScheme(params.type) {
  InputFields(
    url = params.data.url,
    protocol = params.data.protocol,
    onAction = {},
  )
}

private data class InputFieldsParams(
  val url: String,
  val protocol: Protocol,
)

private class InputFieldsProvider : ThemedParameterProvider<InputFieldsParams>(
  InputFieldsParams(url = "", protocol = Protocol.Http),
  InputFieldsParams(url = "my.server.com:1234/path", protocol = Protocol.Https),
)
