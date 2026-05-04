package aktual.test

import aktual.about.vm.AboutViewModel
import aktual.about.vm.LicensesViewModel
import aktual.account.vm.ChangePasswordViewModel
import aktual.account.vm.LoginViewModel
import aktual.account.vm.ServerUrlViewModel
import aktual.budget.list.vm.ListBudgetsViewModel
import aktual.budget.model.RuleId
import aktual.budget.reports.vm.choosetype.ChooseReportTypeViewModel
import aktual.budget.reports.vm.dashboard.ReportsDashboardViewModel
import aktual.budget.rules.vm.edit.EditRuleViewModel
import aktual.budget.rules.vm.list.ListRulesViewModel
import aktual.budget.schedules.vm.list.ListSchedulesViewModel
import aktual.budget.sync.vm.SyncBudgetViewModel
import aktual.budget.transactions.vm.TransactionsViewModel
import aktual.core.theme.DarkTheme
import aktual.metrics.vm.MetricsViewModel
import aktual.prefs.vm.inspect.InspectThemeViewModel
import aktual.prefs.vm.root.SettingsViewModel
import aktual.prefs.vm.theme.ThemeSettingsViewModel
import aktual.prefs.vm.theme.custom.CustomThemeSettingsViewModel
import androidx.lifecycle.ViewModel
import assertk.assertThat
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import logcat.LogcatLogger
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toOkioPath

/** To be implemented for each target - makes sure VMs are bound as expected to DI graph */
abstract class ViewModelSmokeTest<G : TestAppGraph> {
  private lateinit var rootDir: Path
  protected lateinit var appGraph: G

  protected abstract fun buildGraph(container: TestContainer): G

  @BeforeTest
  fun before() {
    optionallySkip()
    rootDir = createTempDirectory().toOkioPath()
    appGraph = buildGraph(TestContainer(rootDir))
    with(appGraph.runLevelController) {
      init(listOf(appGraph))
      onServerChosen(SERVER_URL)
      onLoggedIn(LOGIN_TOKEN)
      onBudget(DB_METADATA)
    }
  }

  @AfterTest
  fun after() {
    LogcatLogger.uninstall()
    appGraph.runLevelController.close()
    FileSystem.SYSTEM.deleteRecursively(rootDir)
  }

  protected abstract fun optionallySkip()

  @Test fun about() = testVm<AboutViewModel>()

  @Test fun budgetList() = testVm<ListBudgetsViewModel>()

  @Test fun customThemeSettings() = testVm<CustomThemeSettingsViewModel>()

  @Test fun licenses() = testVm<LicensesViewModel>()

  @Test fun listRules() = testVm<ListRulesViewModel>()

  @Test fun listSchedules() = testVm<ListSchedulesViewModel>()

  @Test fun login() = testVm<LoginViewModel>()

  @Test fun metrics() = testVm<MetricsViewModel>()

  @Test fun password() = testVm<ChangePasswordViewModel>()

  @Test fun reportDashboard() = testVm<ReportsDashboardViewModel>()

  @Test fun reportList() = testVm<ChooseReportTypeViewModel>()

  @Test fun settings() = testVm<SettingsViewModel>()

  @Test fun themeSettings() = testVm<ThemeSettingsViewModel>()

  @Test fun url() = testVm<ServerUrlViewModel>()

  @Test
  fun syncBudget() =
    testAssistedVM<SyncBudgetViewModel, SyncBudgetViewModel.Factory> { create(BUDGET_ID) }

  @Test
  fun transactions() =
    testAssistedVM<TransactionsViewModel, TransactionsViewModel.Factory> {
      create(TRANSACTIONS_SPEC)
    }

  @Test
  fun inspectTheme() =
    testAssistedVM<InspectThemeViewModel, InspectThemeViewModel.Factory> { create(DarkTheme.id) }

  @Test
  fun editRule() =
    testAssistedVM<EditRuleViewModel, EditRuleViewModel.Factory> { create(RuleId("abc-123")) }

  protected inline fun <reified VM : ViewModel> testVm() = runTest {
    val vmGraph = appGraph.runLevelState.viewModelGraph().first()
    assertThat(vmGraph.viewModelProviders[VM::class])
      .isNotNull()
      .transform { provider -> provider() }
      .isInstanceOf(VM::class)
  }

  protected inline fun <
    reified VM : ViewModel,
    reified F : ManualViewModelAssistedFactory,
  > testAssistedVM(crossinline build: F.() -> VM) = runTest {
    val vmGraph = appGraph.runLevelState.viewModelGraph().first()
    assertThat(vmGraph.manualAssistedFactoryProviders[F::class])
      .isNotNull()
      .transform { provider -> provider() }
      .isInstanceOf<F>()
      .transform { factory -> factory.build() }
      .isInstanceOf(VM::class)
  }
}
