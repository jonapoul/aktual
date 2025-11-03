/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.app.di

import aktual.budget.model.AndroidBudgetFiles
import aktual.budget.model.BudgetFiles
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.ContributesTo

@BindingContainer
@ContributesTo(AppScope::class)
interface AndroidBudgetFilesContainer {
  @Binds val AndroidBudgetFiles.binds: BudgetFiles
}
