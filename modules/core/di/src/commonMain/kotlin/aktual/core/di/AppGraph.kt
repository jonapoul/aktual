/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.core.di

interface AppGraph : BudgetGraph.Factory, ViewModelGraph.Factory {
  val viewModelGraphProvider: ViewModelGraphProvider

  fun interface Holder {
    fun get(): AppGraph
  }
}
