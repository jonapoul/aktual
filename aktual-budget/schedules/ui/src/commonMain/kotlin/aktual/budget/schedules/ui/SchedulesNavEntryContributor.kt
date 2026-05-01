package aktual.budget.schedules.ui

import aktual.app.nav.AktualNavStack
import aktual.app.nav.BudgetNavEntryContributor
import aktual.app.nav.BudgetNavKey
import aktual.app.nav.BudgetNavScope
import aktual.app.nav.EditScheduleNavigator
import aktual.app.nav.ListSchedulesNavRoute
import aktual.budget.schedules.ui.list.ListSchedulesScreen
import androidx.navigation3.runtime.EntryProviderScope
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(BudgetNavScope::class)
class SchedulesNavEntryContributor : BudgetNavEntryContributor {
  override fun contribute(
    scope: EntryProviderScope<BudgetNavKey>,
    stack: AktualNavStack<BudgetNavKey>,
  ) {
    scope.entry<ListSchedulesNavRoute> { ListSchedulesScreen(EditScheduleNavigator(stack)) }
  }
}
