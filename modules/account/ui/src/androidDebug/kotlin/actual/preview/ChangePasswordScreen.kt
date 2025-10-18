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
package actual.preview

import actual.account.ui.password.ChangePasswordScaffold
import actual.account.vm.ChangePasswordState
import actual.core.model.ActualVersions
import actual.core.model.Password
import actual.core.ui.PreviewThemedScreen
import actual.core.ui.TripleScreenPreview
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
    versions = ActualVersions.Dummy,
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
    versions = ActualVersions.Dummy,
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
    versions = ActualVersions.Dummy,
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
    versions = ActualVersions.Dummy,
    onAction = {},
  )
}
