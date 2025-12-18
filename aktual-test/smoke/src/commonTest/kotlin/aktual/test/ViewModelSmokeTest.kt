package aktual.test

import aktual.about.vm.AboutViewModel
import aktual.about.vm.LicensesViewModel
import aktual.account.vm.ChangePasswordViewModel
import aktual.account.vm.LoginViewModel
import aktual.account.vm.ServerUrlViewModel
import aktual.budget.list.vm.ListBudgetsViewModel
import aktual.budget.reports.vm.ChooseReportTypeViewModel
import aktual.budget.reports.vm.ReportsDashboardViewModel
import aktual.budget.sync.vm.SyncBudgetViewModel
import aktual.budget.transactions.vm.TransactionsViewModel
import aktual.core.di.AppGraph
import aktual.metrics.vm.MetricsViewModel
import aktual.settings.vm.SettingsViewModel
import androidx.lifecycle.ViewModel
import assertk.assertThat
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import kotlin.test.BeforeTest
import kotlin.test.Test

/**
 * To be implemented for each target - makes sure VMs are bound as expected to DI graph
 */
abstract class ViewModelSmokeTest {
  protected lateinit var graph: AppGraph
  protected abstract fun buildGraph(): AppGraph

  @BeforeTest
  fun before() {
    graph = buildGraph()
  }

  @Test
  fun about() = testVm<AboutViewModel>()

  @Test
  fun licenses() = testVm<LicensesViewModel>()

  @Test
  fun login() = testVm<LoginViewModel>()

  @Test
  fun metrics() = testVm<MetricsViewModel>()

  @Test
  fun password() = testVm<ChangePasswordViewModel>()

  @Test
  fun settings() = testVm<SettingsViewModel>()

  @Test
  fun url() = testVm<ServerUrlViewModel>()

  @Test
  fun budgetList() = testAssistedVM<ListBudgetsViewModel, ListBudgetsViewModel.Factory> { create(LOGIN_TOKEN) }

  @Test
  fun reportDashboard() = testAssistedVM<ReportsDashboardViewModel, ReportsDashboardViewModel.Factory> {
    create(LOGIN_TOKEN, BUDGET_ID)
  }

  @Test
  fun reportList() = testAssistedVM<ChooseReportTypeViewModel, ChooseReportTypeViewModel.Factory> { create(BUDGET_ID) }

  @Test
  fun syncBudget() = testAssistedVM<SyncBudgetViewModel, SyncBudgetViewModel.Factory> { create(LOGIN_TOKEN, BUDGET_ID) }

  @Test
  fun transactions() = testAssistedVM<TransactionsViewModel, TransactionsViewModel.Factory> {
    create(LOGIN_TOKEN, BUDGET_ID, TRANSACTIONS_SPEC)
  }

  protected inline fun <reified VM : ViewModel> testVm() {
    assertThat(graph.viewModelProviders[VM::class])
      .isNotNull()
      .transform { provider -> provider() }
      .isInstanceOf(VM::class)
  }

  protected inline fun <reified VM : ViewModel, reified F : ManualViewModelAssistedFactory> testAssistedVM(
    build: F.() -> VM,
  ) {
    assertThat(graph.manualAssistedFactoryProviders[F::class])
      .isNotNull()
      .transform { provider -> provider() }
      .isInstanceOf<F>()
      .transform { factory -> factory.build() }
      .isInstanceOf(VM::class)
  }
}
