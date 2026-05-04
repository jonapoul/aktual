package aktual.budget.rules.ui

import aktual.budget.rules.ui.edit.EditRuleScreen
import aktual.budget.rules.ui.list.ListRulesScreen
import aktual.core.nav.BackNavigator
import aktual.core.nav.BudgetNavEntryContributor
import aktual.core.nav.BudgetNavKey
import aktual.core.nav.CreateRuleNavRoute
import aktual.core.nav.EditRuleNavRoute
import aktual.core.nav.EditRuleNavigator
import aktual.core.nav.ListRulesNavRoute
import aktual.core.nav.NavStack
import aktual.core.nav.budgetEntry
import aktual.di.BudgetScope
import androidx.navigation3.runtime.EntryProviderScope
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(BudgetScope::class)
class RulesNavEntryContributor : BudgetNavEntryContributor {
  override fun contribute(scope: EntryProviderScope<BudgetNavKey>, stack: NavStack<BudgetNavKey>) {
    scope.budgetEntry<ListRulesNavRoute> { ListRulesScreen(EditRuleNavigator(stack)) }

    scope.budgetEntry<EditRuleNavRoute> { route ->
      EditRuleScreen(id = route.id, back = BackNavigator(stack))
    }

    scope.budgetEntry<CreateRuleNavRoute> { EditRuleScreen(id = null, back = BackNavigator(stack)) }
  }
}
