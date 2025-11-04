/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.account.ui.url

import androidx.compose.runtime.Immutable

@Immutable
interface ServerUrlNavigator {
  fun toLogin()
  fun toAbout()
}
