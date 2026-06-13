package aktual.budget.tags.ui

import aktual.budget.tags.ui.edit.EditTagScreen
import aktual.budget.tags.ui.list.ListTagsScreen
import aktual.core.nav.BackNavigator
import aktual.core.nav.BudgetNavEntryContributor
import aktual.core.nav.BudgetNavKey
import aktual.core.nav.CreateTagNavRoute
import aktual.core.nav.EditTagNavRoute
import aktual.core.nav.EditTagNavigator
import aktual.core.nav.ListTagsNavRoute
import aktual.core.nav.NavStack
import aktual.core.nav.budgetEntry
import aktual.di.BudgetScope
import androidx.navigation3.runtime.EntryProviderScope
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(BudgetScope::class)
class TagsNavEntryContributor : BudgetNavEntryContributor {
  override fun EntryProviderScope<BudgetNavKey>.contribute(stack: NavStack<BudgetNavKey>) {
    budgetEntry<ListTagsNavRoute> { ListTagsScreen(toEdit = EditTagNavigator(stack)) }

    budgetEntry<CreateTagNavRoute> { EditTagScreen(id = null, back = BackNavigator(stack)) }

    budgetEntry<EditTagNavRoute> { route ->
      EditTagScreen(id = route.id, back = BackNavigator(stack))
    }
  }
}
