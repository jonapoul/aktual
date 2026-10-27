package aktual.budget.transactions.vm

import aktual.budget.BudgetLocalPreferences
import aktual.budget.banksync.domain.AccountDeleter
import aktual.budget.banksync.domain.BankSyncController
import aktual.budget.banksync.domain.BankSyncSummary
import aktual.budget.banksync.domain.NewTransactions
import aktual.budget.db.Accounts
import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.CategoryDao
import aktual.budget.db.dao.ClearedBalances
import aktual.budget.db.dao.TagsDao
import aktual.budget.db.dao.TransactionDao
import aktual.budget.model.AccountSpec
import aktual.budget.model.Amount
import aktual.budget.model.CategorySpec
import aktual.budget.model.DbMetadataKey.Companion.TransactionDensity
import aktual.budget.model.SyncedPrefKey.PerAccount.HideReconciled
import aktual.budget.model.SyncedPrefKey.PerAccount.ShowBalances
import aktual.budget.model.SyncedPrefKey.PerAccount.ShowExtraBalances
import aktual.budget.model.TagSpec
import aktual.budget.model.TransactionId
import aktual.budget.model.TransactionsDensity
import aktual.budget.model.TransactionsSpec
import aktual.budget.tags.domain.TagLabel
import aktual.budget.tags.domain.toTagLabels
import aktual.budget.transactions.domain.AccountViewPrefs
import aktual.budget.transactions.vm.LoadedAccount.AllAccounts
import aktual.budget.transactions.vm.LoadedAccount.Loading
import aktual.budget.transactions.vm.LoadedAccount.SpecificAccount
import aktual.budget.transactions.vm.LoadedAccount.SpecificCategory
import aktual.budget.transactions.vm.LoadedAccount.SpecificTag
import aktual.budget.transactions.vm.LoadedAccount.Uncategorised
import aktual.core.model.BudgetServer
import aktual.di.BudgetScope
import aktual.di.ViewModelContext
import aktual.di.newScope
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
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logcat.logcat

@Stable
@AssistedInject
@Suppress("LongParameterList")
class TransactionsViewModel(
  @Assisted private val spec: TransactionsSpec,
  private val prefs: BudgetLocalPreferences,
  private val accountDao: AccountDao,
  private val transactionDao: TransactionDao,
  private val tagsDao: TagsDao,
  private val categoryDao: CategoryDao,
  private val bankSyncController: BankSyncController,
  private val deleter: AccountDeleter,
  private val viewPrefs: AccountViewPrefs,
  private val transactionPreferences: TransactionPreferences,
  newTransactions: NewTransactions,
  server: BudgetServer,
  viewModelContext: ViewModelContext,
) : ViewModel(viewModelContext.newScope()) {
  @AssistedFactory
  @ManualViewModelAssistedFactoryKey
  @ContributesIntoMap(BudgetScope::class)
  fun interface Factory : ManualViewModelAssistedFactory {
    fun create(@Assisted spec: TransactionsSpec): TransactionsViewModel
  }

  private val mutableLoadedAccount = MutableStateFlow<LoadedAccount>(Loading)
  private val mutableToggledSplits =
    MutableStateFlow<PersistentSet<TransactionId>>(persistentSetOf())
  private val accountId = (spec.accountSpec as? AccountSpec.SpecificAccount)?.id
  private val isRemote = server is Remote
  private var currentPagingSource: PagingSource<Int, Transaction>? = null
  // Buffered, so an event sent while the screen is being recreated waits for it
  private val mutableEvents = Channel<TransactionsEvent>(Channel.BUFFERED)
  private var deleteJob: Job? = null

  val loadedAccount: StateFlow<LoadedAccount> = mutableLoadedAccount.asStateFlow()

  val events: Flow<TransactionsEvent> = mutableEvents.receiveAsFlow()

  val density: StateFlow<TransactionsDensity> =
    prefs
      .map { meta -> meta[TransactionDensity] ?: Default }
      .stateIn(
        viewModelScope,
        Eagerly,
        initialValue = prefs[TransactionDensity] ?: Default,
      )

  // Colours the #tags in each row's notes
  val tags: StateFlow<ImmutableList<TagLabel>> =
    tagsDao
      .observeTags()
      .map { rows -> rows.toTagLabels() }
      .catch { e ->
        logcat.w(e) { "Failed loading tags" }
        emit(persistentListOf())
      }
      .stateIn(viewModelScope, Eagerly, persistentListOf())

  val alternateRowColours: StateFlow<Boolean> =
    transactionPreferences.alternateRowColours.asStateFlow(viewModelScope)

  val expandSplits: StateFlow<Boolean> =
    transactionPreferences.expandSplits.asStateFlow(viewModelScope)

  // Null unless the list is a single account's, the only list these options apply to
  val accountOptions: StateFlow<AccountViewOptions?> =
    if (accountId == null) {
      MutableStateFlow(null)
    } else {
      combine(
          viewPrefs.observe(ShowBalances(accountId)),
          viewPrefs.observe(HideReconciled(accountId)),
          viewPrefs.observe(ShowExtraBalances(accountId)),
          ::AccountViewOptions,
        )
        .stateIn(viewModelScope, Eagerly, initialValue = null)
    }

  // An account's list shows a running balance if its option says so. Other lists that have a
  // balance always show it
  val showRunningBalance: StateFlow<Boolean> =
    accountOptions
      .map { options -> options?.showRunningBalance ?: (accountId == null) }
      .stateIn(viewModelScope, Eagerly, initialValue = accountId == null)

  val clearedBalances: StateFlow<ClearedBalances?> =
    accountOptions
      .map { it?.showExtraBalances == true }
      .distinctUntilChanged()
      .flatMapLatest { show ->
        if (show && accountId != null) {
          transactionDao.observeClearedBalances(accountId)
        } else {
          flowOf(null)
        }
      }
      .stateIn(viewModelScope, Eagerly, initialValue = null)

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
  val showBalance: Boolean =
    spec.tagSpec is AllTags && spec.categorySpec == AllCategories && spec.filtered == null

  val balance: StateFlow<Amount?> =
    if (showBalance) {
      observeBalance().map(::Amount).stateIn(viewModelScope, Eagerly, initialValue = null)
    } else {
      MutableStateFlow(null)
    }

  // A tag list shows the matching parts of each split, with nothing to toggle
  val splitsPinnedOpen: Boolean = spec.tagSpec is TagSpec.SpecificTag

  // The splits toggled away from expandSplits: open ones if splits start closed, and closed ones if
  // they start open. Kept out of the paging data so a toggle doesn't reload the page, and survives
  // an invalidation
  val toggledSplits: StateFlow<ImmutableSet<TransactionId>> = mutableToggledSplits.asStateFlow()

  val pagingData: Flow<PagingData<Transaction>> =
    Pager(
        config = PagingConfig(pageSize = PAGING_SIZE, enablePlaceholders = false),
        pagingSourceFactory = ::buildPagingSource,
      )
      .flow
      .cachedIn(viewModelScope)

  init {
    // A report's drilled down list has the title it was given. A tag-filtered screen titles itself
    // after the tag, then an uncategorised or category one after that filter; otherwise the title
    // follows the account.
    val tagSpec = spec.tagSpec
    val accountSpec = spec.accountSpec
    val categorySpec = spec.categorySpec
    val filtered = spec.filtered
    when {
      filtered != null -> mutableLoadedAccount.update { LoadedAccount.Filtered(filtered.title) }

      tagSpec is TagSpec.SpecificTag ->
        viewModelScope.launch {
          val name = tagsDao.getTag(tagSpec.id)?.tag
          mutableLoadedAccount.update { if (name != null) SpecificTag(name) else AllAccounts }
        }

      spec.categorySpec == CategorySpec.Uncategorised ->
        mutableLoadedAccount.update { Uncategorised }

      categorySpec is CategorySpec.SpecificCategory ->
        viewModelScope.launch {
          val name = categoryDao.name(categorySpec.id)
          mutableLoadedAccount.update {
            if (name != null) SpecificCategory(name, categorySpec.month) else AllAccounts
          }
        }

      accountSpec is AccountSpec.SpecificAccount ->
        viewModelScope.launch {
          val account = accountDao[accountSpec.id] ?: error("No account matching $accountSpec")
          mutableLoadedAccount.update { SpecificAccount(account) }
        }

      accountSpec is Group -> mutableLoadedAccount.update { LoadedAccount.Group(accountSpec.group) }

      else -> mutableLoadedAccount.update { AllAccounts }
    }

    // Toggles were made against the old default, so they start over with the new one
    viewModelScope.launch {
      expandSplits.drop(count = 1).collect { mutableToggledSplits.update { persistentSetOf() } }
    }

    // Hiding reconciled transactions changes the rows in the list
    viewModelScope.launch {
      accountOptions
        .map { it?.hideReconciled == true }
        .distinctUntilChanged()
        .drop(count = 1)
        .collect { currentPagingSource?.invalidate() }
    }

    // Invalidate PagingSource when transaction data changes.
    // Ignore the first item from the flow, that'll be the initial table state.
    viewModelScope.launch {
      transactionDao.observeChanges().drop(count = 1).collect {
        logcat.d { "Transactions table updated, invalidating paging source..." }
        currentPagingSource?.invalidate()
      }
    }

    // The account's new transactions are seen while it's open, even ones a sync adds meanwhile
    if (accountId != null) {
      viewModelScope.launch {
        newTransactions.counts
          .filter { accountId in it }
          .collect {
            try {
              newTransactions.markSeen(accountId)
            } catch (e: CancellationException) {
              throw e
            } catch (e: Exception) {
              logcat.w(e) { "Failed marking $accountId seen" }
            }
          }
      }
    }
  }

  private fun observeBalance(): Flow<Long> =
    when (val accountSpec = spec.accountSpec) {
      AccountSpec.AllAccounts -> transactionDao.observeBalance()
      is AccountSpec.SpecificAccount -> transactionDao.observeBalance(accountSpec.id)
      is Group -> transactionDao.observeBalance(accountSpec.group)
    }

  fun setDensity(density: TransactionsDensity) {
    prefs.update { meta -> meta.set(TransactionDensity, density) }
  }

  fun setAlternateRowColours(enabled: Boolean) {
    viewModelScope.launch { transactionPreferences.alternateRowColours.set(enabled) }
  }

  fun setExpandSplits(enabled: Boolean) {
    viewModelScope.launch { transactionPreferences.expandSplits.set(enabled) }
  }

  fun setAccountOption(option: AccountOption, enabled: Boolean) {
    val account = accountId ?: return
    val key =
      when (option) {
        RunningBalance -> ShowBalances(account)
        HideReconciledRows -> HideReconciled(account)
        ExtraBalances -> ShowExtraBalances(account)
      }
    viewModelScope.launch {
      try {
        viewPrefs.set(key, enabled)
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        logcat.w(e) { "Failed setting $key" }
      }
    }
  }

  fun toggleSplit(id: TransactionId) {
    mutableToggledSplits.update { ids -> if (id in ids) ids.removing(id) else ids.adding(id) }
  }

  fun bankSync() {
    val account = accountId ?: return
    if (!bankSyncController.start(setOf(account))) logcat.d { "Bank sync already running" }
  }

  fun deleteAccount() {
    val account = accountId ?: return
    if (deleteJob?.isActive == true) return
    deleteJob = viewModelScope.launch {
      try {
        val loginRemoved = deleter.delete(account)
        logcat.i { "Deleted account $account" }
        mutableEvents.trySend(if (loginRemoved) AccountDeleted else AccountDeletedBankLoginKept)
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        logcat.e(e) { "Failed deleting account $account" }
        mutableEvents.trySend(DeleteAccountFailed)
      }
    }
  }

  private fun buildPagingSource() =
    TransactionsPagingSource(
        transactionDao = transactionDao,
        tagsDao = tagsDao,
        spec = spec,
        hideReconciled = accountOptions.value?.hideReconciled == true,
      )
      .also { currentPagingSource = it }

  // As the bank sync screen decides it
  private fun Accounts.isLinked() = account_sync_source != null && account_id != null
}
