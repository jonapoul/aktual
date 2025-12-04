/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.core.di

import dev.zacsweers.metrox.viewmodel.ViewModelGraph

interface AppGraph : BudgetGraph.Factory, ViewModelGraph
