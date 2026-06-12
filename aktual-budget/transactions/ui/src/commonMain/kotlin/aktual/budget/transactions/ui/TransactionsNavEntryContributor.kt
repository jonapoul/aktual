package aktual.budget.transactions.ui

import aktual.core.nav.BackNavigator
import aktual.core.nav.BudgetNavEntryContributor
import aktual.core.nav.BudgetNavKey
import aktual.core.nav.NavStack
import aktual.core.nav.TransactionsNavRoute
import aktual.core.nav.budgetEntry
import aktual.di.BudgetScope
import androidx.navigation3.runtime.EntryProviderScope
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(BudgetScope::class)
class TransactionsNavEntryContributor : BudgetNavEntryContributor {
  override fun EntryProviderScope<BudgetNavKey>.contribute(stack: NavStack<BudgetNavKey>) {
    budgetEntry<TransactionsNavRoute> { TransactionsScreen(BackNavigator(stack)) }
  }
}
