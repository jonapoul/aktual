package aktual.test

import aktual.about.vm.AboutViewModel
import aktual.about.vm.LicensesViewModel
import aktual.account.vm.ChangePasswordViewModel
import aktual.account.vm.LoginViewModel
import aktual.account.vm.ServerUrlViewModel
import aktual.budget.di.BudgetGraph
import aktual.budget.list.vm.ListBudgetsViewModel
import aktual.budget.reports.vm.choosetype.ChooseReportTypeViewModel
import aktual.budget.reports.vm.dashboard.ReportsDashboardViewModel
import aktual.budget.rules.vm.ListRulesViewModel
import aktual.budget.sync.vm.SyncBudgetViewModel
import aktual.budget.transactions.vm.TransactionsViewModel
import aktual.core.theme.DarkTheme
import aktual.metrics.vm.MetricsViewModel
import aktual.prefs.vm.inspect.InspectThemeViewModel
import aktual.prefs.vm.root.SettingsViewModel
import aktual.prefs.vm.theme.ThemeSettingsViewModel
import aktual.prefs.vm.theme.custom.CustomThemeSettingsViewModel
import androidx.lifecycle.ViewModel
import app.cash.burst.InterceptTest
import assertk.assertThat
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.flow.update
import logcat.LogcatLogger

/** To be implemented for each target - makes sure VMs are bound as expected to DI graph */
abstract class ViewModelSmokeTest<G : TestAppGraph> {
  @InterceptTest val temporaryFolder = TemporaryFolder()

  protected lateinit var appGraph: G
  private lateinit var budgetGraph: BudgetGraph

  protected abstract fun buildGraph(container: TestContainer): G

  @BeforeTest
  fun before() {
    optionallySkip()
    val container = TestContainer(temporaryFolder)
    appGraph = buildGraph(container)
    budgetGraph = appGraph.budgetGraphBuilder.invoke(DB_METADATA)
    appGraph.budgetGraphHolder.update { budgetGraph }
  }

  @AfterTest
  fun after() {
    LogcatLogger.uninstall()
    budgetGraph.driver.close()
  }

  protected abstract fun optionallySkip()

  @Test fun about() = testVm<AboutViewModel>()

  @Test fun customThemeSettings() = testVm<CustomThemeSettingsViewModel>()

  @Test fun licenses() = testVm<LicensesViewModel>()

  @Test fun listRules() = testVm<ListRulesViewModel>()

  @Test fun login() = testVm<LoginViewModel>()

  @Test fun metrics() = testVm<MetricsViewModel>()

  @Test fun password() = testVm<ChangePasswordViewModel>()

  @Test fun reportDashboard() = testVm<ReportsDashboardViewModel>()

  @Test fun settings() = testVm<SettingsViewModel>()

  @Test fun themeSettings() = testVm<ThemeSettingsViewModel>()

  @Test fun url() = testVm<ServerUrlViewModel>()

  @Test
  fun budgetList() =
    testAssistedVM<ListBudgetsViewModel, ListBudgetsViewModel.Factory> { create(LOGIN_TOKEN) }

  @Test
  fun reportList() =
    testAssistedVM<ChooseReportTypeViewModel, ChooseReportTypeViewModel.Factory> {
      create(BUDGET_ID)
    }

  @Test
  fun syncBudget() =
    testAssistedVM<SyncBudgetViewModel, SyncBudgetViewModel.Factory> {
      create(LOGIN_TOKEN, BUDGET_ID)
    }

  @Test
  fun transactions() =
    testAssistedVM<TransactionsViewModel, TransactionsViewModel.Factory> {
      create(LOGIN_TOKEN, BUDGET_ID, TRANSACTIONS_SPEC)
    }

  @Test
  fun inspectTheme() =
    testAssistedVM<InspectThemeViewModel, InspectThemeViewModel.Factory> { create(DarkTheme.id) }

  protected inline fun <reified VM : ViewModel> testVm() {
    assertThat(appGraph.viewModelProviders[VM::class])
      .isNotNull()
      .transform { provider -> provider() }
      .isInstanceOf(VM::class)
  }

  protected inline fun <
    reified VM : ViewModel,
    reified F : ManualViewModelAssistedFactory,
  > testAssistedVM(build: F.() -> VM) {
    assertThat(appGraph.manualAssistedFactoryProviders[F::class])
      .isNotNull()
      .transform { provider -> provider() }
      .isInstanceOf<F>()
      .transform { factory -> factory.build() }
      .isInstanceOf(VM::class)
  }
}
