package aktual.budget.payees.vm.list

import aktual.budget.BudgetSyncController
import aktual.budget.db.dao.DatabaseTables.PAYEES
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
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
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
      .stateIn(viewModelScope, Eagerly, initialValue = Loading)

  fun retry() {
    retries.tryEmit(Unit)
  }

  // Transfer payees can't be deleted, as upstream
  fun delete(payee: PayeeItem) {
    if (payee.isTransfer) return
    viewModelScope.launch {
      try {
        syncController.syncChanges(tombstone(PAYEES, payee.id.value))
        mutableEvents.tryEmit(ListPayeesEvent.Deleted(payee))
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        logcat.e(e) { "Failed deleting payee ${payee.id}" }
        mutableEvents.tryEmit(DeleteFailed)
      }
    }
  }

  // The undo snackbar can outlive this screen, so the restore can't be tied to viewModelScope
  fun undoDelete(payee: PayeeItem) {
    budgetScope.launch {
      try {
        syncController.syncChanges(untombstone(PAYEES, payee.id.value))
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        logcat.e(e) { "Failed restoring payee ${payee.id}" }
        mutableEvents.tryEmit(RestoreFailed)
      }
    }
  }

  fun toggleFavorite(payee: PayeeItem) {
    if (payee.isTransfer) return
    val favorite = !payee.isFavorite
    viewModelScope.launch {
      try {
        syncController.syncChanges(localChange(PAYEES, payee.id.value, "favorite", favorite))
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        logcat.e(e) { "Failed setting favorite=$favorite on ${payee.id}" }
        mutableEvents.tryEmit(UpdateFailed)
      }
    }
  }
}
