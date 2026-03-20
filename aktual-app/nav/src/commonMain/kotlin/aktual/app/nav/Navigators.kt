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
import aktual.core.model.Token
import aktual.core.theme.Theme
import aktual.metrics.ui.MetricsNavigator
import aktual.settings.ui.inspect.InspectThemeNavigator
import aktual.settings.ui.root.SettingsNavigator
import aktual.settings.ui.theme.ThemeSettingsNavigator
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavKey

fun ChangePasswordNavigator(stack: SnapshotStateList<NavKey>): ChangePasswordNavigator =
  ChangePasswordNavigatorImpl(stack)

fun ChooseReportTypeNavigator(stack: SnapshotStateList<NavKey>): ChooseReportTypeNavigator =
  ChooseReportTypeNavigatorImpl(stack)

fun InfoNavigator(stack: SnapshotStateList<NavKey>): InfoNavigator = InfoNavigatorImpl(stack)

fun LicensesNavigator(stack: SnapshotStateList<NavKey>): LicensesNavigator =
  LicensesNavigatorImpl(stack)

fun ListBudgetsNavigator(stack: SnapshotStateList<NavKey>): ListBudgetsNavigator =
  ListBudgetsNavigatorImpl(stack)

fun LoginNavigator(stack: SnapshotStateList<NavKey>): LoginNavigator = LoginNavigatorImpl(stack)

fun MetricsNavigator(stack: SnapshotStateList<NavKey>): MetricsNavigator =
  MetricsNavigatorImpl(stack)

fun ReportsDashboardNavigator(stack: SnapshotStateList<NavKey>): ReportsDashboardNavigator =
  ReportsDashboardNavigatorImpl(stack)

fun ServerUrlNavigator(stack: SnapshotStateList<NavKey>): ServerUrlNavigator =
  ServerUrlNavigatorImpl(stack)

fun SettingsNavigator(stack: SnapshotStateList<NavKey>): SettingsNavigator =
  SettingsNavigatorImpl(stack)

fun InspectThemeNavigator(stack: SnapshotStateList<NavKey>): InspectThemeNavigator =
  InspectThemeNavigatorImpl(stack)

fun ThemeSettingsNavigator(stack: SnapshotStateList<NavKey>): ThemeSettingsNavigator =
  ThemeSettingsNavigatorImpl(stack)

fun SyncBudgetNavigator(stack: SnapshotStateList<NavKey>): SyncBudgetNavigator =
  SyncBudgetNavigatorImpl(stack)

fun TransactionsNavigator(stack: SnapshotStateList<NavKey>): TransactionsNavigator =
  TransactionsNavigatorImpl(stack)

private class ChangePasswordNavigatorImpl(private val stack: SnapshotStateList<NavKey>) :
  ChangePasswordNavigator {
  override fun back() = stack.debugPop()

  override fun toListBudgets(token: Token) = stack.debugPush(ListBudgetsNavRoute(token))
}

private class InfoNavigatorImpl(private val stack: SnapshotStateList<NavKey>) : InfoNavigator {
  override fun back() = stack.debugPop()

  override fun toLicenses() = stack.debugPush(LicensesNavRoute)
}

private class ListBudgetsNavigatorImpl(private val stack: SnapshotStateList<NavKey>) :
  ListBudgetsNavigator {
  override fun toAbout() = stack.debugPush(InfoNavRoute)

  override fun toChangePassword() = stack.debugPush(ChangePasswordNavRoute)

  override fun toMetrics() = stack.debugPush(MetricsNavRoute)

  override fun toSettings() = stack.debugPush(SettingsNavRoute)

  override fun toSyncBudget(token: Token, id: BudgetId) =
    stack.debugPush(SyncBudgetsNavRoute(token, id))

  override fun toUrl() =
    stack.debugPopUpToAndPush(ServerUrlNavRoute, { it is LoginNavRoute }, inclusive = true)
}

private class LoginNavigatorImpl(private val stack: SnapshotStateList<NavKey>) : LoginNavigator {
  override fun back() = stack.debugPop()

  override fun toUrl() =
    stack.debugPopUpToAndPush(ServerUrlNavRoute, { it is LoginNavRoute }, inclusive = true)

  override fun toListBudgets(token: Token) =
    stack.debugPopUpToAndPush(ListBudgetsNavRoute(token), { it is LoginNavRoute }, inclusive = true)
}

private class MetricsNavigatorImpl(private val stack: SnapshotStateList<NavKey>) :
  MetricsNavigator {
  override fun back() = stack.debugPop()
}

private class ServerUrlNavigatorImpl(private val stack: SnapshotStateList<NavKey>) :
  ServerUrlNavigator {
  override fun toLogin() = stack.debugPush(LoginNavRoute)

  override fun toAbout() = stack.debugPush(InfoNavRoute)
}

private class LicensesNavigatorImpl(private val stack: SnapshotStateList<NavKey>) :
  LicensesNavigator {
  override fun back() = stack.debugPop()
}

private class ReportsDashboardNavigatorImpl(private val stack: SnapshotStateList<NavKey>) :
  ReportsDashboardNavigator {
  override fun back() = stack.debugPop()

  override fun toReport(token: Token, budgetId: BudgetId, widgetId: WidgetId) =
    stack.debugPush(ReportNavRoute(token, budgetId, widgetId))

  override fun createReport(token: Token, budgetId: BudgetId) =
    stack.debugPush(CreateReportNavRoute(token, budgetId))
}

private class ChooseReportTypeNavigatorImpl(private val stack: SnapshotStateList<NavKey>) :
  ChooseReportTypeNavigator {
  override fun back() = stack.debugPop()

  override fun toReport(token: Token, budgetId: BudgetId, widgetId: WidgetId) =
    stack.debugPush(ReportNavRoute(token, budgetId, widgetId))
}

private class SettingsNavigatorImpl(private val stack: SnapshotStateList<NavKey>) :
  SettingsNavigator {
  override fun back() = stack.debugPop()

  override fun toThemeSettings() = stack.debugPush(ThemeSettingsNavRoute)
}

private class InspectThemeNavigatorImpl(private val stack: SnapshotStateList<NavKey>) :
  InspectThemeNavigator {
  override fun navBack() = stack.debugPop()
}

private class ThemeSettingsNavigatorImpl(private val stack: SnapshotStateList<NavKey>) :
  ThemeSettingsNavigator {
  override fun back() = stack.debugPop()

  override fun inspectTheme(id: Theme.Id) = stack.debugPush(InspectThemeNavRoute(id))
}

private class SyncBudgetNavigatorImpl(private val stack: SnapshotStateList<NavKey>) :
  SyncBudgetNavigator {
  override fun back() = stack.debugPop()

  override fun toBudget(token: Token, budgetId: BudgetId) =
    stack.debugPopUpToAndPush(
      TransactionsNavRoute(token, budgetId),
      { it is ListBudgetsNavRoute },
      inclusive = false,
    )
}

private class TransactionsNavigatorImpl(private val stack: SnapshotStateList<NavKey>) :
  TransactionsNavigator {
  override fun back() = stack.debugPop()
}
