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

import actual.account.ui.password.ConfirmPasswordForm
import actual.account.vm.ChangePasswordState
import actual.core.model.Password
import actual.core.ui.PreviewThemedColumn
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
