package aktual.budget.schedules.ui

import aktual.budget.schedules.ui.list.ListSchedulesScreen
import aktual.core.nav.BudgetNavEntryContributor
import aktual.core.nav.BudgetNavKey
import aktual.core.nav.EditScheduleNavigator
import aktual.core.nav.ListSchedulesNavRoute
import aktual.core.nav.NavStack
import aktual.core.nav.budgetEntry
import aktual.di.BudgetScope
import androidx.navigation3.runtime.EntryProviderScope
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(BudgetScope::class)
class SchedulesNavEntryContributor : BudgetNavEntryContributor {
  override fun contribute(scope: EntryProviderScope<BudgetNavKey>, stack: NavStack<BudgetNavKey>) {
    scope.budgetEntry<ListSchedulesNavRoute> { ListSchedulesScreen(EditScheduleNavigator(stack)) }
  }
}
