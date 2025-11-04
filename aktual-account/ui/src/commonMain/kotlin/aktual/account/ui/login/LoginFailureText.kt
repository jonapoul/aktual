/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.account.ui.login

import aktual.account.domain.LoginResult
import aktual.core.ui.LocalTheme
import aktual.core.ui.Theme
import aktual.l10n.Strings
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign

@Stable
@Composable
internal fun LoginFailureText(
  result: LoginResult.Failure,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  val errorMessage = when (result) {
    is LoginResult.InvalidPassword -> Strings.loginFailurePassword
    is LoginResult.HttpFailure -> Strings.loginFailureHttp(result.code, result.message)
    is LoginResult.NetworkFailure -> Strings.loginFailureNetwork(result.reason)
    is LoginResult.OtherFailure -> Strings.loginFailureOther(result.reason)
  }

  Text(
    modifier = modifier.testTag(Tags.LoginFailureText),
    text = errorMessage,
    color = theme.errorText,
    textAlign = TextAlign.Center,
  )
}
