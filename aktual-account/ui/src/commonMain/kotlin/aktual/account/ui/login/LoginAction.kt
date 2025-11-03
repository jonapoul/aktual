/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.account.ui.login

import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface LoginAction {
  data object ChangeServer : LoginAction
  data object NavBack : LoginAction
  data object SignIn : LoginAction

  data class EnterPassword(val password: String) : LoginAction
}
