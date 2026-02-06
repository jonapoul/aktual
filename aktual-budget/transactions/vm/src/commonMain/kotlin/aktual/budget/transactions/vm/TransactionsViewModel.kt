package aktual.budget.transactions.vm

import aktual.budget.db.dao.AccountsDao
import aktual.budget.db.dao.PreferencesDao
import aktual.budget.db.dao.TransactionsDao
import aktual.budget.db.transactions.GetById
import aktual.budget.model.AccountSpec
import aktual.budget.model.Amount
import aktual.budget.model.BudgetId
import aktual.budget.model.DbMetadata
import aktual.budget.model.SyncedPrefKey
import aktual.budget.model.TransactionId
import aktual.budget.model.TransactionsFormat
import aktual.budget.model.TransactionsSpec
import aktual.budget.transactions.vm.LoadedAccount.AllAccounts
import aktual.budget.transactions.vm.LoadedAccount.Loading
import aktual.budget.transactions.vm.LoadedAccount.SpecificAccount
import aktual.core.di.BudgetGraphHolder
import aktual.core.model.Token
import alakazam.kotlin.CoroutineContexts
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.cachedIn
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logcat.logcat

@AssistedInject
class TransactionsViewModel(
  @Suppress("unused") @Assisted private val token: Token,
  @Assisted private val budgetId: BudgetId,
  @Assisted private val spec: TransactionsSpec,
  budgetGraphs: BudgetGraphHolder,
  contexts: CoroutineContexts,
) : ViewModel(), TransactionStateSource, TransactionIdSource {
  @AssistedFactory
  @ManualViewModelAssistedFactoryKey(Factory::class)
  @ContributesIntoMap(AppScope::class)
  fun interface Factory : ManualViewModelAssistedFactory {
    fun create(
      @Assisted token: Token,
      @Assisted budgetId: BudgetId,
      @Assisted spec: TransactionsSpec,
    ): TransactionsViewModel
  }

  // data sources
  private val budgetGraph = budgetGraphs.require()
  private val accountsDao = AccountsDao(budgetGraph.database, contexts)
  private val transactionsDao = TransactionsDao(budgetGraph.database, contexts)
  private val prefs = budgetGraph.localPreferences
  private val syncedPrefs = PreferencesDao(budgetGraph.database, contexts)

  // local data
  private val mutableLoadedAccount = MutableStateFlow<LoadedAccount>(Loading)
  private val checkedTransactionIds = MutableStateFlow(persistentMapOf<TransactionId, Boolean>())
  private var currentPagingSource: PagingSource<Int, TransactionId>? = null

  // API
  val loadedAccount: StateFlow<LoadedAccount> = mutableLoadedAccount.asStateFlow()

  val format: StateFlow<TransactionsFormat> = prefs
    .map { meta -> meta[TransactionFormatKey] ?: TransactionsFormat.Default }
    .stateIn(viewModelScope, Eagerly, initialValue = TransactionsFormat.Default)

  override val pagingData: Flow<PagingData<TransactionId>> = Pager(
    config = PagingConfig(pageSize = PAGING_SIZE, enablePlaceholders = false),
    pagingSourceFactory = ::buildPagingSource,
  ).flow.cachedIn(viewModelScope)

  init {
    budgetGraph.throwIfWrongBudget(budgetId)

    when (val s = spec.accountSpec) {
      is AccountSpec.AllAccounts -> mutableLoadedAccount.update { AllAccounts }

      is AccountSpec.SpecificAccount -> viewModelScope.launch {
        val account = accountsDao[s.id] ?: error("No account matching $s")
        mutableLoadedAccount.update { SpecificAccount(account) }
      }
    }

    // Invalidate PagingSource when transaction data changes.
    // Ignore the first item from the flow, that'll be the initial table state.
    viewModelScope.launch {
      val countFlow = when (val s = spec.accountSpec) {
        AccountSpec.AllAccounts -> transactionsDao.observeCount()
        is AccountSpec.SpecificAccount -> transactionsDao.observeCountByAccount(s.id)
      }
      countFlow.drop(count = 1).collect {
        logcat.d { "Transactions table updated, invalidating paging source..." }
        currentPagingSource?.invalidate()
      }
    }
  }

  fun setFormat(format: TransactionsFormat) {
    prefs.update { meta -> meta.set(TransactionFormatKey, format) }
  }

  override fun isChecked(id: TransactionId): Flow<Boolean> =
    checkedTransactionIds.map { it.getOrDefault(id, false) }

  fun setChecked(id: TransactionId, isChecked: Boolean) = checkedTransactionIds.update { it.put(id, isChecked) }

  fun setPrivacyMode(privacyMode: Boolean) {
    viewModelScope.launch {
      syncedPrefs[SyncedPrefKey.Global.IsPrivacyEnabled] = privacyMode.toString()
    }
  }

  override fun transactionState(id: TransactionId) = transactionsDao
    .observeById(id)
    .also { logcat.d { "Observing transaction with ID $id" } }
    .distinctUntilChanged()
    .map { toTransactionState(it, id) }

  private fun toTransactionState(data: GetById?, id: TransactionId): TransactionState {
    if (data == null) return TransactionState.DoesntExist(id)
    val transaction = with(data) {
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
    return TransactionState.Loaded(transaction)
  }

  private fun buildPagingSource() = TransactionsPagingSource(transactionsDao, spec.accountSpec)
    .also { currentPagingSource = it }

  private companion object {
    val TransactionFormatKey = DbMetadata.enumKey<TransactionsFormat>("transactionFormat")
  }
}
