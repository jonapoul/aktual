package aktual.budget.accounts.vm

import aktual.budget.db.dao.AccountDao
import aktual.budget.model.evaluateAmountInput
import aktual.budget.transactions.domain.TransactionWriter
import aktual.core.Calendar
import aktual.core.runCatchingCancellable
import aktual.di.BudgetScope
import aktual.di.ViewModelContext
import aktual.di.newScope
import alakazam.kotlin.requireMessage
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.launchMolecule
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logcat.logcat

/**
 * Creates a local account, as upstream's CreateLocalAccountModal. The name must be unique among
 * accounts that aren't deleted, closed ones included. See validateAccountName().
 */
@Stable
@ViewModelKey
@ContributesIntoMap(BudgetScope::class)
class CreateAccountViewModel
internal constructor(
  private val writer: TransactionWriter,
  private val accountDao: AccountDao,
  private val calendar: Calendar,
  viewModelContext: ViewModelContext,
) : ViewModel(viewModelContext.newScope()) {
  private val mutableName = MutableStateFlow("")
  private val mutableBalance = MutableStateFlow("")
  private val mutableOffBudget = MutableStateFlow(false)
  private val mutableIsNegative = MutableStateFlow(false)
  private val mutableExistingNames = MutableStateFlow(persistentSetOf<String>())
  private val mutableIsSaving = MutableStateFlow(false)
  private val mutableSaveError = MutableStateFlow<String?>(null)

  // Buffered, so the event waits for the screen if it's stopped when the account is created
  private val eventChannel = Channel<CreateAccountEvent>(BUFFERED)
  val events: Flow<CreateAccountEvent> = eventChannel.receiveAsFlow()

  val state: StateFlow<CreateAccountState> =
    viewModelScope.launchMolecule(Immediate) {
      val name by mutableName.collectAsState()
      val balance by mutableBalance.collectAsState()
      val offBudget by mutableOffBudget.collectAsState()
      val isNegative by mutableIsNegative.collectAsState()
      val existingNames by mutableExistingNames.collectAsState()
      val isSaving by mutableIsSaving.collectAsState()
      val saveError by mutableSaveError.collectAsState()

      val trimmed = name.trim()
      CreateAccountState(
        offBudget = offBudget,
        isNegative = isNegative,
        duplicateName = trimmed.takeIf { it in existingNames },
        isBalanceValid = evaluateAmountInput(balance) != null,
        canCreate = canCreate(name, balance, existingNames, isSaving),
        isSaving = isSaving,
        saveError = saveError,
      )
    }

  init {
    viewModelScope.launch {
      val names = runCatchingCancellable {
        accountDao.getAllWithStatus().mapNotNull { it.name }.toPersistentSet()
      }
        .getOrElse { e ->
          logcat.e(e) { "Failed loading account names" }
          persistentSetOf()
        }
      mutableExistingNames.update { names }
    }
  }

  fun setName(name: String) = mutableName.update { name }

  fun setBalance(balance: String) = mutableBalance.update { balance }

  fun setOffBudget(offBudget: Boolean) = mutableOffBudget.update { offBudget }

  fun setNegative(negative: Boolean) = mutableIsNegative.update { negative }

  fun dismissSaveError() = mutableSaveError.update { null }

  fun create() {
    // Not state.value, which may not have caught up with the latest input yet
    val isValid =
      canCreate(
        mutableName.value,
        mutableBalance.value,
        mutableExistingNames.value,
        mutableIsSaving.value,
      )
    if (!isValid) return
    val name = mutableName.value.trim()
    val typed = evaluateAmountInput(mutableBalance.value) ?: return
    val amount = if (mutableIsNegative.value) -typed else typed
    val offBudget = mutableOffBudget.value
    mutableIsSaving.update { true }

    viewModelScope.launch {
      runCatchingCancellable {
        val id = writer.write { createAccount(name, offBudget, amount, calendar.today()) }
        // Stays saving, so it can't be created twice before the screen closes
        eventChannel.send(CreateAccountEvent.Created(id))
      }
        .onFailure { e ->
          logcat.e(e) { "Failed creating account $name" }
          mutableSaveError.update { e.requireMessage() }
          mutableIsSaving.update { false }
        }
    }
  }

  private fun canCreate(
    name: String,
    balance: String,
    existingNames: Set<String>,
    isSaving: Boolean,
  ): Boolean {
    val trimmed = name.trim()
    return trimmed.isNotEmpty() &&
      trimmed !in existingNames &&
      evaluateAmountInput(balance) != null &&
      !isSaving
  }
}
