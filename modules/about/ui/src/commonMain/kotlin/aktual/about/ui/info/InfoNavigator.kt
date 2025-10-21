/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.about.ui.info

import androidx.compose.runtime.Immutable

@Immutable
interface InfoNavigator {
  fun back(): Boolean
  fun toLicenses()
}
