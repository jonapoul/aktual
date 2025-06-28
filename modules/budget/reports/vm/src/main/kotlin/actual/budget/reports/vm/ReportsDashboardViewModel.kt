package actual.budget.reports.vm

import actual.account.model.LoginToken
import actual.budget.model.BudgetId
import actual.budget.model.CustomReportId
import alakazam.kotlin.logging.Logger
import androidx.lifecycle.ViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Suppress("unused")
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
    Logger.d("init")
    start()
  }

  override fun onCleared() {
    super.onCleared()
    job?.cancel()
  }

  private fun start() {
    // TBC
  }

  fun renameReport(id: CustomReportId) {
    // TODO
  }

  fun deleteReport(id: CustomReportId) {
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
