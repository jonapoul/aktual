package aktual.budget.transactions.vm.edit

import aktual.budget.model.Amount
import aktual.budget.model.TransactionId
import aktual.budget.transactions.domain.LoadedTransaction
import aktual.budget.transactions.domain.TransactionLoader
import aktual.budget.transactions.vm.edit.EditTransactionState.Failure
import aktual.budget.transactions.vm.toTransaction
import aktual.di.BudgetScope
import alakazam.kotlin.requireMessage
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.launchMolecule
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logcat.logcat

@Stable
@AssistedInject
class EditTransactionViewModel
internal constructor(
  @Assisted private val id: TransactionId,
  private val loader: TransactionLoader,
) : ViewModel() {
  @AssistedFactory
  @ManualViewModelAssistedFactoryKey
  @ContributesIntoMap(BudgetScope::class)
  fun interface Factory : ManualViewModelAssistedFactory {
    fun create(@Assisted id: TransactionId): EditTransactionViewModel
  }

  // What's currently saved, following any changes made elsewhere, e.g. by a sync
  private val mutableSaved = MutableStateFlow<TransactionDetails?>(null)
  private val mutableFailure = MutableStateFlow<Failure?>(null)
  private val mutableMode = MutableStateFlow<TransactionEditMode>(View)

  val state: StateFlow<EditTransactionState> =
    viewModelScope.launchMolecule(Immediate) {
      val saved by mutableSaved.collectAsState()
      val failure by mutableFailure.collectAsState()
      val mode by mutableMode.collectAsState()
      buildState(saved, failure, mode)
    }

  init {
    viewModelScope.launch { observe() }
  }

  private fun buildState(
    saved: TransactionDetails?,
    failure: Failure?,
    mode: TransactionEditMode,
  ): EditTransactionState {
    if (failure != null) return failure
    if (saved == null) return Loading
    return EditTransactionState.Loaded(saved = saved, mode = mode, canEdit = true)
  }

  private suspend fun observe() {
    try {
      loader.observe(id).collect { loaded ->
        if (loaded == null) {
          mutableFailure.update { NotFound }
        } else {
          mutableFailure.update { null }
          mutableSaved.update { loaded.toDetails() }
        }
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      logcat.e(e) { "Failed loading transaction $id" }
      mutableFailure.update { Failure.Other(e.requireMessage()) }
    }
  }

  private fun LoadedTransaction.toDetails() =
    TransactionDetails(
      transaction = detail.row.toTransaction(balance = Amount(balanceAfter), children = children),
      cleared = detail.cleared,
      reconciled = detail.reconciled,
    )
}
