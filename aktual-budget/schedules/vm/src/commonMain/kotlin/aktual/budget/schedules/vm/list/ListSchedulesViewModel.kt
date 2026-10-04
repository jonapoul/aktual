package aktual.budget.schedules.vm.list

import aktual.budget.BudgetSyncController
import aktual.budget.db.dao.DatabaseTables.RULES
import aktual.budget.db.dao.DatabaseTables.SCHEDULES
import aktual.budget.model.LocalChange
import aktual.budget.model.tombstone
import aktual.budget.model.untombstone
import aktual.budget.schedules.domain.Schedule
import aktual.budget.schedules.domain.SchedulesLoader
import aktual.di.BudgetScope
import aktual.prefs.SchedulePreferences
import aktual.prefs.asStateFlow
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
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logcat.logcat

@Stable
@ViewModelKey
@ContributesIntoMap(BudgetScope::class)
class ListSchedulesViewModel
internal constructor(
  private val loader: SchedulesLoader,
  private val syncController: BudgetSyncController,
  preferences: SchedulePreferences,
) : ViewModel() {
  private val mutableSchedules = MutableStateFlow<ImmutableList<Schedule>>(persistentListOf())
  private val mutableIsLoading = MutableStateFlow(true)
  private val mutableFailure = MutableStateFlow<String?>(null)

  private val mutableEvents =
    MutableSharedFlow<ListSchedulesEvent>(
      replay = 0,
      extraBufferCapacity = 1,
      onBufferOverflow = DROP_OLDEST,
    )
  val events: SharedFlow<ListSchedulesEvent> = mutableEvents.asSharedFlow()

  private val showCompleted = preferences.showCompleted.asStateFlow(viewModelScope)

  val state: StateFlow<ListSchedulesState> =
    viewModelScope.launchMolecule(Immediate) {
      val schedules by mutableSchedules.collectAsState()
      val isLoading by mutableIsLoading.collectAsState()
      val failure by mutableFailure.collectAsState()
      val includeCompleted by showCompleted.collectAsState()
      val visible =
        if (includeCompleted) {
          schedules
        } else {
          schedules.filterNot { it.isCompleted }.toImmutableList()
        }
      when {
        isLoading -> Loading
        failure != null -> Failure(failure)
        visible.isEmpty() -> Empty
        else -> Success(visible)
      }
    }

  init {
    reload()
  }

  fun reload(showLoading: Boolean = true) {
    if (showLoading) mutableIsLoading.update { true }
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

  // deleteSchedule() in packages/loot-core/src/server/schedules/app.ts
  fun delete(schedule: Schedule) {
    viewModelScope.launch {
      val index = mutableSchedules.value.indexOfFirst { it.id == schedule.id }
      try {
        syncController.syncChanges(schedule.tombstones(::tombstone))
        mutableSchedules.update { list ->
          list.filterNot { it.id == schedule.id }.toImmutableList()
        }
        mutableEvents.tryEmit(ListSchedulesEvent.Deleted(schedule, index))
        logcat.i { "Deleted schedule ${schedule.id}" }
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        logcat.e(e) { "Failed deleting schedule ${schedule.id}" }
        mutableEvents.tryEmit(ListSchedulesEvent.DeleteFailed(schedule))
      }
    }
  }

  fun undoDelete(schedule: Schedule, index: Int) {
    viewModelScope.launch {
      try {
        syncController.syncChanges(schedule.tombstones(::untombstone))
        mutableSchedules.update { list ->
          if (list.any { it.id == schedule.id }) {
            list
          } else {
            list.toMutableList().apply { add(index.coerceIn(0, size), schedule) }.toImmutableList()
          }
        }
        logcat.i { "Restored schedule ${schedule.id}" }
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        logcat.e(e) { "Failed restoring schedule ${schedule.id}" }
        mutableEvents.tryEmit(ListSchedulesEvent.RestoreFailed(schedule))
      }
    }
  }
}

private fun Schedule.tombstones(change: (String, String) -> LocalChange): List<LocalChange> =
  listOf(change(RULES, ruleId.value), change(SCHEDULES, id.value))
