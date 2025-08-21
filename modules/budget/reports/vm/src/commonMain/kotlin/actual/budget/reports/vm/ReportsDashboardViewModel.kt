package actual.budget.reports.vm

import actual.budget.model.BudgetId
import actual.budget.model.WidgetId
import actual.core.di.AssistedFactoryKey
import actual.core.di.ViewModelAssistedFactory
import actual.core.di.ViewModelScope
import actual.core.model.LoginToken
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

@Suppress("unused")
@Inject
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

  @AssistedFactory
  @AssistedFactoryKey(Factory::class)
  @ContributesIntoMap(ViewModelScope::class)
  fun interface Factory : ViewModelAssistedFactory {
    fun create(
      @Assisted token: LoginToken,
      @Assisted budgetId: BudgetId,
    ): ReportsDashboardViewModel
  }
}
