package aktual.budget.schedules.vm.list

import aktual.budget.db.dao.ScheduleDao
import aktual.budget.di.BudgetGraphHolder
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Stable
@ViewModelKey
@ContributesIntoMap(AppScope::class, binding<ViewModel>())
class ListSchedulesViewModel(budgetGraphs: BudgetGraphHolder) : ViewModel() {
  private val budgetGraph = budgetGraphs.require()

  @Suppress("unused") private val scheduleDao = ScheduleDao(budgetGraph.database)

  private val mutableState = MutableStateFlow<ListSchedulesState>(Loading)
  val state: StateFlow<ListSchedulesState> = mutableState.asStateFlow()

  fun reload() {
    // TBC
  }
}
