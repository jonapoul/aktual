package aktual.budget.reports.vm

import aktual.budget.model.BudgetId
import aktual.budget.model.WidgetId
import aktual.core.model.LoginToken
import androidx.lifecycle.ViewModel
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import logcat.logcat

@Suppress("unused", "VarCouldBeVal")
@AssistedInject
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
  @ManualViewModelAssistedFactoryKey(Factory::class)
  @ContributesIntoMap(AppScope::class)
  fun interface Factory : ManualViewModelAssistedFactory {
    fun create(
      @Assisted token: LoginToken,
      @Assisted budgetId: BudgetId,
    ): ReportsDashboardViewModel
  }
}
