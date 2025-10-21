/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.account.ui.password

import aktual.core.model.LoginToken
import androidx.compose.runtime.Immutable

@Immutable
interface ChangePasswordNavigator {
  fun back(): Boolean
  fun toListBudgets(token: LoginToken)
}
