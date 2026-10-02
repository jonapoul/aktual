package aktual.budget.schedules.ui

import aktual.budget.schedules.ui.edit.EditScheduleScreen
import aktual.budget.schedules.ui.list.ListSchedulesScreen
import aktual.budget.schedules.ui.search.SearchSchedulesScreen
import aktual.core.nav.BackNavigator
import aktual.core.nav.BudgetNavEntryContributor
import aktual.core.nav.BudgetNavKey
import aktual.core.nav.CreateScheduleNavRoute
import aktual.core.nav.EditScheduleNavRoute
import aktual.core.nav.EditScheduleNavigator
import aktual.core.nav.ListSchedulesNavRoute
import aktual.core.nav.NavStack
import aktual.core.nav.ScheduleSettingsNavigator
import aktual.core.nav.SearchSchedulesNavRoute
import aktual.core.nav.SearchSchedulesNavigator
import aktual.core.nav.budgetEntry
import aktual.di.BudgetScope
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(BudgetScope::class)
class SchedulesNavEntryContributor : BudgetNavEntryContributor {
  override fun EntryProviderScope<BudgetNavKey>.contribute(
    stack: NavStack<BudgetNavKey>,
    appStack: NavStack<NavKey>,
  ) {
    budgetEntry<ListSchedulesNavRoute> {
      ListSchedulesScreen(
        editSchedule = EditScheduleNavigator(stack),
        toSearch = SearchSchedulesNavigator(stack),
        toSettings = ScheduleSettingsNavigator(appStack),
      )
    }

    budgetEntry<SearchSchedulesNavRoute> {
      SearchSchedulesScreen(
        back = BackNavigator(stack),
        editSchedule = EditScheduleNavigator(stack),
      )
    }

    budgetEntry<EditScheduleNavRoute> { route ->
      EditScheduleScreen(id = route.id, back = BackNavigator(stack))
    }

    budgetEntry<CreateScheduleNavRoute> {
      EditScheduleScreen(id = null, back = BackNavigator(stack))
    }
  }
}
