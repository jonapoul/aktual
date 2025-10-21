/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.preview

import aktual.account.ui.password.ConfirmPasswordForm
import aktual.account.vm.ChangePasswordState
import aktual.core.model.Password
import aktual.core.ui.PreviewThemedColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun EmptyForm() = PreviewThemedColumn {
  ConfirmPasswordForm(
    inputPassword1 = Password.Empty,
    inputPassword2 = Password.Empty,
    showPasswords = false,
    state = null,
    passwordsMatch = false,
    onAction = {},
  )
}

@Preview
@Composable
private fun ShowPasswords() = PreviewThemedColumn {
  ConfirmPasswordForm(
    inputPassword1 = Password.Dummy,
    inputPassword2 = Password.Dummy,
    showPasswords = true,
    state = null,
    passwordsMatch = true,
    onAction = {},
  )
}

@Preview
@Composable
private fun Loading() = PreviewThemedColumn {
  ConfirmPasswordForm(
    inputPassword1 = Password.Dummy,
    inputPassword2 = Password.Dummy,
    showPasswords = false,
    state = ChangePasswordState.Loading,
    passwordsMatch = true,
    onAction = {},
  )
}
