package actual.budget.reports.vm

import actual.account.model.LoginToken
import actual.budget.model.BudgetId
import actual.budget.model.WidgetId
import androidx.lifecycle.ViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import logcat.logcat

@Suppress("unused", "VarCouldBeVal")
@HiltViewModel(assistedFactory = ReportsDashboardViewModel.Factory::class)
class ReportsDashboardViewModel @AssistedInject constructor(
  @Assisted inputs: Inputs,
) : ViewModel() {
  private val token = inputs.token
  private val budgetId = inputs.budgetId

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

  data class Inputs(
    val token: LoginToken,
    val budgetId: BudgetId,
  )

  @AssistedFactory
  fun interface Factory {
    fun create(
      @Assisted inputs: Inputs,
    ): ReportsDashboardViewModel
  }
}
