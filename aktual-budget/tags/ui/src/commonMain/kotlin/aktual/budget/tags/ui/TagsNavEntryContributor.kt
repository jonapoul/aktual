package aktual.budget.tags.ui

import aktual.budget.tags.ui.list.ListTagsScreen
import aktual.core.nav.BudgetNavEntryContributor
import aktual.core.nav.BudgetNavKey
import aktual.core.nav.ListTagsNavRoute
import aktual.core.nav.NavStack
import aktual.core.nav.budgetEntry
import aktual.di.BudgetScope
import androidx.navigation3.runtime.EntryProviderScope
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(BudgetScope::class)
class TagsNavEntryContributor : BudgetNavEntryContributor {
  override fun contribute(scope: EntryProviderScope<BudgetNavKey>, stack: NavStack<BudgetNavKey>) {
    scope.budgetEntry<ListTagsNavRoute> { ListTagsScreen() }
  }
}
