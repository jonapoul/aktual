package aktual.budget.transactions.ui

import aktual.budget.model.AccountSpec
import aktual.budget.model.CategorySpec
import aktual.budget.model.FilteredSpec
import aktual.budget.model.TagSpec
import aktual.budget.model.TransactionsSpec
import aktual.budget.transactions.ui.edit.EditTransactionScreen
import aktual.budget.transactions.vm.edit.EditTransactionTarget
import aktual.core.nav.AccountGroupTransactionsNavRoute
import aktual.core.nav.AccountTransactionsNavRoute
import aktual.core.nav.BackNavigator
import aktual.core.nav.BudgetEntryScope
import aktual.core.nav.BudgetNavEntryContributor
import aktual.core.nav.BudgetNavKey
import aktual.core.nav.BudgetNavigator
import aktual.core.nav.CategoryTransactionsNavRoute
import aktual.core.nav.CreateTransactionNavRoute
import aktual.core.nav.EditTransactionNavigator
import aktual.core.nav.FilteredTransactionsNavRoute
import aktual.core.nav.NavStack
import aktual.core.nav.TransactionNavRoute
import aktual.core.nav.TransactionSettingsNavigator
import aktual.core.nav.TransactionsNavRoute
import aktual.core.nav.TransactionsWithTagNavRoute
import aktual.core.nav.UncategorisedTransactionsNavRoute
import aktual.di.BudgetScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(BudgetScope::class)
class TransactionsNavEntryContributor : BudgetNavEntryContributor {
  override fun BudgetEntryScope.contribute(
    stack: NavStack<BudgetNavKey>,
    appStack: NavStack<NavKey>,
  ) {
    budgetEntry<TransactionsNavRoute> {
      TransactionsScreen(
        back = BackNavigator(stack),
        toSettings = TransactionSettingsNavigator(appStack),
        openTransaction = EditTransactionNavigator(stack),
        spec = TransactionsSpec(),
        isRoot = true,
      )
    }

    budgetEntry<TransactionsWithTagNavRoute> { route ->
      TransactionsScreen(
        back = BackNavigator(stack),
        toSettings = TransactionSettingsNavigator(appStack),
        openTransaction = EditTransactionNavigator(stack),
        spec = TransactionsSpec(tagSpec = TagSpec.SpecificTag(route.id)),
      )
    }

    budgetEntry<AccountTransactionsNavRoute> { route ->
      TransactionsScreen(
        back = BackNavigator(stack),
        toSettings = TransactionSettingsNavigator(appStack),
        openTransaction = EditTransactionNavigator(stack),
        spec = TransactionsSpec(accountSpec = AccountSpec.SpecificAccount(route.id)),
        isRoot = stack.isRootedAt(route),
      )
    }

    budgetEntry<AccountGroupTransactionsNavRoute> { route ->
      TransactionsScreen(
        back = BackNavigator(stack),
        toSettings = TransactionSettingsNavigator(appStack),
        openTransaction = EditTransactionNavigator(stack),
        spec = TransactionsSpec(accountSpec = AccountSpec.Group(route.group)),
        isRoot = stack.isRootedAt(route),
      )
    }

    budgetEntry<FilteredTransactionsNavRoute> { route ->
      TransactionsScreen(
        back = BackNavigator(stack),
        toSettings = TransactionSettingsNavigator(appStack),
        openTransaction = EditTransactionNavigator(stack),
        spec = TransactionsSpec(filtered = FilteredSpec(route.title, route.filter)),
      )
    }

    budgetEntry<CategoryTransactionsNavRoute> { route ->
      TransactionsScreen(
        back = BackNavigator(stack),
        toSettings = TransactionSettingsNavigator(appStack),
        openTransaction = EditTransactionNavigator(stack),
        spec =
          TransactionsSpec(
            categorySpec = CategorySpec.SpecificCategory(route.category, route.month),
          ),
      )
    }

    budgetEntry<TransactionNavRoute> { route ->
      EditTransactionScreen(
        target = EditTransactionTarget.Existing(route.id),
        back = BackNavigator(stack),
        toBudget = { BudgetNavigator(stack)() },
      )
    }

    budgetEntry<CreateTransactionNavRoute> { route ->
      EditTransactionScreen(
        target = EditTransactionTarget.New(route.account, route.category),
        back = BackNavigator(stack),
        // The budget takes this screen's place, since it wouldn't see the new categories
        toBudget = {
          stack.pop()
          BudgetNavigator(stack)()
        },
      )
    }

    budgetEntry<UncategorisedTransactionsNavRoute> {
      TransactionsScreen(
        back = BackNavigator(stack),
        toSettings = TransactionSettingsNavigator(appStack),
        openTransaction = EditTransactionNavigator(stack),
        spec = TransactionsSpec(categorySpec = Uncategorised),
      )
    }
  }
}

// The nav drawer opens an account as the only entry of its stack. Remembered so the screen keeps
// its nav icon while it animates out
@Composable
private fun NavStack<BudgetNavKey>.isRootedAt(route: BudgetNavKey): Boolean =
  remember(route) { firstOrNull() == route }
