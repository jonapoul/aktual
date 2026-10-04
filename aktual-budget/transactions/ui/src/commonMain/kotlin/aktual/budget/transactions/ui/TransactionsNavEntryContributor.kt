package aktual.budget.transactions.ui

import aktual.budget.model.AccountSpec
import aktual.budget.model.TagSpec
import aktual.budget.model.TransactionsSpec
import aktual.core.nav.AccountTransactionsNavRoute
import aktual.core.nav.BackNavigator
import aktual.core.nav.BudgetEntryScope
import aktual.core.nav.BudgetNavEntryContributor
import aktual.core.nav.BudgetNavKey
import aktual.core.nav.NavStack
import aktual.core.nav.TransactionsNavRoute
import aktual.core.nav.TransactionsWithTagNavRoute
import aktual.di.BudgetScope
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(BudgetScope::class)
class TransactionsNavEntryContributor : BudgetNavEntryContributor {
  override fun BudgetEntryScope.contribute(
    stack: NavStack<BudgetNavKey>,
    appStack: NavStack<NavKey>,
  ) {
    budgetEntry<TransactionsNavRoute> {
      TransactionsScreen(back = BackNavigator(stack), spec = TransactionsSpec(), isRoot = true)
    }

    budgetEntry<TransactionsWithTagNavRoute> { route ->
      TransactionsScreen(
        back = BackNavigator(stack),
        spec = TransactionsSpec(tagSpec = TagSpec.SpecificTag(route.id)),
      )
    }

    budgetEntry<AccountTransactionsNavRoute> { route ->
      TransactionsScreen(
        back = BackNavigator(stack),
        spec = TransactionsSpec(accountSpec = AccountSpec.SpecificAccount(route.id)),
      )
    }
  }
}
