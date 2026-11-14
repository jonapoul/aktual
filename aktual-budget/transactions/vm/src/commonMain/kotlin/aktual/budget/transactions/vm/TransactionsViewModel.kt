package aktual.budget.transactions.vm

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
import aktual.budget.model.Condition
import aktual.budget.model.ConditionOp
import aktual.budget.model.DbMetadataKey.Companion.TransactionDensity
import aktual.budget.model.SyncedPrefKey.PerAccount.HideReconciled
import aktual.budget.model.SyncedPrefKey.PerAccount.ShowBalances
import aktual.budget.model.SyncedPrefKey.PerAccount.ShowExtraBalances
import aktual.budget.model.TagSpec
import aktual.budget.model.TransactionId
import aktual.budget.model.TransactionSearch
import aktual.budget.model.TransactionsDensity
import aktual.budget.model.TransactionsSpec
import aktual.budget.tags.domain.TagLabel
import aktual.budget.tags.domain.toTagLabels
import aktual.budget.transactions.domain.AccountViewPrefs
import aktual.budget.transactions.domain.Reconciler
import aktual.budget.transactions.domain.TransactionFilters
import aktual.budget.transactions.vm.LoadedAccount.AllAccounts
import aktual.budget.transactions.vm.LoadedAccount.Loading
import aktual.budget.transactions.vm.LoadedAccount.SpecificAccount
import aktual.budget.transactions.vm.LoadedAccount.SpecificCategory
import aktual.budget.transactions.vm.LoadedAccount.SpecificTag
import aktual.budget.transactions.vm.LoadedAccount.Uncategorised
import aktual.budget.utils.BudgetLocalPreferences
import aktual.core.model.BudgetServer
import aktual.core.utils.runCatchingCancellable
import aktual.di.BudgetScope
import aktual.di.ViewModelContext
import aktual.di.newScope
import aktual.log
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
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Stable
@AssistedInject
@Suppress("LongParameterList")
class TransactionsViewModel
internal constructor(
  @Assisted private val spec: TransactionsSpec,
  private val prefs: BudgetLocalPreferences,
  private val accountDao: AccountDao,
  private val transactionDao: TransactionDao,
  private val tagsDao: TagsDao,
  private val categoryDao: CategoryDao,
  private val bankSyncController: BankSyncController,
  private val deleter: AccountDeleter,
  private val reconciler: Reconciler,
  private val viewPrefs: AccountViewPrefs,
  private val transactionPreferences: TransactionPreferences,
  private val filters: TransactionFilters,
  newTransactions: NewTransactions,
  server: BudgetServer,
  private val clock: Clock,
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
  private var currentPagingSource: PagingSource<PageKey, Transaction>? = null
  // Buffered, so an event sent while the screen is being recreated waits for it
  private val mutableEvents = Channel<TransactionsEvent>(Channel.BUFFERED)
  private var deleteJob: Job? = null
  // The bank balance being reconciled against, while reconciling
  private val mutableReconcileTarget = MutableStateFlow<Amount?>(null)
  private val mutableReconcileBusy = MutableStateFlow(false)
  private var reconcileJob: Job? = null

  val loadedAccount: StateFlow<LoadedAccount> = mutableLoadedAccount.asStateFlow()

  // Names a rename can't take, closed accounts included. See upstream's validateAccountName()

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
        log.w(e) { "Failed loading tags" }
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

  // Open accounts to switch to. Empty unless the list is a single account's
  val accountChoices: StateFlow<ImmutableList<AccountChoice>> =
    if (accountId == null) {
      MutableStateFlow(persistentListOf())
    } else {
      accountDao
        .observeAllActive()
        .map { rows -> rows.map { AccountChoice(it.id, it.name) }.toImmutableList() }
        .catch { e ->
          log.w(e) { "Failed loading accounts" }
          emit(persistentListOf())
        }
        .stateIn(viewModelScope, Eagerly, initialValue = persistentListOf())
    }

  // An account's list shows a running balance if its option says so. Other lists that have a
  // balance always show it
  val showRunningBalance: StateFlow<Boolean> =
    accountOptions
      .map { options -> options?.showRunningBalance ?: (accountId == null) }
      .stateIn(viewModelScope, Eagerly, initialValue = accountId == null)

  private val accountClearedBalances: StateFlow<ClearedBalances?> =
    if (accountId == null) {
      MutableStateFlow(null)
    } else {
      transactionDao
        .observeClearedBalances(accountId)
        .catch { e -> log.w(e) { "Failed loading cleared balances of $accountId" } }
        .stateIn(viewModelScope, Eagerly, initialValue = null)
    }

  // Shown while reconciling too, for the cleared balance being matched
  val clearedBalances: StateFlow<ClearedBalances?> =
    combine(accountClearedBalances, accountOptions, mutableReconcileTarget, ::balances)
      .stateIn(viewModelScope, Eagerly, initialValue = null)

  val reconciling: StateFlow<Reconciling?> =
    combine(mutableReconcileTarget, accountClearedBalances, mutableReconcileBusy, ::reconciling)
      .stateIn(viewModelScope, Eagerly, initialValue = null)

  // Null until the account and its cleared balance have loaded
  val reconcileStart: StateFlow<ReconcileStart?> =
    combine(mutableLoadedAccount, accountClearedBalances, ::reconcileStart)
      .stateIn(viewModelScope, Eagerly, initialValue = null)

  // Reconciled rows show while reconciling, as upstream's do
  private val hideReconciled: StateFlow<Boolean> =
    combine(accountOptions, mutableReconcileTarget, ::hideReconciled)
      .stateIn(viewModelScope, Eagerly, initialValue = false)

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

  // As typed, for the search field and filter chips. Shared by every account list
  val search: StateFlow<TransactionSearch> = filters.state

  // The search the list is narrowed by. Typing is debounced, but clearing the text and changing
  // the conditions apply at once. Null for an empty search, or a list that ignores it
  @OptIn(FlowPreview::class)
  private val activeSearch: StateFlow<TransactionSearch?> =
    combine(
        filters.state
          .map { it.text.trim() }
          .distinctUntilChanged()
          .debounce { text -> if (text.isEmpty()) Duration.ZERO else QUERY_DEBOUNCE },
        filters.state.map { it.conditions }.distinctUntilChanged(),
        ::TransactionSearch,
      )
      .map(spec::activeSearch)
      .distinctUntilChanged()
      .stateIn(viewModelScope, Eagerly, initialValue = spec.activeSearch(filters.state.value))

  // A filtered or searched list only holds part of each account, so it has no balance to show
  val showBalance: StateFlow<Boolean> =
    activeSearch
      .map(::hasBalance)
      .stateIn(viewModelScope, Eagerly, initialValue = hasBalance(activeSearch.value))

  val balance: StateFlow<Amount?> =
    if (spec.isAccountList) {
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

  // A new search starts a new pager, so the list starts from the top
  val pagingData: Flow<PagingData<Transaction>> =
    activeSearch
      .flatMapLatest { search ->
        Pager(
            config = PagingConfig(pageSize = PAGING_SIZE, enablePlaceholders = false),
            pagingSourceFactory = { buildPagingSource(search) },
          )
          .flow
      }
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
          // Watched, so a rename from the edit sheet shows in the title
          accountDao.observe(accountSpec.id).collect { account ->
            account ?: error("No account matching $accountSpec")
            mutableLoadedAccount.update { SpecificAccount(account) }
          }
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
      hideReconciled.drop(count = 1).collect { currentPagingSource?.invalidate() }
    }

    // Invalidate PagingSource when transaction data changes.
    // Ignore the first item from the flow, that'll be the initial table state.
    viewModelScope.launch {
      transactionDao.observeChanges().drop(count = 1).collect {
        log.d { "Transactions table updated, invalidating paging source..." }
        currentPagingSource?.invalidate()
      }
    }

    // The account's new transactions are seen while it's open, even ones a sync adds meanwhile
    if (accountId != null) {
      viewModelScope.launch {
        newTransactions.counts
          .filter { accountId in it }
          .collect {
            runCatchingCancellable(
              attempt = { newTransactions.markSeen(accountId) },
              onFailure = { e -> log.w(e) { "Failed marking $accountId seen" } },
            )
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
      runCatchingCancellable(
        attempt = { viewPrefs.set(key, enabled) },
        onFailure = { e -> log.w(e) { "Failed setting $key" } },
      )
    }
  }

  fun setQuery(text: String) = filters.setQuery(text)

  fun addCondition(condition: Condition) = filters.addCondition(condition)

  fun editCondition(index: Int, condition: Condition) = filters.editCondition(index, condition)

  fun removeCondition(index: Int) = filters.removeCondition(index)

  fun setConditionsOp(op: ConditionOp) = filters.setConditionsOp(op)

  fun clearFilters() = filters.clear()

  fun toggleSplit(id: TransactionId) {
    mutableToggledSplits.update { ids -> if (id in ids) ids.removing(id) else ids.adding(id) }
  }

  fun bankSync() {
    val account = accountId ?: return
    if (!bankSyncController.start(setOf(account))) log.d { "Bank sync already running" }
  }

  fun deleteAccount() {
    val account = accountId ?: return
    if (deleteJob?.isActive == true) return
    deleteJob = viewModelScope.launch {
      runCatchingCancellable(
        attempt = {
          val loginRemoved = deleter.delete(account)
          log.i { "Deleted account $account" }
          mutableEvents.trySend(if (loginRemoved) AccountDeleted else AccountDeletedBankLoginKept)
        },
        onFailure = { e ->
          log.e(e) { "Failed deleting account $account" }
          mutableEvents.trySend(DeleteAccountFailed)
        },
      )
    }
  }

  fun startReconciling(target: Amount) {
    if (accountId == null) return
    mutableReconcileTarget.update { target }
  }

  fun setCleared(id: TransactionId, cleared: Boolean) =
    reconcile("Failed setting $id cleared") { reconciler.setCleared(id, cleared) }

  fun unlockTransaction(id: TransactionId) =
    reconcile("Failed unlocking $id") { reconciler.unlock(id) }

  // notes is the adjustment's, in the user's language as upstream writes it
  fun createReconcileAdjustment(notes: String) {
    val account = accountId ?: return
    val difference = reconciling.value?.difference ?: return
    if (difference == Zero) return
    reconcile("Failed adding adjustment to $account") {
      reconciler.createAdjustment(account, difference, notes)
    }
  }

  fun finishReconciling() {
    val account = accountId ?: return
    val target = mutableReconcileTarget.value ?: return
    reconcile("Failed finishing reconciling $account") {
      reconciler.finish(account, target)
      mutableReconcileTarget.update { null }
      val now = clock.now()
      mutableLoadedAccount.update { loaded ->
        if (loaded is SpecificAccount) {
          SpecificAccount(loaded.account.copy(last_reconciled = now))
        } else {
          loaded
        }
      }
    }
  }

  // One write at a time, so a double tap can't add two adjustments
  private fun reconcile(failure: String, block: suspend () -> Unit) {
    if (reconcileJob?.isActive == true) return
    reconcileJob = viewModelScope.launch {
      mutableReconcileBusy.update { true }
      runCatchingCancellable(
        attempt = { block() },
        onFailure = { e ->
          log.e(e) { failure }
          mutableEvents.trySend(ReconcileFailed)
        },
        finally = { mutableReconcileBusy.update { false } },
      )
    }
  }

  private fun hasBalance(search: TransactionSearch?) = spec.isAccountList && search == null

  private fun buildPagingSource(search: TransactionSearch?) =
    TransactionsPagingSource(
        transactionDao = transactionDao,
        tagsDao = tagsDao,
        spec = spec,
        hideReconciled = hideReconciled.value,
        search = search,
      )
      .also { currentPagingSource = it }

  // As the bank sync screen decides it
  private fun Accounts.isLinked() = account_sync_source != null && account_id != null
}

private val QUERY_DEBOUNCE = 300.milliseconds
