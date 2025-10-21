/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.preview

import aktual.account.ui.login.PasswordLogin
import aktual.core.model.Password
import aktual.core.ui.PreviewThemedColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun Loading() = PreviewThemedColumn {
  PasswordLogin(
    isLoading = true,
    enteredPassword = Password.Dummy,
    onAction = {},
  )
}

@Preview
@Composable
private fun Filled() = PreviewThemedColumn {
  PasswordLogin(
    isLoading = false,
    enteredPassword = Password.Dummy,
    onAction = {},
  )
}

@Preview
@Composable
private fun Empty() = PreviewThemedColumn {
  PasswordLogin(
    isLoading = false,
    enteredPassword = Password.Empty,
    onAction = {},
  )
}
