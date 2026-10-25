package aktual.budget.payees.vm.list

import aktual.budget.BudgetSyncController
import aktual.budget.db.dao.DatabaseTables.PAYEES
import aktual.budget.model.PayeeId
import aktual.budget.model.localChange
import aktual.budget.model.tombstone
import aktual.budget.model.untombstone
import aktual.budget.payees.vm.PayeesLoader
import aktual.di.BudgetCoroutineScope
import aktual.di.BudgetScope
import alakazam.kotlin.requireMessage
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logcat.logcat

@Stable
@ViewModelKey
@ContributesIntoMap(BudgetScope::class)
class ListPayeesViewModel
internal constructor(
  loader: PayeesLoader,
  private val syncController: BudgetSyncController,
  private val budgetScope: BudgetCoroutineScope,
) : ViewModel() {
  private val retries = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
  private val mutableSelected = MutableStateFlow<PersistentSet<PayeeId>>(persistentSetOf())

  private val mutableEvents =
    MutableSharedFlow<ListPayeesEvent>(extraBufferCapacity = 1, onBufferOverflow = DROP_OLDEST)
  val events: SharedFlow<ListPayeesEvent> = mutableEvents.asSharedFlow()

  val state: StateFlow<ListPayeesState> =
    retries
      .onStart { emit(Unit) }
      .flatMapLatest {
        loader
          .observe()
          .map { payees -> if (payees.isEmpty()) Empty else Success(payees.toImmutableList()) }
          .onStart<ListPayeesState> { emit(Loading) }
          .catch { e ->
            logcat.e(e) { "Failed loading payees" }
            emit(Failure(e.requireMessage()))
          }
      }
      .onEach(::pruneSelection)
      .stateIn(viewModelScope, Eagerly, initialValue = Loading)

  // Transfer payees can't be selected, as upstream
  val selected: StateFlow<ImmutableSet<PayeeId>> = mutableSelected.asStateFlow()

  fun retry() {
    retries.tryEmit(Unit)
  }

  fun toggleSelected(id: PayeeId) {
    if (payees().none { it.id == id && !it.isTransfer }) return
    mutableSelected.update { if (id in it) it.removing(id) else it.adding(id) }
  }

  fun selectAll() = mutableSelected.update { selectable().toPersistentSet() }

  fun clearSelection() = mutableSelected.update { persistentSetOf() }

  fun deleteSelected() {
    val ids = mutableSelected.value
    if (ids.isEmpty()) return
    viewModelScope.launch {
      try {
        syncController.syncChanges(ids.map { tombstone(PAYEES, it.value) })
        clearSelection()
        mutableEvents.tryEmit(ListPayeesEvent.Deleted(ids))
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        logcat.e(e) { "Failed deleting payees $ids" }
        mutableEvents.tryEmit(DeleteFailed)
      }
    }
  }

  // The undo snackbar can outlive this screen, so the restore can't be tied to viewModelScope
  fun undoDelete(ids: Set<PayeeId>) {
    budgetScope.launch {
      try {
        syncController.syncChanges(ids.map { untombstone(PAYEES, it.value) })
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        logcat.e(e) { "Failed restoring payees $ids" }
        mutableEvents.tryEmit(RestoreFailed)
      }
    }
  }

  // As upstream: unfavourite them all if they're all favourites, otherwise favourite them all
  fun toggleFavoriteSelected() {
    val ids = mutableSelected.value
    val selectedPayees = payees().filter { it.id in ids }
    if (selectedPayees.isEmpty()) return
    val favorite = !selectedPayees.all { it.isFavorite }
    viewModelScope.launch {
      try {
        syncController.syncChanges(
          selectedPayees.map { localChange(PAYEES, it.id.value, "favorite", favorite) },
        )
        clearSelection()
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        logcat.e(e) { "Failed setting favorite=$favorite on $ids" }
        mutableEvents.tryEmit(UpdateFailed)
      }
    }
  }

  private fun payees(): List<PayeeItem> = (state.value as? Success)?.payees.orEmpty()

  private fun selectable(): List<PayeeId> = payees().filterNot { it.isTransfer }.map { it.id }

  // Drops payees that have gone, like ones deleted by another device
  private fun pruneSelection(state: ListPayeesState) {
    when (state) {
      Empty -> clearSelection()
      is Success -> {
        val alive = state.payees.filterNot { it.isTransfer }.mapTo(HashSet()) { it.id }
        mutableSelected.update { ids -> ids.retainingAll(alive) }
      }
      Loading,
      is Failure -> Unit
    }
  }
}
