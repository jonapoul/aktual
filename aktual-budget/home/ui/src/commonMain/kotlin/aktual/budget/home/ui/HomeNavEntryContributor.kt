package aktual.budget.home.ui

import aktual.core.nav.BankSyncNavigator
import aktual.core.nav.BudgetEntryScope
import aktual.core.nav.BudgetNavEntryContributor
import aktual.core.nav.BudgetNavKey
import aktual.core.nav.HomeNavRoute
import aktual.core.nav.NavStack
import aktual.core.nav.TransactionsNavigator
import aktual.di.BudgetScope
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(BudgetScope::class)
class HomeNavEntryContributor : BudgetNavEntryContributor {
  override fun BudgetEntryScope.contribute(
    stack: NavStack<BudgetNavKey>,
    appStack: NavStack<NavKey>,
  ) {
    budgetEntry<HomeNavRoute> {
      HomeScreen(transactions = TransactionsNavigator(stack), bankSync = BankSyncNavigator(stack))
    }
  }
}
