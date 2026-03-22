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

internal class ChangePasswordNavigatorImpl(private val stack: SnapshotStateList<NavKey>) :
  ChangePasswordNavigator {
  override fun back() = stack.debugPop()

  override fun toListBudgets(token: Token) = stack.debugPush(ListBudgetsNavRoute(token))
}

internal class InfoNavigatorImpl(private val stack: SnapshotStateList<NavKey>) : InfoNavigator {
  override fun back() = stack.debugPop()

  override fun toLicenses() = stack.debugPush(LicensesNavRoute)
}

internal class ListBudgetsNavigatorImpl(private val stack: SnapshotStateList<NavKey>) :
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

internal class LoginNavigatorImpl(private val stack: SnapshotStateList<NavKey>) : LoginNavigator {
  override fun back() = stack.debugPop()

  override fun toUrl() =
    stack.debugPopUpToAndPush(ServerUrlNavRoute, { it is LoginNavRoute }, inclusive = true)

  override fun toListBudgets(token: Token) =
    stack.debugPopUpToAndPush(ListBudgetsNavRoute(token), { it is LoginNavRoute }, inclusive = true)
}

internal class MetricsNavigatorImpl(private val stack: SnapshotStateList<NavKey>) :
  MetricsNavigator {
  override fun back() = stack.debugPop()
}

internal class ServerUrlNavigatorImpl(private val stack: SnapshotStateList<NavKey>) :
  ServerUrlNavigator {
  override fun toLogin() = stack.debugPush(LoginNavRoute)

  override fun toAbout() = stack.debugPush(InfoNavRoute)
}

internal class LicensesNavigatorImpl(private val stack: SnapshotStateList<NavKey>) :
  LicensesNavigator {
  override fun back() = stack.debugPop()
}

internal class ReportsDashboardNavigatorImpl(private val stack: SnapshotStateList<NavKey>) :
  ReportsDashboardNavigator {
  override fun back() = stack.debugPop()

  override fun toReport(token: Token, budgetId: BudgetId, widgetId: WidgetId) =
    stack.debugPush(ReportNavRoute(token, budgetId, widgetId))

  override fun createReport(token: Token, budgetId: BudgetId) =
    stack.debugPush(CreateReportNavRoute(token, budgetId))
}

internal class ChooseReportTypeNavigatorImpl(private val stack: SnapshotStateList<NavKey>) :
  ChooseReportTypeNavigator {
  override fun back() = stack.debugPop()

  override fun toReport(token: Token, budgetId: BudgetId, widgetId: WidgetId) =
    stack.debugPush(ReportNavRoute(token, budgetId, widgetId))
}

internal class SettingsNavigatorImpl(private val stack: SnapshotStateList<NavKey>) :
  SettingsNavigator {
  override fun back() = stack.debugPop()

  override fun toThemeSettings() = stack.debugPush(ThemeSettingsNavRoute)
}

internal class InspectThemeNavigatorImpl(private val stack: SnapshotStateList<NavKey>) :
  InspectThemeNavigator {
  override fun navBack() = stack.debugPop()
}

internal class ThemeSettingsNavigatorImpl(private val stack: SnapshotStateList<NavKey>) :
  ThemeSettingsNavigator {
  override fun back() = stack.debugPop()

  override fun inspectTheme(id: Theme.Id) = stack.debugPush(InspectThemeNavRoute(id))
}

internal class SyncBudgetNavigatorImpl(private val stack: SnapshotStateList<NavKey>) :
  SyncBudgetNavigator {
  override fun back() = stack.debugPop()

  override fun toBudget(token: Token, budgetId: BudgetId) =
    stack.debugPopUpToAndPush(
      TransactionsNavRoute(token, budgetId),
      { it is ListBudgetsNavRoute },
      inclusive = false,
    )
}

internal class TransactionsNavigatorImpl(private val stack: SnapshotStateList<NavKey>) :
  TransactionsNavigator {
  override fun back() = stack.debugPop()
}
