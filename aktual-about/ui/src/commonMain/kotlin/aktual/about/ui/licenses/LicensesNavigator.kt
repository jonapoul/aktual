/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.about.ui.licenses

import androidx.compose.runtime.Immutable

@Immutable
fun interface LicensesNavigator {
  fun back(): Boolean
}
