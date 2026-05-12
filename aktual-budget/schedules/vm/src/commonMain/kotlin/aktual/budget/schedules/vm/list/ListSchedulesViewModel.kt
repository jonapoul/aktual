package aktual.budget.schedules.vm.list

import aktual.budget.db.dao.ScheduleDao
import aktual.di.BudgetScope
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// TODO: Implement
@Suppress("unused")
@Stable
@ViewModelKey
@ContributesIntoMap(BudgetScope::class)
class ListSchedulesViewModel(scheduleDao: ScheduleDao) : ViewModel() {
  private val mutableState = MutableStateFlow<ListSchedulesState>(Loading)
  val state: StateFlow<ListSchedulesState> = mutableState.asStateFlow()

  fun reload() {
    // TBC
  }
}
