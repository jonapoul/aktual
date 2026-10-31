package aktual.budget.schedules.vm.list

import aktual.budget.BudgetSyncController
import aktual.budget.db.dao.DatabaseTables.RULES
import aktual.budget.db.dao.DatabaseTables.SCHEDULES
import aktual.budget.model.LocalChange
import aktual.budget.model.ScheduleSort
import aktual.budget.model.tombstone
import aktual.budget.model.untombstone
import aktual.budget.schedules.domain.Schedule
import aktual.budget.schedules.domain.ScheduleTransactionPoster
import aktual.budget.schedules.domain.SchedulesLoader
import aktual.budget.schedules.vm.edit.ScheduleWriter
import aktual.core.Calendar
import aktual.core.runCatchingCancellable
import aktual.di.BudgetScope
import aktual.di.ViewModelContext
import aktual.di.newScope
import aktual.prefs.SchedulePreferences
import aktual.prefs.asStateFlow
import alakazam.kotlin.requireMessage
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import logcat.logcat

@Stable
@ViewModelKey
@ContributesIntoMap(BudgetScope::class)
class ListSchedulesViewModel
internal constructor(
  loader: SchedulesLoader,
  private val syncController: BudgetSyncController,
  private val writer: ScheduleWriter,
  private val poster: ScheduleTransactionPoster,
  private val preferences: SchedulePreferences,
  calendar: Calendar,
  viewModelContext: ViewModelContext,
) : ViewModel(viewModelContext.newScope()) {
  private val retries = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

  private val mutableEvents =
    MutableSharedFlow<ListSchedulesEvent>(
      replay = 0,
      extraBufferCapacity = 1,
      onBufferOverflow = DROP_OLDEST,
    )
  val events: SharedFlow<ListSchedulesEvent> = mutableEvents.asSharedFlow()

  val state: StateFlow<ListSchedulesState> =
    retries
      .onStart { emit(Unit) }
      .flatMapLatest {
        combine(
            loader.observe(),
            preferences.showCompleted.asFlow(),
            preferences.sortField.asFlow(),
            preferences.sortDirection.asFlow(),
          ) { schedules, includeCompleted, sortField, sortDirection ->
            val visible =
              if (includeCompleted) {
                schedules
              } else {
                schedules.filterNot { it.isCompleted }
              }
            val sort = ScheduleSort(sortField, sortDirection)
            if (visible.isEmpty()) {
              Empty
            } else {
              Success(visible.sortedWith(sort.comparator()).toImmutableList(), sort)
            }
          }
          .onStart<ListSchedulesState> { emit(Loading) }
          .catch { e ->
            logcat.e(e) { "Failed loading schedules" }
            emit(Failure(e.requireMessage()))
          }
      }
      .stateIn(viewModelScope, Eagerly, initialValue = Loading)

  val showCompleted: StateFlow<Boolean> = preferences.showCompleted.asStateFlow(viewModelScope)

  val today: StateFlow<LocalDate> =
    calendar
      .observeToday()
      .stateIn(viewModelScope, WhileSubscribed(stopTimeoutMillis = 5_000), calendar.today())

  fun retry() {
    retries.tryEmit(Unit)
  }

  // deleteSchedule() in packages/loot-core/src/server/schedules/app.ts
  fun delete(schedule: Schedule) {
    viewModelScope.launch {
      runCatchingCancellable {
        syncController.syncChanges(schedule.tombstones(::tombstone))
        mutableEvents.tryEmit(ListSchedulesEvent.Deleted(schedule))
        logcat.i { "Deleted schedule ${schedule.id}" }
      }
        .onFailure { e ->
          logcat.e(e) { "Failed deleting schedule ${schedule.id}" }
          mutableEvents.tryEmit(ListSchedulesEvent.DeleteFailed(schedule))
        }
    }
  }

  fun undoDelete(schedule: Schedule) {
    viewModelScope.launch {
      runCatchingCancellable {
        syncController.syncChanges(schedule.tombstones(::untombstone))
        logcat.i { "Restored schedule ${schedule.id}" }
      }
        .onFailure { e ->
          logcat.e(e) { "Failed restoring schedule ${schedule.id}" }
          mutableEvents.tryEmit(ListSchedulesEvent.RestoreFailed(schedule))
        }
    }
  }

  fun complete(schedule: Schedule) {
    viewModelScope.launch {
      runCatchingCancellable {
        writer.complete(schedule.id)
        mutableEvents.tryEmit(ListSchedulesEvent.Completed(schedule))
      }
        .onFailure { e ->
          logcat.e(e) { "Failed completing schedule ${schedule.id}" }
          mutableEvents.tryEmit(ListSchedulesEvent.CompleteFailed(schedule))
        }
    }
  }

  fun undoComplete(schedule: Schedule) {
    viewModelScope.launch {
      runCatchingCancellable { writer.uncomplete(schedule.id) }
        .onFailure { e ->
          logcat.e(e) { "Failed undoing completion of schedule ${schedule.id}" }
          mutableEvents.tryEmit(ListSchedulesEvent.RestoreFailed(schedule))
        }
    }
  }

  fun restart(schedule: Schedule) {
    viewModelScope.launch {
      runCatchingCancellable { writer.restart(schedule.id) }
        .onFailure { e ->
          logcat.e(e) { "Failed restarting schedule ${schedule.id}" }
          mutableEvents.tryEmit(ListSchedulesEvent.RestartFailed(schedule))
        }
    }
  }

  fun post(schedule: Schedule, date: LocalDate) {
    viewModelScope.launch {
      runCatchingCancellable {
        val transactions = poster.post(schedule.id, date)
        mutableEvents.tryEmit(ListSchedulesEvent.Posted(schedule, transactions.toImmutableList()))
      }
        .onFailure { e ->
          logcat.e(e) { "Failed posting schedule ${schedule.id}" }
          mutableEvents.tryEmit(ListSchedulesEvent.PostFailed(schedule))
        }
    }
  }

  fun undoPost(event: ListSchedulesEvent.Posted) {
    viewModelScope.launch {
      runCatchingCancellable { poster.unpost(event.transactions) }
        .onFailure { e ->
          logcat.e(e) { "Failed undoing post of schedule ${event.schedule.id}" }
          mutableEvents.tryEmit(ListSchedulesEvent.UnpostFailed(event.schedule))
        }
    }
  }

  fun setSort(sort: ScheduleSort) {
    viewModelScope.launch {
      preferences.sortField.set(sort.field)
      preferences.sortDirection.set(sort.direction)
    }
  }

  fun setShowCompleted(show: Boolean) {
    viewModelScope.launch { preferences.showCompleted.set(show) }
  }

  private fun ScheduleSort.comparator(): Comparator<Schedule> {
    val byField =
      when (field) {
        NextDate -> compareBy<Schedule> { it.nextDate }.thenBy { it.status.ordinal }
        Name -> compareBy(nullsLast()) { it.name?.lowercase() }
        Payee -> compareBy { it.payeeName.lowercase() }
        Account -> compareBy { it.accountName.lowercase() }
        Amount -> compareBy { it.amount }
        Status -> compareBy { it.status.ordinal }
      }
    return direction.apply(byField).thenBy { it.nextDate }
  }
}

private fun Schedule.tombstones(change: (String, String) -> LocalChange): List<LocalChange> =
  listOf(change(RULES, ruleId.value), change(SCHEDULES, id.value))
