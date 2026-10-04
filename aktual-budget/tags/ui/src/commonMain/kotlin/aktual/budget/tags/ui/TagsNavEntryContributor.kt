package aktual.budget.tags.ui

import aktual.budget.tags.ui.edit.EditTagScreen
import aktual.budget.tags.ui.list.ListTagsScreen
import aktual.budget.tags.ui.search.SearchTagsScreen
import aktual.core.nav.BackNavigator
import aktual.core.nav.BudgetEntryScope
import aktual.core.nav.BudgetNavEntryContributor
import aktual.core.nav.BudgetNavKey
import aktual.core.nav.CreateTagNavRoute
import aktual.core.nav.EditTagNavRoute
import aktual.core.nav.EditTagNavigator
import aktual.core.nav.ListTagsNavRoute
import aktual.core.nav.NavStack
import aktual.core.nav.SearchTagsNavRoute
import aktual.core.nav.SearchTagsNavigator
import aktual.core.nav.TransactionsNavigator
import aktual.di.BudgetScope
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(BudgetScope::class)
class TagsNavEntryContributor : BudgetNavEntryContributor {
  override fun BudgetEntryScope.contribute(
    stack: NavStack<BudgetNavKey>,
    appStack: NavStack<NavKey>,
  ) {
    budgetEntry<ListTagsNavRoute> {
      ListTagsScreen(
        toEdit = EditTagNavigator(stack),
        toTransactions = TransactionsNavigator(stack),
        toSearch = SearchTagsNavigator(stack),
      )
    }

    budgetEntry<SearchTagsNavRoute> {
      SearchTagsScreen(back = BackNavigator(stack), toEdit = EditTagNavigator(stack))
    }

    budgetEntry<CreateTagNavRoute> { EditTagScreen(id = null, back = BackNavigator(stack)) }

    budgetEntry<EditTagNavRoute> { route ->
      EditTagScreen(id = route.id, back = BackNavigator(stack))
    }
  }
}
