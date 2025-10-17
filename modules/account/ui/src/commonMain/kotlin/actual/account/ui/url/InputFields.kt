/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package actual.account.ui.url

import actual.core.model.Protocol
import actual.core.ui.ExposedDropDownMenu
import actual.core.ui.LocalTheme
import actual.core.ui.TextField
import actual.core.ui.Theme
import actual.core.ui.keyboardFocusRequester
import alakazam.kotlin.core.parse
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
