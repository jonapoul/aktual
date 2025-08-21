package actual.budget.transactions.vm

import actual.budget.db.dao.AccountsDao
import actual.budget.db.dao.PreferencesDao
import actual.budget.db.dao.TransactionsDao
import actual.budget.db.transactions.GetById
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
import actual.core.di.AssistedFactoryKey
import actual.core.di.BudgetGraphHolder
import actual.core.di.ViewModelAssistedFactory
import actual.core.di.ViewModelScope
import actual.core.di.throwIfWrongBudget
import actual.core.model.LoginToken
import alakazam.kotlin.core.CoroutineContexts
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

@Inject
class TransactionsViewModel(
  @Suppress("unused") @Assisted private val token: LoginToken,
  @Assisted private val budgetId: BudgetId,
  @Assisted private val spec: TransactionsSpec,
  budgetGraphs: BudgetGraphHolder,
  contexts: CoroutineContexts,
) : ViewModel() {
  // data sources
  private val budgetGraph = budgetGraphs.require()
  private val accountsDao = AccountsDao(budgetGraph.database, contexts)
  private val transactionsDao = TransactionsDao(budgetGraph.database, contexts)
  private val prefs = budgetGraph.localPreferences
  private val syncedPrefs = PreferencesDao(budgetGraph.database, contexts)

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

  val transactions: StateFlow<ImmutableList<DatedTransactions>> = getIdsFlow()
    .distinctUntilChanged()
    .map(::toDatedTransactions)
    .stateIn(viewModelScope, Eagerly, initialValue = persistentListOf())

  val sorting: StateFlow<TransactionsSorting> = prefs
    .map(::TransactionsSorting)
    .stateIn(viewModelScope, Eagerly, initialValue = TransactionsSorting.Default)

  init {
    budgetGraph.throwIfWrongBudget(budgetId)

    when (val spec = spec.accountSpec) {
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
    .filterNotNull()
    .distinctUntilChanged()
    .map(::toTransaction)

  private fun getIdsFlow(): Flow<List<DatedId>> = when (val spec = spec.accountSpec) {
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

  @AssistedFactory
  @AssistedFactoryKey(Factory::class)
  @ContributesIntoMap(ViewModelScope::class)
  fun interface Factory : ViewModelAssistedFactory {
    fun create(
      @Assisted token: LoginToken,
      @Assisted budgetId: BudgetId,
      @Assisted spec: TransactionsSpec,
    ): TransactionsViewModel
  }
}
