package aktual.budget.accounts.ui

import aktual.core.nav.AccountTransactionsNavRoute
import aktual.core.nav.BackNavigator
import aktual.core.nav.BudgetEntryScope
import aktual.core.nav.BudgetNavEntryContributor
import aktual.core.nav.BudgetNavKey
import aktual.core.nav.CreateAccountNavRoute
import aktual.core.nav.NavStack
import aktual.di.BudgetScope
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(BudgetScope::class)
class AccountsNavEntryContributor : BudgetNavEntryContributor {
  override fun BudgetEntryScope.contribute(
    stack: NavStack<BudgetNavKey>,
    appStack: NavStack<NavKey>,
  ) {
    budgetEntry<CreateAccountNavRoute> {
      CreateAccountScreen(
        back = BackNavigator(stack),
        // Replaces this screen, so back from the new account skips the form
        toAccount = { id ->
          stack.removeAt(stack.lastIndex)
          stack.push(AccountTransactionsNavRoute(id))
        },
      )
    }
  }
}
