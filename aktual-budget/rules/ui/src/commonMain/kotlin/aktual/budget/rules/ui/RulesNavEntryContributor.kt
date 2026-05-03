package aktual.budget.rules.ui

import aktual.app.nav.AktualNavStack
import aktual.app.nav.BackNavigator
import aktual.app.nav.BudgetNavEntryContributor
import aktual.app.nav.BudgetNavKey
import aktual.app.nav.BudgetNavScope
import aktual.app.nav.CreateRuleNavRoute
import aktual.app.nav.EditRuleNavRoute
import aktual.app.nav.EditRuleNavigator
import aktual.app.nav.ListRulesNavRoute
import aktual.app.nav.budgetEntry
import aktual.budget.rules.ui.edit.EditRuleScreen
import aktual.budget.rules.ui.list.ListRulesScreen
import androidx.navigation3.runtime.EntryProviderScope
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(BudgetNavScope::class)
class RulesNavEntryContributor : BudgetNavEntryContributor {
  override fun contribute(
    scope: EntryProviderScope<BudgetNavKey>,
    stack: AktualNavStack<BudgetNavKey>,
  ) {
    scope.budgetEntry<ListRulesNavRoute> { ListRulesScreen(EditRuleNavigator(stack)) }

    scope.budgetEntry<EditRuleNavRoute> { route ->
      EditRuleScreen(id = route.id, back = BackNavigator(stack))
    }

    scope.budgetEntry<CreateRuleNavRoute> { EditRuleScreen(id = null, back = BackNavigator(stack)) }
  }
}
