package aktual.budget.banksync.ui

import aktual.budget.model.AccountSyncSource
import aktual.core.nav.BackNavigator
import aktual.core.nav.BankSyncNavRoute
import aktual.core.nav.BankSyncProviderSetupNavRoute
import aktual.core.nav.BankSyncProviderSetupNavigator
import aktual.core.nav.BankSyncProvidersNavRoute
import aktual.core.nav.BankSyncProvidersNavigator
import aktual.core.nav.BankSyncSettingsNavRoute
import aktual.core.nav.BankSyncSettingsNavigator
import aktual.core.nav.BudgetEntryScope
import aktual.core.nav.BudgetNavEntryContributor
import aktual.core.nav.BudgetNavKey
import aktual.core.nav.LinkBankAccountNavRoute
import aktual.core.nav.LinkBankAccountNavigator
import aktual.core.nav.NavStack
import aktual.di.BudgetScope
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(BudgetScope::class)
class BankSyncNavEntryContributor : BudgetNavEntryContributor {
  override fun BudgetEntryScope.contribute(
    stack: NavStack<BudgetNavKey>,
    appStack: NavStack<NavKey>,
  ) {
    budgetEntry<BankSyncNavRoute> {
      BankSyncScreen(
        settings = BankSyncSettingsNavigator(stack),
        link = LinkBankAccountNavigator(stack),
        providers = BankSyncProvidersNavigator(stack),
      )
    }
    budgetEntry<BankSyncSettingsNavRoute> { route ->
      BankSyncSettingsScreen(id = route.id, back = BackNavigator(stack))
    }
    budgetEntry<LinkBankAccountNavRoute> { route ->
      LinkBankAccountScreen(
        id = route.id,
        back = BackNavigator(stack),
        providers = BankSyncProvidersNavigator(stack),
      )
    }
    budgetEntry<BankSyncProvidersNavRoute> {
      BankSyncProvidersScreen(
        back = BackNavigator(stack),
        setUp = BankSyncProviderSetupNavigator(stack),
      )
    }
    budgetEntry<BankSyncProviderSetupNavRoute> { route ->
      BankSyncProviderSetupScreen(
        source = AccountSyncSource.fromString(route.source),
        back = BackNavigator(stack),
      )
    }
  }
}
