package aktual.budget.accounts.vm

import aktual.budget.accounts.vm.manage.AccountGroupItem
import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.AccountGroupDao
import aktual.budget.model.AccountGroupId
import aktual.budget.model.evaluateAmountInput
import aktual.budget.transactions.domain.TransactionWriter
import aktual.core.utils.Calendar
import aktual.core.utils.runCatchingCancellable
import aktual.di.BudgetScope
import aktual.di.ViewModelContext
import aktual.di.newScope
import aktual.log
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
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Creates a local account, as upstream's CreateLocalAccountModal. The name must be unique among
 * accounts that aren't deleted, closed ones included. See validateAccountName(). It can go straight
 * into an account group.
 */
@Stable
@ViewModelKey
@ContributesIntoMap(BudgetScope::class)
class CreateAccountViewModel
internal constructor(
  private val writer: TransactionWriter,
  private val accountDao: AccountDao,
  groupDao: AccountGroupDao,
  private val calendar: Calendar,
  viewModelContext: ViewModelContext,
) : ViewModel(viewModelContext.newScope()) {
  private val mutableName = MutableStateFlow("")
  private val mutableBalance = MutableStateFlow("")
  private val mutableOffBudget = MutableStateFlow(false)
  private val mutableGroup = MutableStateFlow<AccountGroupId?>(null)
  private val mutableIsNegative = MutableStateFlow(false)
  private val mutableExistingNames = MutableStateFlow(persistentSetOf<String>())
  private val mutableIsSaving = MutableStateFlow(false)
  private val mutableSaveError = MutableStateFlow<String?>(null)

  private val groups: Flow<ImmutableList<AccountGroupItem>> =
    groupDao
      .observeAllActive()
      .map { rows -> rows.map { AccountGroupItem(it.id, it.name.orEmpty()) }.toImmutableList() }
      .catch { e ->
        log.e(e) { "Failed loading account groups" }
        emit(persistentListOf())
      }

  // Buffered, so the event waits for the screen if it's stopped when the account is created
  private val eventChannel = Channel<CreateAccountEvent>(BUFFERED)
  val events: Flow<CreateAccountEvent> = eventChannel.receiveAsFlow()

  val state: StateFlow<CreateAccountState> =
    viewModelScope.launchMolecule(Immediate) {
      val name by mutableName.collectAsState()
      val balance by mutableBalance.collectAsState()
      val offBudget by mutableOffBudget.collectAsState()
      val group by mutableGroup.collectAsState()
      val groups by groups.collectAsState(persistentListOf())
      val isNegative by mutableIsNegative.collectAsState()
      val existingNames by mutableExistingNames.collectAsState()
      val isSaving by mutableIsSaving.collectAsState()
      val saveError by mutableSaveError.collectAsState()

      val trimmed = name.trim()
      CreateAccountState(
        offBudget = offBudget,
        // A group deleted since it was picked goes back to Ungrouped
        group = group?.takeIf { id -> groups.any { it.id == id } },
        groups = groups,
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
      val names =
        runCatchingCancellable(
          attempt = { accountDao.getAllWithStatus().mapNotNull { it.name }.toPersistentSet() },
          onFailure = { e ->
            log.e(e) { "Failed loading account names" }
            persistentSetOf()
          },
        )
      mutableExistingNames.update { names }
    }
  }

  fun setName(name: String) = mutableName.update { name }

  fun setBalance(balance: String) = mutableBalance.update { balance }

  fun setOffBudget(offBudget: Boolean) = mutableOffBudget.update { offBudget }

  fun setGroup(group: AccountGroupId?) = mutableGroup.update { group }

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
    val group = mutableGroup.value?.takeIf { id -> state.value.groups.any { it.id == id } }
    mutableIsSaving.update { true }

    viewModelScope.launch {
      runCatchingCancellable(
        attempt = {
          val id = writer.write { createAccount(name, offBudget, amount, calendar.today(), group) }
          // Stays saving, so it can't be created twice before the screen closes
          eventChannel.send(CreateAccountEvent.Created(id))
        },
        onFailure = { e ->
          log.e(e) { "Failed creating account $name" }
          mutableSaveError.update { e.requireMessage() }
          mutableIsSaving.update { false }
        },
      )
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
