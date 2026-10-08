package aktual.budget.transactions.vm

import aktual.budget.BudgetLocalPreferences
import aktual.budget.banksync.domain.BankSyncController
import aktual.budget.banksync.domain.BankSyncSummary
import aktual.budget.db.Accounts
import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.PreferencesDao
import aktual.budget.db.dao.TagsDao
import aktual.budget.db.dao.TransactionDao
import aktual.budget.model.AccountSpec
import aktual.budget.model.Amount
import aktual.budget.model.CategorySpec
import aktual.budget.model.DbMetadata
import aktual.budget.model.SyncedPrefKey
import aktual.budget.model.TagSpec
import aktual.budget.model.TransactionId
import aktual.budget.model.TransactionsDensity
import aktual.budget.model.TransactionsSpec
import aktual.budget.transactions.vm.LoadedAccount.AllAccounts
import aktual.budget.transactions.vm.LoadedAccount.Loading
import aktual.budget.transactions.vm.LoadedAccount.SpecificAccount
import aktual.budget.transactions.vm.LoadedAccount.SpecificTag
import aktual.budget.transactions.vm.LoadedAccount.Uncategorised
import aktual.core.model.BudgetServer
import aktual.di.BudgetScope
import aktual.prefs.TransactionPreferences
import aktual.prefs.asStateFlow
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.cachedIn
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logcat.logcat

@Stable
@AssistedInject
class TransactionsViewModel(
  @Assisted private val spec: TransactionsSpec,
  private val prefs: BudgetLocalPreferences,
  private val accountDao: AccountDao,
  private val transactionDao: TransactionDao,
  private val tagsDao: TagsDao,
  private val preferencesDao: PreferencesDao,
  private val bankSyncController: BankSyncController,
  transactionPreferences: TransactionPreferences,
  server: BudgetServer,
) : ViewModel() {
  @AssistedFactory
  @ManualViewModelAssistedFactoryKey
  @ContributesIntoMap(BudgetScope::class)
  fun interface Factory : ManualViewModelAssistedFactory {
    fun create(@Assisted spec: TransactionsSpec): TransactionsViewModel
  }

  private val mutableLoadedAccount = MutableStateFlow<LoadedAccount>(Loading)
  private val mutableExpanded = MutableStateFlow<PersistentSet<TransactionId>>(persistentSetOf())
  private val accountId = (spec.accountSpec as? AccountSpec.SpecificAccount)?.id
  private val isRemote = server is Remote
  private var currentPagingSource: PagingSource<Int, Transaction>? = null

  val loadedAccount: StateFlow<LoadedAccount> = mutableLoadedAccount.asStateFlow()

  val density: StateFlow<TransactionsDensity> =
    prefs
      .map { meta -> meta[TransactionDensityKey] ?: Default }
      .stateIn(viewModelScope, Eagerly, initialValue = prefs[TransactionDensityKey] ?: Default)

  val alternateRowColours: StateFlow<Boolean> =
    transactionPreferences.alternateRowColours.asStateFlow(viewModelScope)

  val canBankSync: StateFlow<Boolean> =
    mutableLoadedAccount
      .map { it is SpecificAccount && it.account.isLinked() && isRemote }
      .stateIn(viewModelScope, Eagerly, initialValue = false)

  val isBankSyncing: StateFlow<Boolean> =
    bankSyncController.progress
      .map { it.isRunning && accountId in it.pending }
      .stateIn(viewModelScope, Eagerly, initialValue = false)

  val bankSyncFinished: Flow<BankSyncSummary> =
    bankSyncController.finished.mapNotNull { results ->
      BankSyncSummary.of(results.filter { it.account == accountId })
    }

  // A filtered list only holds part of each account, so it has no balance to show
  val showBalance: Boolean = spec.tagSpec is AllTags && spec.categorySpec == AllCategories

  val balance: StateFlow<Amount?> =
    if (showBalance) {
      transactionDao
        .observeBalance(accountId)
        .map(::Amount)
        .stateIn(viewModelScope, Eagerly, initialValue = null)
    } else {
      MutableStateFlow<Amount?>(null)
    }

  // A tag list shows the matching parts of each split, with nothing to toggle
  val splitsPinnedOpen: Boolean = spec.tagSpec is TagSpec.SpecificTag

  // The splits showing their parts. Kept out of the paging data so a toggle doesn't reload the
  // page, and survives an invalidation
  val expanded: StateFlow<ImmutableSet<TransactionId>> = mutableExpanded.asStateFlow()

  val pagingData: Flow<PagingData<Transaction>> =
    Pager(
        config = PagingConfig(pageSize = PAGING_SIZE, enablePlaceholders = false),
        pagingSourceFactory = ::buildPagingSource,
      )
      .flow
      .cachedIn(viewModelScope)

  init {
    // A tag-filtered screen titles itself after the tag, then an uncategorised one after that
    // filter; otherwise the title follows the account.
    val tagSpec = spec.tagSpec
    val accountSpec = spec.accountSpec
    when {
      tagSpec is TagSpec.SpecificTag ->
        viewModelScope.launch {
          val name = tagsDao.getTag(tagSpec.id)?.tag
          mutableLoadedAccount.update { if (name != null) SpecificTag(name) else AllAccounts }
        }

      spec.categorySpec == CategorySpec.Uncategorised ->
        mutableLoadedAccount.update { Uncategorised }

      accountSpec is AccountSpec.SpecificAccount ->
        viewModelScope.launch {
          val account = accountDao[accountSpec.id] ?: error("No account matching $accountSpec")
          mutableLoadedAccount.update { SpecificAccount(account) }
        }

      else -> mutableLoadedAccount.update { AllAccounts }
    }

    // Invalidate PagingSource when transaction data changes.
    // Ignore the first item from the flow, that'll be the initial table state.
    viewModelScope.launch {
      transactionDao.observeChanges().drop(count = 1).collect {
        logcat.d { "Transactions table updated, invalidating paging source..." }
        currentPagingSource?.invalidate()
      }
    }
  }

  fun setDensity(density: TransactionsDensity) {
    prefs.update { meta -> meta.set(TransactionDensityKey, density) }
  }

  fun toggleExpanded(id: TransactionId) {
    mutableExpanded.update { ids -> if (id in ids) ids.removing(id) else ids.adding(id) }
  }

  fun bankSync() {
    val account = accountId ?: return
    if (!bankSyncController.start(setOf(account))) logcat.d { "Bank sync already running" }
  }

  fun setPrivacyMode(privacyMode: Boolean) {
    viewModelScope.launch {
      preferencesDao[SyncedPrefKey.Global.IsPrivacyEnabled] = privacyMode.toString()
    }
  }

  private fun buildPagingSource() =
    TransactionsPagingSource(transactionDao, tagsDao, spec).also { currentPagingSource = it }

  // As the bank sync screen decides it
  private fun Accounts.isLinked() = account_sync_source != null && account_id != null

  private companion object {
    val TransactionDensityKey = DbMetadata.enumKey<TransactionsDensity>("transactionDensity")
  }
}
