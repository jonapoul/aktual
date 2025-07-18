package actual.budget.transactions.vm

import actual.account.model.LoginToken
import actual.budget.db.dao.AccountsDao
import actual.budget.db.dao.PreferencesDao
import actual.budget.db.dao.TransactionsDao
import actual.budget.db.transactions.GetById
import actual.budget.di.BudgetComponentStateHolder
import actual.budget.model.AccountSpec
import actual.budget.model.Amount
import actual.budget.model.BudgetId
import actual.budget.model.SyncedPrefKey
import actual.budget.model.TransactionId
import actual.budget.model.TransactionsFormat
import actual.budget.model.TransactionsSpec
import actual.budget.transactions.vm.LoadedAccount.AllAccounts
import actual.budget.transactions.vm.LoadedAccount.Loading
import actual.budget.transactions.vm.LoadedAccount.SpecificAccount
import alakazam.kotlin.core.CoroutineContexts
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel(assistedFactory = TransactionsViewModel.Factory::class)
class TransactionsViewModel @AssistedInject constructor(
  @Assisted inputs: Inputs,
  components: BudgetComponentStateHolder,
  contexts: CoroutineContexts,
) : ViewModel() {
  // data sources
  private val component = components.require()
  private val accountsDao = AccountsDao(component.database, contexts)
  private val transactionsDao = TransactionsDao(component.database, contexts)
  private val prefs = component.localPreferences
  private val syncedPrefs = PreferencesDao(component.database, contexts)

  // local data
  private val mutableLoadedAccount = MutableStateFlow<LoadedAccount>(Loading)
  private val expandedGroups = MutableStateFlow(persistentMapOf<LocalDate, Boolean>())
  private val expandAllGroups = MutableStateFlow(false)
  private val checkedTransactionIds = MutableStateFlow(persistentMapOf<TransactionId, Boolean>())

  // API
  val loadedAccount: StateFlow<LoadedAccount> = mutableLoadedAccount.asStateFlow()

  val format: StateFlow<TransactionsFormat> = prefs
    .map { meta -> meta[TransactionFormatDelegate] }
    .stateIn(viewModelScope, Eagerly, initialValue = TransactionsFormat.Default)

  val transactions: StateFlow<ImmutableList<DatedTransactions>> = getIdsFlow(inputs)
    .distinctUntilChanged()
    .map(::toDatedTransactions)
    .stateIn(viewModelScope, Eagerly, initialValue = persistentListOf())

  val sorting: StateFlow<TransactionsSorting> = prefs
    .map(::TransactionsSorting)
    .stateIn(viewModelScope, Eagerly, initialValue = TransactionsSorting.Default)

  init {
    require(component.budgetId == inputs.budgetId) {
      "Loading from the wrong budget! Expected ${inputs.budgetId}, got ${component.budgetId}"
    }

    when (val spec = inputs.spec.accountSpec) {
      is AccountSpec.AllAccounts -> mutableLoadedAccount.update { AllAccounts }
      is AccountSpec.SpecificAccount -> viewModelScope.launch {
        val account = accountsDao[spec.id] ?: error("No account matching $spec")
        mutableLoadedAccount.update { SpecificAccount(account) }
      }
    }
  }

  fun setFormat(format: TransactionsFormat) {
    prefs.update { meta -> meta.set(TransactionFormatDelegate, format) }
  }

  fun isChecked(id: TransactionId): Flow<Boolean> = checkedTransactionIds.map { it.getOrDefault(id, false) }

  fun isExpanded(date: LocalDate): Flow<Boolean> = combine(
    expandAllGroups,
    expandedGroups,
    transform = { expandAll, expanded -> expandAll || expanded.getOrDefault(date, true) },
  )

  fun setChecked(id: TransactionId, isChecked: Boolean) = checkedTransactionIds.update { it.put(id, isChecked) }

  fun setExpanded(date: LocalDate, isExpanded: Boolean) = expandedGroups.update { it.put(date, isExpanded) }

  fun setPrivacyMode(privacyMode: Boolean) {
    viewModelScope.launch {
      syncedPrefs[SyncedPrefKey.Global.IsPrivacyEnabled] = privacyMode.toString()
    }
  }

  fun expandAll(expand: Boolean) {
    expandAllGroups.update { expand }
    if (expand) expandedGroups.update { persistentMapOf() }
  }

  fun observe(id: TransactionId): Flow<Transaction> = transactionsDao
    .observeById(id)
    .onEach { println("observe $id = $it") }
    .filterNotNull()
    .distinctUntilChanged()
    .map(::toTransaction)

  private fun getIdsFlow(inputs: Inputs): Flow<List<DatedId>> = when (val spec = inputs.spec.accountSpec) {
    AccountSpec.AllAccounts ->
      transactionsDao
        .observeIds()
        .map { list -> list.map { (id, date) -> DatedId(id, date) } }

    is AccountSpec.SpecificAccount ->
      transactionsDao
        .observeIdsByAccount(spec.id)
        .map { list -> list.map { (id, date) -> DatedId(id, date) } }
  }

  private fun toDatedTransactions(datedIds: List<DatedId>) = datedIds
    .groupBy { it.date }
    .map { (date, datedIds) -> DatedTransactions(date, datedIds.map { it.id }.toImmutableList()) }
    .toImmutableList()

  private fun toTransaction(data: GetById): Transaction = with(data) {
    Transaction(
      id = id,
      date = date,
      account = accountName,
      payee = payeeName,
      notes = notes,
      category = categoryName,
      amount = Amount(amount),
    )
  }

  private data class DatedId(val id: TransactionId, val date: LocalDate)

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
