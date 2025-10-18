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
package actual.account.ui.login

import actual.account.domain.LoginResult
import actual.core.ui.LocalTheme
import actual.core.ui.Theme
import actual.l10n.Strings
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
