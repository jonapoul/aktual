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
import aktual.core.ui.LoadingScreenIfNotNull
import aktual.di.AppScope
import aktual.di.RunLevelState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(AppScope::class)
class AccountNavEntryContributor(private val runLevelState: RunLevelState) : NavEntryContributor {
  override fun EntryProviderScope<NavKey>.contribute(stack: NavStack<NavKey>) {
    entry<ChangePasswordNavRoute> {
      ChangePasswordScreen(
        navBack = BackNavigator(stack),
        toListBudgets = ListBudgetsNavigator(stack),
      )
    }

    entry<LoginNavRoute> {
      val serverChosenGraph by remember {
        runLevelState.serverChosen()
      }
        .collectAsState(initial = null)

      LoadingScreenIfNotNull(serverChosenGraph) {
        LoginScreen(
          back = BackNavigator(stack),
          toServerUrl = ServerUrlNavigator(stack),
          toListBudgets = ListBudgetsNavigator(stack),
        )
      }
    }

    entry<ServerUrlNavRoute> {
      ServerUrlScreen(toLogin = LoginNavigator(stack), toInfo = InfoNavigator(stack))
    }
  }
}
