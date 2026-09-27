package aktual.budget.schedules.ui

import aktual.budget.schedules.ui.list.ListSchedulesScreen
import aktual.budget.schedules.ui.search.SearchSchedulesScreen
import aktual.core.nav.BackNavigator
import aktual.core.nav.BudgetNavEntryContributor
import aktual.core.nav.BudgetNavKey
import aktual.core.nav.EditScheduleNavigator
import aktual.core.nav.ListSchedulesNavRoute
import aktual.core.nav.NavStack
import aktual.core.nav.SearchSchedulesNavRoute
import aktual.core.nav.SearchSchedulesNavigator
import aktual.core.nav.budgetEntry
import aktual.di.BudgetScope
import androidx.navigation3.runtime.EntryProviderScope
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(BudgetScope::class)
class SchedulesNavEntryContributor : BudgetNavEntryContributor {
  override fun EntryProviderScope<BudgetNavKey>.contribute(stack: NavStack<BudgetNavKey>) {
    budgetEntry<ListSchedulesNavRoute> {
      ListSchedulesScreen(
        editSchedule = EditScheduleNavigator(stack),
        toSearch = SearchSchedulesNavigator(stack),
      )
    }

    budgetEntry<SearchSchedulesNavRoute> {
      SearchSchedulesScreen(
        back = BackNavigator(stack),
        editSchedule = EditScheduleNavigator(stack),
      )
    }
  }
}
