package aktual.budget.demo

import aktual.api.client.BankSyncApi
import aktual.api.client.BankSyncApiImpl
import aktual.budget.model.BudgetId
import aktual.di.BudgetScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides

// Replaces BankSyncApiImpl's binding, since the demo budget has no server to sync through
@BindingContainer
@ContributesTo(BudgetScope::class, replaces = [BankSyncApiImpl::class])
object DemoBankSyncContainer {
  @Provides
  fun bankSyncApi(
    budgetId: BudgetId,
    remote: Lazy<BankSyncApiImpl>,
    demo: Lazy<DemoBankSyncApi>,
  ): BankSyncApi = if (budgetId == Demo) demo.value else remote.value
}
