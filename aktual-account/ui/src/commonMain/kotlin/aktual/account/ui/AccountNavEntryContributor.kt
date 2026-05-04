package aktual.account.ui

import aktual.account.ui.login.LoginScreen
import aktual.account.ui.password.ChangePasswordScreen
import aktual.account.ui.url.ServerUrlScreen
import aktual.core.nav.BackNavigator
import aktual.core.nav.ChangePasswordNavRoute
import aktual.core.nav.InfoNavigator
import aktual.core.nav.ListBudgetsNavigator
import aktual.core.nav.LoginNavRoute
import aktual.core.nav.LoginNavigator
import aktual.core.nav.NavEntryContributor
import aktual.core.nav.NavStack
import aktual.core.nav.ServerUrlNavRoute
import aktual.core.nav.ServerUrlNavigator
import aktual.di.AppScope
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(AppScope::class)
class AccountNavEntryContributor : NavEntryContributor {
  override fun contribute(scope: EntryProviderScope<NavKey>, stack: NavStack<NavKey>) {
    scope.entry<ChangePasswordNavRoute> {
      ChangePasswordScreen(
        navBack = BackNavigator(stack),
        toListBudgets = ListBudgetsNavigator(stack),
      )
    }

    scope.entry<LoginNavRoute> {
      LoginScreen(
        back = BackNavigator(stack),
        toServerUrl = ServerUrlNavigator(stack),
        toListBudgets = ListBudgetsNavigator(stack),
      )
    }

    scope.entry<ServerUrlNavRoute> {
      ServerUrlScreen(toLogin = LoginNavigator(stack), toInfo = InfoNavigator(stack))
    }
  }
}
