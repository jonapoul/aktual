package aktual.budget.schedules.vm.list

import aktual.budget.schedules.vm.Schedule
import aktual.budget.schedules.vm.SchedulesLoader
import aktual.di.BudgetScope
import alakazam.kotlin.requireMessage
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.launchMolecule
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logcat.logcat

@Stable
@ViewModelKey
@ContributesIntoMap(BudgetScope::class)
class ListSchedulesViewModel internal constructor(private val loader: SchedulesLoader) :
  ViewModel() {
  private val mutableSchedules = MutableStateFlow<ImmutableList<Schedule>>(persistentListOf())
  private val mutableIsLoading = MutableStateFlow(true)
  private val mutableFailure = MutableStateFlow<String?>(null)

  val state: StateFlow<ListSchedulesState> =
    viewModelScope.launchMolecule(Immediate) {
      val schedules by mutableSchedules.collectAsState()
      val isLoading by mutableIsLoading.collectAsState()
      val failure by mutableFailure.collectAsState()
      when {
        isLoading -> Loading
        failure != null -> Failure(failure)
        schedules.isEmpty() -> Empty
        else -> Success(schedules)
      }
    }

  init {
    reload()
  }

  fun reload() {
    mutableIsLoading.update { true }
    viewModelScope.launch {
      try {
        val schedules = loader.load()
        mutableSchedules.update { schedules }
        mutableFailure.update { null }
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        logcat.e(e) { "Failed loading schedules" }
        mutableFailure.update { e.requireMessage() }
      } finally {
        mutableIsLoading.update { false }
      }
    }
  }
}
