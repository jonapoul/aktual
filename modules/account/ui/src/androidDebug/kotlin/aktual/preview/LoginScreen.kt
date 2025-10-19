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
package aktual.preview

import aktual.account.domain.LoginResult
import aktual.account.ui.login.LoginScaffold
import aktual.core.model.AktualVersions
import aktual.core.model.Password
import aktual.core.model.ServerUrl
import aktual.core.ui.PreviewThemedScreen
import aktual.core.ui.TripleScreenPreview
import androidx.compose.runtime.Composable

@TripleScreenPreview
@Composable
private fun Regular() = PreviewThemedScreen {
  LoginScaffold(
    versions = AktualVersions.Dummy,
    enteredPassword = Password.Empty,
    url = ServerUrl.Demo,
    isLoading = false,
    loginFailure = null,
    onAction = {},
  )
}

@TripleScreenPreview
@Composable
private fun WithErrorMessage() = PreviewThemedScreen {
  LoginScaffold(
    versions = AktualVersions.Dummy,
    enteredPassword = Password.Dummy,
    url = ServerUrl("https://this.is.a.long.url.discombobulated.com/aktual/budget/whatever.json"),
    isLoading = true,
    loginFailure = LoginResult.InvalidPassword,
    onAction = {},
  )
}
