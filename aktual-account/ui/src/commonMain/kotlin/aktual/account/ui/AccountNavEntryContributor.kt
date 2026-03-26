package aktual.account.ui

import aktual.account.ui.login.LoginNavigator
import aktual.account.ui.login.LoginScreen
import aktual.account.ui.password.ChangePasswordNavigator
import aktual.account.ui.password.ChangePasswordScreen
import aktual.account.ui.url.ServerUrlNavigator
import aktual.account.ui.url.ServerUrlScreen
import aktual.app.nav.ChangePasswordNavRoute
import aktual.app.nav.InfoNavRoute
import aktual.app.nav.ListBudgetsNavRoute
import aktual.app.nav.LoginNavRoute
import aktual.app.nav.NavEntryContributor
import aktual.app.nav.NavScope
import aktual.app.nav.ServerUrlNavRoute
import aktual.app.nav.debugPop
import aktual.app.nav.debugPopUpToAndPush
import aktual.app.nav.debugPush
import aktual.core.model.Token
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(NavScope::class)
class AccountNavEntryContributor : NavEntryContributor {
  override fun contribute(scope: EntryProviderScope<NavKey>, stack: SnapshotStateList<NavKey>) {
    scope.entry<ChangePasswordNavRoute> { ChangePasswordScreen(ChangePasswordNavigatorImpl(stack)) }
    scope.entry<LoginNavRoute> { LoginScreen(LoginNavigatorImpl(stack)) }
    scope.entry<ServerUrlNavRoute> { ServerUrlScreen(ServerUrlNavigatorImpl(stack)) }
  }
}

private class ChangePasswordNavigatorImpl(private val stack: SnapshotStateList<NavKey>) :
  ChangePasswordNavigator {
  override fun back() = stack.debugPop()

  override fun toListBudgets(token: Token) = stack.debugPush(ListBudgetsNavRoute(token))
}

private class LoginNavigatorImpl(private val stack: SnapshotStateList<NavKey>) : LoginNavigator {
  override fun back() = stack.debugPop()

  override fun toUrl() =
    stack.debugPopUpToAndPush(ServerUrlNavRoute, { it is LoginNavRoute }, inclusive = true)

  override fun toListBudgets(token: Token) =
    stack.debugPopUpToAndPush(ListBudgetsNavRoute(token), { it is LoginNavRoute }, inclusive = true)
}

private class ServerUrlNavigatorImpl(private val stack: SnapshotStateList<NavKey>) :
  ServerUrlNavigator {
  override fun toLogin() = stack.debugPush(LoginNavRoute)

  override fun toAbout() = stack.debugPush(InfoNavRoute)
}
