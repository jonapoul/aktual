package aktual.budget.accounts.ui

import aktual.budget.accounts.ui.manage.ManageAccountsScreen
import aktual.core.nav.AccountTransactionsNavRoute
import aktual.core.nav.BackNavigator
import aktual.core.nav.BudgetEntryScope
import aktual.core.nav.BudgetNavEntryContributor
import aktual.core.nav.BudgetNavKey
import aktual.core.nav.CreateAccountNavRoute
import aktual.core.nav.CreateAccountNavigator
import aktual.core.nav.ManageAccountsNavRoute
import aktual.core.nav.NavStack
import aktual.core.nav.TransactionsNavigator
import aktual.di.BudgetScope
import androidx.compose.runtime.remember
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

    budgetEntry<ManageAccountsNavRoute> { route ->
      // Opened from the nav drawer as the only entry of its stack. Remembered so the screen keeps
      // its nav icon while it animates out
      val isRoot = remember(route) { stack.firstOrNull() == route }
      ManageAccountsScreen(
        back = BackNavigator(stack),
        toCreateAccount = CreateAccountNavigator(stack),
        toTransactions = TransactionsNavigator(stack),
        isRoot = isRoot,
      )
    }
  }
}
