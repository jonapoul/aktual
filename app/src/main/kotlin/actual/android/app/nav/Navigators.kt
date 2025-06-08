package actual.android.app.nav

import actual.about.info.ui.InfoNavigator
import actual.about.licenses.ui.LicensesNavigator
import actual.account.login.ui.LoginNavigator
import actual.account.model.LoginToken
import actual.account.password.ui.ChangePasswordNavigator
import actual.budget.list.ui.ListBudgetsNavigator
import actual.budget.model.BudgetId
import actual.budget.sync.ui.SyncBudgetNavigator
import actual.settings.ui.SettingsNavigator
import actual.url.ui.ServerUrlNavigator
import alakazam.kotlin.logging.Logger
import androidx.navigation.NavHostController

fun ChangePasswordNavigator(nav: NavHostController): ChangePasswordNavigator = ChangePasswordNavigatorImpl(nav)
fun InfoNavigator(nav: NavHostController): InfoNavigator = InfoNavigatorImpl(nav)
fun ListBudgetsNavigator(nav: NavHostController): ListBudgetsNavigator = ListBudgetsNavigatorImpl(nav)
fun LicensesNavigator(nav: NavHostController): LicensesNavigator = LicensesNavigatorImpl(nav)
fun LoginNavigator(nav: NavHostController): LoginNavigator = LoginNavigatorImpl(nav)
fun ServerUrlNavigator(nav: NavHostController): ServerUrlNavigator = ServerUrlNavigatorImpl(nav)
fun SettingsNavigator(nav: NavHostController): SettingsNavigator = SettingsNavigatorImpl(nav)
fun SyncBudgetNavigator(nav: NavHostController): SyncBudgetNavigator = SyncBudgetNavigatorImpl(nav)

private class ChangePasswordNavigatorImpl(private val nav: NavHostController) : ChangePasswordNavigator {
  override fun back() = nav.popBackStack()
  override fun toListBudgets(token: LoginToken) = nav.debugNav(ListBudgetsNavRoute)
}

private class InfoNavigatorImpl(private val nav: NavHostController) : InfoNavigator {
  override fun back() = nav.popBackStack()
  override fun toLicenses() = nav.debugNav(LicensesNavRoute)
}

private class ListBudgetsNavigatorImpl(private val nav: NavHostController) : ListBudgetsNavigator {
  override fun toChangePassword() = nav.debugNav(ChangePasswordNavRoute)
  override fun toSettings() = nav.debugNav(SettingsNavRoute)
  override fun toSyncBudget(token: LoginToken, id: BudgetId) = nav.debugNav(SyncBudgetsNavRoute(token, id))
  override fun toUrl() = nav.debugNav(ServerUrlNavRoute) { popUpTo(LoginNavRoute) { inclusive = true } }
}

private class LoginNavigatorImpl(private val nav: NavHostController) : LoginNavigator {
  override fun back() = nav.popBackStack()
  override fun toUrl() = nav.debugNav(ServerUrlNavRoute) { popUpTo(LoginNavRoute) { inclusive = true } }
  override fun toListBudgets(token: LoginToken) =
    nav.debugNav(ListBudgetsNavRoute(token)) { popUpTo(LoginNavRoute) { inclusive = true } }
}

private class ServerUrlNavigatorImpl(private val nav: NavHostController) : ServerUrlNavigator {
  override fun toLogin() = nav.debugNav(LoginNavRoute)
  override fun toAbout() = nav.debugNav(InfoNavRoute)
}

private class LicensesNavigatorImpl(private val nav: NavHostController) : LicensesNavigator {
  override fun back() = nav.popBackStack()
}

private class SettingsNavigatorImpl(private val nav: NavHostController) : SettingsNavigator {
  override fun back() = nav.popBackStack()
}

private class SyncBudgetNavigatorImpl(private val nav: NavHostController) : SyncBudgetNavigator {
  override fun back() = nav.popBackStack()
  override fun toBudget(token: LoginToken, budgetId: BudgetId) {
    Logger.w("Not implemented yet! nav=$nav, token=$token, budgetId=$budgetId")
  }
}
