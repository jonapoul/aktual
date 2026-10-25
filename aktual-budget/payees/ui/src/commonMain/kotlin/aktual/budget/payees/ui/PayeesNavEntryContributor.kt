package aktual.budget.payees.ui

import aktual.budget.payees.ui.list.ListPayeesScreen
import aktual.budget.payees.ui.search.SearchPayeesScreen
import aktual.core.nav.BackNavigator
import aktual.core.nav.BudgetEntryScope
import aktual.core.nav.BudgetNavEntryContributor
import aktual.core.nav.BudgetNavKey
import aktual.core.nav.ListPayeesNavRoute
import aktual.core.nav.NavStack
import aktual.core.nav.SearchPayeesNavRoute
import aktual.core.nav.SearchPayeesNavigator
import aktual.di.BudgetScope
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(BudgetScope::class)
class PayeesNavEntryContributor : BudgetNavEntryContributor {
  override fun BudgetEntryScope.contribute(
    stack: NavStack<BudgetNavKey>,
    appStack: NavStack<NavKey>,
  ) {
    budgetEntry<ListPayeesNavRoute> { ListPayeesScreen(toSearch = SearchPayeesNavigator(stack)) }

    budgetEntry<SearchPayeesNavRoute> { SearchPayeesScreen(back = BackNavigator(stack)) }
  }
}
