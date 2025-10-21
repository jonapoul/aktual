/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.app.desktop

import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner

internal class JvmViewModelStoreOwner : ViewModelStoreOwner {
  override val viewModelStore = ViewModelStore()
}
