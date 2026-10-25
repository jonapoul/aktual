package aktual.budget.rules.ui

import aktual.budget.rules.ui.edit.EditRuleScreen
import aktual.budget.rules.ui.list.ListRulesScreen
import aktual.budget.rules.ui.search.SearchRulesScreen
import aktual.core.nav.BackNavigator
import aktual.core.nav.BudgetEntryScope
import aktual.core.nav.BudgetNavEntryContributor
import aktual.core.nav.BudgetNavKey
import aktual.core.nav.CreateRuleNavRoute
import aktual.core.nav.EditRuleNavRoute
import aktual.core.nav.EditRuleNavigator
import aktual.core.nav.ListRulesNavRoute
import aktual.core.nav.NavStack
import aktual.core.nav.SearchRulesNavRoute
import aktual.core.nav.SearchRulesNavigator
import aktual.di.BudgetScope
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(BudgetScope::class)
class RulesNavEntryContributor : BudgetNavEntryContributor {
  override fun BudgetEntryScope.contribute(
    stack: NavStack<BudgetNavKey>,
    appStack: NavStack<NavKey>,
  ) {
    budgetEntry<ListRulesNavRoute> {
      ListRulesScreen(editRule = EditRuleNavigator(stack), toSearch = SearchRulesNavigator(stack))
    }

    budgetEntry<SearchRulesNavRoute> {
      SearchRulesScreen(back = BackNavigator(stack), editRule = EditRuleNavigator(stack))
    }

    budgetEntry<EditRuleNavRoute> { route ->
      EditRuleScreen(id = route.id, back = BackNavigator(stack))
    }

    budgetEntry<CreateRuleNavRoute> { EditRuleScreen(id = null, back = BackNavigator(stack)) }
  }
}
