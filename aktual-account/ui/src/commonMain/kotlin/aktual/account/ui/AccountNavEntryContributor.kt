package aktual.account.ui

import aktual.account.ui.login.LoginScreen
import aktual.account.ui.password.ChangePasswordScreen
import aktual.account.ui.url.ServerUrlScreen
import aktual.app.nav.AktualNavStack
import aktual.app.nav.BackNavigator
import aktual.app.nav.ChangePasswordNavRoute
import aktual.app.nav.InfoNavigator
import aktual.app.nav.ListBudgetsNavigator
import aktual.app.nav.LoginNavRoute
import aktual.app.nav.LoginNavigator
import aktual.app.nav.NavEntryContributor
import aktual.app.nav.NavScope
import aktual.app.nav.ServerUrlNavRoute
import aktual.app.nav.ServerUrlNavigator
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(NavScope::class)
class AccountNavEntryContributor : NavEntryContributor {
  override fun contribute(scope: EntryProviderScope<NavKey>, stack: AktualNavStack<NavKey>) {
    scope.entry<ChangePasswordNavRoute> {
      ChangePasswordScreen(
        navBack = BackNavigator(stack),
        navToListBudgets = ListBudgetsNavigator(stack),
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
