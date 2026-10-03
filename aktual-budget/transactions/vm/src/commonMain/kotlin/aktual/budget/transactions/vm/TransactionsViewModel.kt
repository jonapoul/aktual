package aktual.budget.transactions.vm

import aktual.budget.BudgetLocalPreferences
import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.PreferencesDao
import aktual.budget.db.dao.TagsDao
import aktual.budget.db.dao.TransactionDao
import aktual.budget.model.AccountSpec
import aktual.budget.model.Amount
import aktual.budget.model.DbMetadata
import aktual.budget.model.SyncedPrefKey
import aktual.budget.model.TagSpec
import aktual.budget.model.TransactionsDensity
import aktual.budget.model.TransactionsSpec
import aktual.budget.transactions.vm.LoadedAccount.AllAccounts
import aktual.budget.transactions.vm.LoadedAccount.Loading
import aktual.budget.transactions.vm.LoadedAccount.SpecificAccount
import aktual.budget.transactions.vm.LoadedAccount.SpecificTag
import aktual.di.BudgetScope
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
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
) : ViewModel() {
  @AssistedFactory
  @ManualViewModelAssistedFactoryKey
  @ContributesIntoMap(BudgetScope::class)
  fun interface Factory : ManualViewModelAssistedFactory {
    fun create(@Assisted spec: TransactionsSpec): TransactionsViewModel
  }

  private val mutableLoadedAccount = MutableStateFlow<LoadedAccount>(Loading)
  private var currentPagingSource: PagingSource<Int, Transaction>? = null

  val loadedAccount: StateFlow<LoadedAccount> = mutableLoadedAccount.asStateFlow()

  val density: StateFlow<TransactionsDensity> =
    prefs
      .map { meta -> meta[TransactionDensityKey] ?: Default }
      .stateIn(viewModelScope, Eagerly, initialValue = Default)

  // Dummy value until #1675 computes the real one
  val balance: StateFlow<Amount?> = MutableStateFlow(DummyBalance).asStateFlow()

  val pagingData: Flow<PagingData<Transaction>> =
    Pager(
        config = PagingConfig(pageSize = PAGING_SIZE, enablePlaceholders = false),
        pagingSourceFactory = ::buildPagingSource,
      )
      .flow
      .cachedIn(viewModelScope)

  init {
    // A tag-filtered screen titles itself after the tag; otherwise the title follows the account.
    when (val tagSpec = spec.tagSpec) {
      is TagSpec.SpecificTag ->
        viewModelScope.launch {
          val name = tagsDao.getTag(tagSpec.id)?.tag
          mutableLoadedAccount.update { if (name != null) SpecificTag(name) else AllAccounts }
        }

      is TagSpec.AllTags ->
        when (val s = spec.accountSpec) {
          is AccountSpec.AllAccounts -> mutableLoadedAccount.update { AllAccounts }

          is AccountSpec.SpecificAccount ->
            viewModelScope.launch {
              val account = accountDao[s.id] ?: error("No account matching $s")
              mutableLoadedAccount.update { SpecificAccount(account) }
            }
        }
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

  fun setPrivacyMode(privacyMode: Boolean) {
    viewModelScope.launch {
      preferencesDao[SyncedPrefKey.Global.IsPrivacyEnabled] = privacyMode.toString()
    }
  }

  private fun buildPagingSource() =
    TransactionsPagingSource(transactionDao, tagsDao, spec).also { currentPagingSource = it }

  private companion object {
    val TransactionDensityKey = DbMetadata.enumKey<TransactionsDensity>("transactionDensity")
  }
}
