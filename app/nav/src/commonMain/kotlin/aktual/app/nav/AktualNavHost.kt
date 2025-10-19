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

import aktual.about.ui.info.InfoScreen
import aktual.about.ui.licenses.LicensesScreen
import aktual.account.ui.login.LoginScreen
import aktual.account.ui.password.ChangePasswordScreen
import aktual.account.ui.url.ServerUrlScreen
import aktual.budget.list.ui.ListBudgetsScreen
import aktual.budget.reports.ui.ChooseReportTypeScreen
import aktual.budget.reports.ui.ReportsDashboardScreen
import aktual.budget.sync.ui.SyncBudgetScreen
import aktual.budget.transactions.ui.TransactionsScreen
import aktual.core.model.LoginToken
import aktual.settings.ui.SettingsScreen
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AktualNavHost(
  isServerUrlSet: Boolean,
  loginToken: LoginToken?,
  modifier: Modifier = Modifier,
  nav: NavHostController = rememberNavController(),
) {
  NavHost(
    modifier = modifier,
    navController = nav,
    startDestination = when {
      loginToken != null && isServerUrlSet -> ListBudgetsNavRoute(loginToken)
      isServerUrlSet -> LoginNavRoute
      else -> ServerUrlNavRoute
    },
  ) {
    composable<ChangePasswordNavRoute> { ChangePasswordScreen(ChangePasswordNavigator(nav)) }

    composable<InfoNavRoute> { InfoScreen(InfoNavigator(nav)) }

    composable<LicensesNavRoute> { LicensesScreen(LicensesNavigator(nav)) }

    composableWithArg<ListBudgetsNavRoute>(mapOf(LoginTokenType)) { route, _ ->
      ListBudgetsScreen(ListBudgetsNavigator(nav), route.token)
    }

    composable<LoginNavRoute> { LoginScreen(LoginNavigator(nav)) }

    composableWithArg<ReportsListNavRoute>(mapOf(BudgetIdType, LoginTokenType)) { route, _ ->
      ReportsDashboardScreen(ReportsDashboardNavigator(nav), route.budgetId, route.token)
    }

    composableWithArg<ReportNavRoute>(mapOf(BudgetIdType, LoginTokenType, WidgetIdType)) { route, _ ->
      // TBC
    }

    composableWithArg<CreateReportNavRoute>(mapOf(BudgetIdType, LoginTokenType)) { route, _ ->
      ChooseReportTypeScreen(ChooseReportTypeNavigator(nav), route.budgetId, route.token)
    }

    composable<ServerUrlNavRoute> { ServerUrlScreen(ServerUrlNavigator(nav)) }

    composable<SettingsNavRoute> { SettingsScreen(SettingsNavigator(nav)) }

    composableWithArg<SyncBudgetsNavRoute>(mapOf(BudgetIdType, LoginTokenType)) { route, _ ->
      SyncBudgetScreen(SyncBudgetNavigator(nav), route.budgetId, route.token)
    }

    composableWithArg<TransactionsNavRoute>(mapOf(BudgetIdType, LoginTokenType)) { route, _ ->
      TransactionsScreen(TransactionsNavigator(nav), route.budgetId, route.token)
    }
  }
}
