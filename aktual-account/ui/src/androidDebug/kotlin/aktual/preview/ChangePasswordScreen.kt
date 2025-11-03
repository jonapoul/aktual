/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.preview

import aktual.account.ui.password.ChangePasswordScaffold
import aktual.account.vm.ChangePasswordState
import aktual.core.model.AktualVersions
import aktual.core.model.Password
import aktual.core.ui.PreviewThemedScreen
import aktual.core.ui.TripleScreenPreview
import androidx.compose.runtime.Composable

@TripleScreenPreview
@Composable
private fun Regular() = PreviewThemedScreen {
  ChangePasswordScaffold(
    inputPassword1 = Password.Empty,
    inputPassword2 = Password.Empty,
    showPasswords = false,
    passwordsMatch = false,
    state = null,
    versions = AktualVersions.Dummy,
    onAction = {},
  )
}

@TripleScreenPreview
@Composable
private fun Loading() = PreviewThemedScreen {
  ChangePasswordScaffold(
    inputPassword1 = Password.Empty,
    inputPassword2 = Password.Empty,
    showPasswords = false,
    passwordsMatch = false,
    state = ChangePasswordState.Loading,
    versions = AktualVersions.Dummy,
    onAction = {},
  )
}

@TripleScreenPreview
@Composable
private fun Success() = PreviewThemedScreen {
  ChangePasswordScaffold(
    inputPassword1 = Password.Empty,
    inputPassword2 = Password.Empty,
    showPasswords = false,
    passwordsMatch = false,
    state = ChangePasswordState.Success,
    versions = AktualVersions.Dummy,
    onAction = {},
  )
}

@TripleScreenPreview
@Composable
private fun Failure() = PreviewThemedScreen {
  ChangePasswordScaffold(
    inputPassword1 = Password.Dummy,
    inputPassword2 = Password.Dummy,
    showPasswords = false,
    passwordsMatch = true,
    state = ChangePasswordState.NetworkFailure,
    versions = AktualVersions.Dummy,
    onAction = {},
  )
}
