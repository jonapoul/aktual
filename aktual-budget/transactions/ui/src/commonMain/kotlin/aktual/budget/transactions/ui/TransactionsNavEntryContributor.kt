package aktual.budget.transactions.ui

import aktual.budget.model.TagSpec
import aktual.budget.model.TransactionsSpec
import aktual.core.nav.BackNavigator
import aktual.core.nav.BudgetNavEntryContributor
import aktual.core.nav.BudgetNavKey
import aktual.core.nav.NavStack
import aktual.core.nav.TransactionsNavRoute
import aktual.core.nav.TransactionsWithTagNavRoute
import aktual.core.nav.budgetEntry
import aktual.di.BudgetScope
import androidx.navigation3.runtime.EntryProviderScope
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(BudgetScope::class)
class TransactionsNavEntryContributor : BudgetNavEntryContributor {
  override fun EntryProviderScope<BudgetNavKey>.contribute(stack: NavStack<BudgetNavKey>) {
    budgetEntry<TransactionsNavRoute> {
      TransactionsScreen(back = BackNavigator(stack), spec = TransactionsSpec())
    }

    budgetEntry<TransactionsWithTagNavRoute> { route ->
      TransactionsScreen(
        back = BackNavigator(stack),
        spec = TransactionsSpec(tagSpec = TagSpec.SpecificTag(route.id)),
      )
    }
  }
}
