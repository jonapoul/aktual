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
package aktual.account.ui.password

import aktual.core.model.Password
import aktual.core.ui.TextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

@Composable
internal fun PasswordEntryText(
  password: Password,
  placeholderText: String,
  showPassword: Boolean,
  onValueChange: (Password) -> Unit,
  modifier: Modifier = Modifier,
  imeAction: ImeAction = ImeAction.Go,
  onGo: (() -> Unit)? = null,
) {
  TextField(
    modifier = modifier,
    value = password.toString(),
    onValueChange = { value -> onValueChange(Password(value)) },
    placeholderText = placeholderText,
    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
    keyboardOptions = KeyboardOptions(
      autoCorrectEnabled = false,
      capitalization = KeyboardCapitalization.None,
      keyboardType = KeyboardType.Password,
      imeAction = imeAction,
    ),
    keyboardActions = if (onGo == null) KeyboardActions.Default else KeyboardActions(onGo = { onGo() }),
  )
}
