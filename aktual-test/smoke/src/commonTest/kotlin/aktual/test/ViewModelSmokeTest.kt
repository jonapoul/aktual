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
import aktual.core.theme.DarkColors
import aktual.metrics.vm.MetricsViewModel
import aktual.prefs.vm.inspect.InspectThemeViewModel
import aktual.prefs.vm.root.SettingsViewModel
import aktual.prefs.vm.theme.ThemeSettingsViewModel
import aktual.prefs.vm.theme.custom.CustomThemeSettingsViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.SAVED_STATE_REGISTRY_OWNER_KEY
import androidx.lifecycle.VIEW_MODEL_STORE_OWNER_KEY
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.Companion.VIEW_MODEL_KEY
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.enableSavedStateHandles
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import assertk.assertThat
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import logcat.LogcatLogger
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toOkioPath

/** To be implemented for each target - makes sure VMs are bound as expected to DI graph */
abstract class ViewModelSmokeTest<G : TestAppGraph> {
  protected lateinit var rootDir: Path
  protected lateinit var appGraph: G
  protected var viewModel: ViewModel? = null

  @BeforeTest
  fun before() {
    optionallySkip()
    rootDir = createTempDirectory().toOkioPath()
    appGraph = buildGraph()
    with(appGraph.runLevelController) {
      init(listOf(appGraph))
      onServerChosen(SERVER_URL)
      onLoggedIn(LOGIN_TOKEN)
      onBudget(DB_METADATA)
    }
  }

  @AfterTest
  fun after() {
    // cancel viewModelScope before after() deletes the temp dir, to avoid SQLITE_CANTOPEN
    // from background coroutines that outlive the test
    viewModel?.viewModelScope?.cancel()

    LogcatLogger.uninstall()
    appGraph.close()
    FileSystem.SYSTEM.deleteRecursively(rootDir)

    // platform hook: runs after graph teardown so platforms can drain pending async work
    // (e.g. Android needs to idle the main looper to avoid multiple-DataStore errors)
    afterPlatformCleanup()
  }

  protected abstract fun buildGraph(): G

  protected open fun afterPlatformCleanup() = Unit

  protected open fun optionallySkip() = Unit

  @Test fun about() = testSavedStateVM<AboutViewModel>()

  @Test fun budgetList() = testVm<ListBudgetsViewModel>()

  @Test fun customThemeSettings() = testVm<CustomThemeSettingsViewModel>()

  @Test fun licenses() = testSavedStateVM<LicensesViewModel>()

  @Test fun listRules() = testSavedStateVM<ListRulesViewModel>()

  @Test fun listSchedules() = testSavedStateVM<ListSchedulesViewModel>()

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
    testAssistedVM<InspectThemeViewModel, InspectThemeViewModel.Factory> { create(DarkColors.id) }

  @Test
  fun editRule() =
    testAssistedVM<EditRuleViewModel, EditRuleViewModel.Factory> { create(RuleId("abc-123")) }

  protected inline fun <reified VM : ViewModel> testVm() = runTest {
    val viewModelFactory = appGraph.runLevelState.viewModelFactory().first()
    viewModel = viewModelFactory.create(VM::class, CreationExtras.Empty)
    assertThat(viewModel).isNotNull().isInstanceOf(VM::class)
  }

  protected inline fun <reified VM : ViewModel> testSavedStateVM() = runTest {
    val viewModelFactory = appGraph.runLevelState.viewModelFactory().first()
    viewModel = viewModelFactory.create(modelClass = VM::class, extras = savedStateCreationExtras())
    assertThat(viewModel).isNotNull().isInstanceOf(VM::class)
  }

  protected inline fun <
    reified VM : ViewModel,
    reified F : ManualViewModelAssistedFactory,
  > testAssistedVM(crossinline build: F.() -> VM) = runTest {
    val viewModelFactory = appGraph.runLevelState.viewModelFactory().first()
    val factory = viewModelFactory.createManuallyAssistedFactory(F::class)
    assertThat(factory)
      .isNotNull()
      .transform { provider -> provider() }
      .isInstanceOf<F>()
      .transform { f -> f.build().also { viewModel = it } }
      .isInstanceOf(VM::class)
  }

  protected fun savedStateCreationExtras(): CreationExtras {
    val owner = TestSavedStateOwner().apply { enableSavedStateHandles() }
    return MutableCreationExtras().apply {
      this[SAVED_STATE_REGISTRY_OWNER_KEY] = owner
      this[VIEW_MODEL_STORE_OWNER_KEY] = owner
      this[VIEW_MODEL_KEY] = "smoke-test"
    }
  }

  private class TestSavedStateOwner : SavedStateRegistryOwner, ViewModelStoreOwner {
    private val lifecycleRegistry = LifecycleRegistry.createUnsafe(this)
    private val controller =
      SavedStateRegistryController.create(this).apply { performRestore(null) }

    override val lifecycle: Lifecycle
      get() = lifecycleRegistry

    override val savedStateRegistry: SavedStateRegistry
      get() = controller.savedStateRegistry

    override val viewModelStore = ViewModelStore()
  }
}
