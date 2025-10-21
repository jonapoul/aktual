/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.preview

import aktual.account.domain.LoginResult
import aktual.account.ui.login.LoginFailureText
import aktual.core.ui.PreviewThemedColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun PreviewInvalidPassword() = PreviewThemedColumn {
  LoginFailureText(LoginResult.InvalidPassword)
}

@Preview
@Composable
private fun PreviewHttpFailure() = PreviewThemedColumn {
  LoginFailureText(LoginResult.HttpFailure(code = 404, message = "Resource not found"))
}

@Preview
@Composable
private fun PreviewNetworkFailure() = PreviewThemedColumn {
  LoginFailureText(LoginResult.NetworkFailure(reason = "Network problem"))
}

@Preview
@Composable
private fun PreviewOtherFailure() = PreviewThemedColumn {
  LoginFailureText(LoginResult.OtherFailure("Something broke"))
}
