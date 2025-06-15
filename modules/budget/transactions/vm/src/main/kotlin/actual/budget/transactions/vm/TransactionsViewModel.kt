package actual.budget.transactions.vm

import actual.account.model.LoginToken
import actual.budget.db.dao.AccountsDao
import actual.budget.di.BudgetComponentStateHolder
import actual.budget.model.AccountSpec
import actual.budget.model.BudgetId
import actual.budget.model.TransactionId
import actual.budget.model.TransactionsFormat
import actual.budget.model.TransactionsSpec
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel(assistedFactory = TransactionsViewModel.Factory::class)
class TransactionsViewModel @AssistedInject constructor(
  @Assisted inputs: Inputs,
  components: BudgetComponentStateHolder,
) : ViewModel() {
  // data sources
  private val component = components.require()
  private val accountsDao = component.database.let(::AccountsDao)
  private val prefs = component.localPreferences

  // local data
  private val mutableLoadedAccount = MutableStateFlow<LoadedAccount>(LoadedAccount.Loading)
  private val expandedGroups = MutableStateFlow(persistentMapOf<LocalDate, Boolean>())
  private val expandAllGroups = MutableStateFlow(false)
  private val checkedTransactionIds = MutableStateFlow(persistentMapOf<TransactionId, Boolean>())

  // API
  val loadedAccount: StateFlow<LoadedAccount> = mutableLoadedAccount.asStateFlow()

  val format: StateFlow<TransactionsFormat> = prefs
    .map { it.transactionFormat.get() }
    .stateIn(viewModelScope, Eagerly, initialValue = TransactionsFormat.Default)

  val transactions: StateFlow<ImmutableList<DatedTransactions>> = MutableStateFlow(persistentListOf())

  val sorting: StateFlow<TransactionsSorting> = prefs
    .map(::TransactionsSorting)
    .stateIn(viewModelScope, Eagerly, initialValue = TransactionsSorting.Default)

  init {
    require(component.budgetId == inputs.budgetId) {
      "Loading from the wrong budget! Expected ${inputs.budgetId}, got ${component.budgetId}"
    }

    when (val spec = inputs.spec.accountSpec) {
      is AccountSpec.AllAccounts -> mutableLoadedAccount.update { LoadedAccount.AllAccounts }
      is AccountSpec.SpecificAccount -> viewModelScope.launch {
        val account = accountsDao[spec.id] ?: error("No account matching $spec")
        mutableLoadedAccount.update { LoadedAccount.SpecificAccount(account) }
      }
    }
  }

  fun setFormat(format: TransactionsFormat) {
    prefs.update { it.transactionFormat.set(format) }
  }

  fun isChecked(id: TransactionId): Flow<Boolean> = checkedTransactionIds.map { it.getOrDefault(id, false) }

  fun isExpanded(date: LocalDate): Flow<Boolean> = combine(
    expandAllGroups,
    expandedGroups,
    transform = { expandAll, expanded -> expandAll || expanded.getOrDefault(date, true) },
  )

  fun setChecked(id: TransactionId, isChecked: Boolean) = checkedTransactionIds.update { it.put(id, isChecked) }

  fun setExpanded(date: LocalDate, isExpanded: Boolean) = expandedGroups.update { it.put(date, isExpanded) }

  fun expandAll(expand: Boolean) {
    expandAllGroups.update { expand }
    if (expand) expandedGroups.update { persistentMapOf() }
  }

  data class Inputs(
    val token: LoginToken,
    val budgetId: BudgetId,
    val spec: TransactionsSpec,
  )

  @AssistedFactory
  fun interface Factory {
    fun create(
      @Assisted inputs: Inputs,
    ): TransactionsViewModel
  }
}
