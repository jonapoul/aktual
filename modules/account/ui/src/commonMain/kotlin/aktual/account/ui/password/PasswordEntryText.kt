/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
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
