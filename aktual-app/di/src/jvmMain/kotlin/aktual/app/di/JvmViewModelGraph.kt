/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.app.di

import aktual.core.di.ViewModelGraph
import aktual.core.di.ViewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Provides

@GraphExtension(ViewModelScope::class)
interface JvmViewModelGraph : ViewModelGraph {
  @GraphExtension.Factory
  fun interface Factory : ViewModelGraph.Factory {
    override fun create(
      @Provides extras: CreationExtras,
    ): JvmViewModelGraph
  }
}
