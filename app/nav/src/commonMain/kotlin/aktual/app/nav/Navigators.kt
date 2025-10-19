/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package aktual.app.nav

import aktual.about.ui.info.InfoNavigator
import aktual.about.ui.licenses.LicensesNavigator
import aktual.account.ui.login.LoginNavigator
import aktual.account.ui.password.ChangePasswordNavigator
import aktual.account.ui.url.ServerUrlNavigator
import aktual.budget.list.ui.ListBudgetsNavigator
import aktual.budget.model.BudgetId
import aktual.budget.model.WidgetId
import aktual.budget.reports.ui.ChooseReportTypeNavigator
import aktual.budget.reports.ui.ReportsDashboardNavigator
import aktual.budget.sync.ui.SyncBudgetNavigator
import aktual.budget.transactions.ui.TransactionsNavigator
import aktual.core.model.LoginToken
import aktual.settings.ui.SettingsNavigator
import androidx.navigation.NavHostController

fun ChangePasswordNavigator(nav: NavHostController): ChangePasswordNavigator = ChangePasswordNavigatorImpl(nav)
fun ChooseReportTypeNavigator(nav: NavHostController): ChooseReportTypeNavigator = ChooseReportTypeNavigatorImpl(nav)
fun InfoNavigator(nav: NavHostController): InfoNavigator = InfoNavigatorImpl(nav)
fun LicensesNavigator(nav: NavHostController): LicensesNavigator = LicensesNavigatorImpl(nav)
fun ListBudgetsNavigator(nav: NavHostController): ListBudgetsNavigator = ListBudgetsNavigatorImpl(nav)
fun LoginNavigator(nav: NavHostController): LoginNavigator = LoginNavigatorImpl(nav)
fun ReportsDashboardNavigator(nav: NavHostController): ReportsDashboardNavigator = ReportsDashboardNavigatorImpl(nav)
fun ServerUrlNavigator(nav: NavHostController): ServerUrlNavigator = ServerUrlNavigatorImpl(nav)
fun SettingsNavigator(nav: NavHostController): SettingsNavigator = SettingsNavigatorImpl(nav)
fun SyncBudgetNavigator(nav: NavHostController): SyncBudgetNavigator = SyncBudgetNavigatorImpl(nav)
fun TransactionsNavigator(nav: NavHostController): TransactionsNavigator = TransactionsNavigatorImpl(nav)

private class ChangePasswordNavigatorImpl(private val nav: NavHostController) : ChangePasswordNavigator {
  override fun back() = nav.popBackStack()
  override fun toListBudgets(token: LoginToken) = nav.debugNav(ListBudgetsNavRoute(token))
}

private class InfoNavigatorImpl(private val nav: NavHostController) : InfoNavigator {
  override fun back() = nav.popBackStack()
  override fun toLicenses() = nav.debugNav(LicensesNavRoute)
}

private class ListBudgetsNavigatorImpl(private val nav: NavHostController) : ListBudgetsNavigator {
  override fun toAbout() = nav.debugNav(InfoNavRoute)
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

private class ReportsDashboardNavigatorImpl(private val nav: NavHostController) : ReportsDashboardNavigator {
  override fun back() = nav.popBackStack()

  override fun toReport(token: LoginToken, budgetId: BudgetId, widgetId: WidgetId) =
    nav.debugNav(ReportNavRoute(token, budgetId, widgetId))

  override fun createReport(token: LoginToken, budgetId: BudgetId) =
    nav.debugNav(CreateReportNavRoute(token, budgetId))
}

private class ChooseReportTypeNavigatorImpl(private val nav: NavHostController) : ChooseReportTypeNavigator {
  override fun back() = nav.popBackStack()

  override fun toReport(token: LoginToken, budgetId: BudgetId, widgetId: WidgetId) =
    nav.debugNav(ReportNavRoute(token, budgetId, widgetId))
}

private class SettingsNavigatorImpl(private val nav: NavHostController) : SettingsNavigator {
  override fun back() = nav.popBackStack()
}

private class SyncBudgetNavigatorImpl(private val nav: NavHostController) : SyncBudgetNavigator {
  override fun back() = nav.popBackStack()
  override fun toBudget(token: LoginToken, budgetId: BudgetId) = nav.debugNav(TransactionsNavRoute(token, budgetId))
}

private class TransactionsNavigatorImpl(private val nav: NavHostController) : TransactionsNavigator {
  override fun back() = nav.popBackStack()
}
