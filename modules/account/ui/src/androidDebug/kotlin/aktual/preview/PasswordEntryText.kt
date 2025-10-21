/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.preview

import aktual.account.ui.password.PasswordEntryText
import aktual.core.model.Password
import aktual.core.ui.PreviewThemedColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun EmptyInput() = PreviewThemedColumn {
  PasswordEntryText(
    password = Password(""),
    placeholderText = "Password",
    showPassword = false,
    onValueChange = {},
    onGo = {},
  )
}

@Preview
@Composable
private fun FullInput() = PreviewThemedColumn {
  PasswordEntryText(
    password = Password("abc-123"),
    placeholderText = "Password",
    showPassword = false,
    onValueChange = {},
    onGo = {},
  )
}

@Preview
@Composable
private fun ShowPassword() = PreviewThemedColumn {
  PasswordEntryText(
    password = Password("abc-123"),
    placeholderText = "Password",
    showPassword = true,
    onValueChange = {},
    onGo = {},
  )
}
