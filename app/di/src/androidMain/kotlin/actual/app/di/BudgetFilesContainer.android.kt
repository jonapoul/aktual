package actual.app.di

import actual.budget.model.AndroidBudgetFiles
import actual.budget.model.BudgetFiles
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.ContributesTo

@BindingContainer
@ContributesTo(AppScope::class)
actual interface BudgetFilesContainer {
  @Binds val AndroidBudgetFiles.binds: BudgetFiles
}
