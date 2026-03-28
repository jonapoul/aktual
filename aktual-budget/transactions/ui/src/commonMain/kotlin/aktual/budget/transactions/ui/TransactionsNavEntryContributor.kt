package aktual.budget.transactions.ui

import aktual.app.nav.BackNavigator
import aktual.app.nav.BudgetNavEntryContributor
import aktual.app.nav.BudgetNavKey
import aktual.app.nav.BudgetNavScope
import aktual.app.nav.TransactionsNavRoute
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.EntryProviderScope
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(BudgetNavScope::class)
class TransactionsNavEntryContributor : BudgetNavEntryContributor {
  override fun contribute(
    scope: EntryProviderScope<BudgetNavKey>,
    stack: SnapshotStateList<BudgetNavKey>,
  ) {
    scope.entry<TransactionsNavRoute> { route ->
      TransactionsScreen(BackNavigator(stack), route.budgetId, route.token)
    }
  }
}
