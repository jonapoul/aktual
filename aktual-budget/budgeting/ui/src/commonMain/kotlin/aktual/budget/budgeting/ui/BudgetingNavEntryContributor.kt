package aktual.budget.budgeting.ui

import aktual.core.nav.BackNavigator
import aktual.core.nav.BudgetCategoryNavRoute
import aktual.core.nav.BudgetCategoryNavigator
import aktual.core.nav.BudgetEntryScope
import aktual.core.nav.BudgetNavEntryContributor
import aktual.core.nav.BudgetNavKey
import aktual.core.nav.BudgetNavRoute
import aktual.core.nav.NavStack
import aktual.core.nav.TransactionsNavigator
import aktual.di.BudgetScope
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(BudgetScope::class)
class BudgetingNavEntryContributor : BudgetNavEntryContributor {
  override fun BudgetEntryScope.contribute(
    stack: NavStack<BudgetNavKey>,
    appStack: NavStack<NavKey>,
  ) {
    budgetEntry<BudgetNavRoute> { route ->
      BudgetScreen(
        month = route.month,
        transactions = TransactionsNavigator(stack),
        categories = BudgetCategoryNavigator(stack),
      )
    }

    budgetEntry<BudgetCategoryNavRoute> { route ->
      BudgetCategoryScreen(
        category = route.category,
        month = route.month,
        back = BackNavigator(stack),
        transactions = TransactionsNavigator(stack),
      )
    }
  }
}
