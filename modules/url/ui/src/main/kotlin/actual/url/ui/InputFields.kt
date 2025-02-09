package actual.url.ui

import actual.core.ui.ExposedDropDownMenu
import actual.core.ui.HorizontalSpacer
import actual.core.ui.PreviewColumn
import actual.core.ui.TextField
import actual.url.model.Protocol
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.toImmutableList

@Composable
internal fun InputFields(
  url: String,
  protocol: Protocol,
  onAction: (ServerUrlAction) -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.fillMaxWidth(),
  ) {
    ExposedDropDownMenu(
      modifier = Modifier.width(110.dp),
      value = protocol.toString(),
      options = PROTOCOLS,
      onValueChange = { onAction(ServerUrlAction.SelectProtocol(Protocol.fromString(it))) },
    )

    HorizontalSpacer(width = 5.dp)

    val focusManager = LocalFocusManager.current

    TextField(
      modifier = Modifier.weight(1f),
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
          focusManager.clearFocus()
          onAction(ServerUrlAction.ConfirmUrl)
        },
      ),
    )
  }
}

private val PROTOCOLS = Protocol.entries.map { it.toString() }.toImmutableList()

private const val EXAMPLE_URL = "example.com"

@Preview
@Composable
private fun Empty() = PreviewColumn {
  InputFields(
    url = "",
    protocol = Protocol.Http,
    onAction = {},
  )
}

@Preview
@Composable
private fun Filled() = PreviewColumn {
  InputFields(
    url = "my.server.com:1234/path",
    protocol = Protocol.Http,
    onAction = {},
  )
}
