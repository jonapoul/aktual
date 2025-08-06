package actual.budget.reports.vm

import actual.account.model.LoginToken
import actual.budget.model.BudgetId
import actual.budget.model.WidgetId
import actual.core.di.ViewModelFactory
import actual.core.di.ViewModelFactoryKey
import actual.core.di.ViewModelKey
import actual.core.di.ViewModelScope
import androidx.lifecycle.ViewModel
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import logcat.logcat

@Inject
@ViewModelKey(ReportsDashboardViewModel::class)
@ContributesIntoMap(ViewModelScope::class)
class ReportsDashboardViewModel(
  @Assisted private val token: LoginToken,
  @Assisted private val budgetId: BudgetId,
) : ViewModel() {
  private var job: Job? = null

  private val mutableState = MutableStateFlow<DashboardState>(DashboardState.Loading)
  val state: StateFlow<DashboardState> = mutableState.asStateFlow()

  init {
    logcat.d { "init" }
    start()
  }

  override fun onCleared() {
    super.onCleared()
    job?.cancel()
  }

  private fun start() {
    // TBC
  }

  fun renameReport(id: WidgetId) {
    // TODO
  }

  fun deleteReport(id: WidgetId) {
    // TODO
  }

  @Inject
  @AssistedFactory
  @ViewModelFactoryKey(Factory::class)
  @ContributesIntoMap(ViewModelScope::class)
  fun interface Factory : ViewModelFactory {
    fun create(
      @Assisted token: LoginToken,
      @Assisted budgetId: BudgetId,
    ): ReportsDashboardViewModel
  }
}
