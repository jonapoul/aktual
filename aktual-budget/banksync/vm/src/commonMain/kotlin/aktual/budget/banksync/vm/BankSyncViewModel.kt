package aktual.budget.banksync.vm

import aktual.api.client.BankSyncApi
import aktual.api.model.banksync.BankSyncStatusResponse
import aktual.budget.db.dao.AccountDao
import aktual.budget.model.AccountSyncSource
import aktual.core.model.BudgetServer
import aktual.di.BudgetScope
import alakazam.kotlin.requireMessage
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.launchMolecule
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlin.time.Clock
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logcat.logcat

/**
 * Read-only view of each open account's bank sync link, grouped by provider. Nothing here syncs or
 * links anything yet.
 *
 * See packages/desktop-client/src/components/mobile/banksync/MobileBankSyncPage.tsx
 */
@Stable
@ViewModelKey
@ContributesIntoMap(BudgetScope::class)
class BankSyncViewModel(
  private val accountDao: AccountDao,
  private val api: BankSyncApi,
  private val server: BudgetServer,
  private val clock: Clock,
) : ViewModel() {
  private val mutableAccounts = MutableStateFlow(LoadedAccounts.None)
  private val mutableStatuses =
    MutableStateFlow<PersistentMap<AccountSyncSource, BankSyncProviderStatus>>(persistentMapOf())
  private val mutableIsLoading = MutableStateFlow(true)
  private val mutableFailure = MutableStateFlow<String?>(null)
  private var loadJob: Job? = null

  val state: StateFlow<BankSyncState> =
    viewModelScope.launchMolecule(Immediate) {
      val accounts by mutableAccounts.collectAsState()
      val statuses by mutableStatuses.collectAsState()
      val isLoading by mutableIsLoading.collectAsState()
      val failure by mutableFailure.collectAsState()
      when {
        isLoading -> Loading
        failure != null -> Failure(failure)
        accounts.isEmpty() -> Empty
        else -> accounts.toSuccess(statuses)
      }
    }

  init {
    reload()
  }

  fun reload(showLoading: Boolean = true) {
    if (showLoading) mutableIsLoading.update { true }
    loadJob?.cancel()
    loadJob = viewModelScope.launch {
      val sources =
        try {
          val accounts = loadAccounts()
          mutableAccounts.update { accounts }
          mutableFailure.update { null }
          accounts.linked.keys
        } catch (e: CancellationException) {
          throw e
        } catch (e: Exception) {
          logcat.e(e) { "Failed loading bank sync accounts" }
          mutableFailure.update { e.requireMessage() }
          emptySet()
        } finally {
          mutableIsLoading.update { false }
        }

      // Show the accounts straight away, then fill in each provider's status as it arrives
      checkProviders(sources)
    }
  }

  private suspend fun loadAccounts(): LoadedAccounts {
    val now = clock.now()
    val (linked, unlinked) = accountDao.getBankSyncAccounts().partition { it.isLinked }
    val groups =
      linked
        .groupBy { requireNotNull(it.account_sync_source) }
        .mapValues { (_, rows) -> rows.map { it.toBankSyncAccount(now) }.toImmutableList() }
    return LoadedAccounts(
      linked = groups,
      unlinked = unlinked.map { it.toBankSyncAccount(now) }.toImmutableList(),
    )
  }

  private suspend fun checkProviders(sources: Set<AccountSyncSource>) {
    if (server !is BudgetServer.Remote) {
      mutableStatuses.update {
        sources.associateWith { BankSyncProviderStatus.NoServer }.toPersistentMap()
      }
      return
    }

    // keep any status we already know, so a silent reload doesn't flash back to "checking"
    mutableStatuses.update { current ->
      sources.associateWith { current[it] ?: Checking }.toPersistentMap()
    }

    coroutineScope {
      for (source in sources) {
        launch {
          val status = fetchStatus(source)
          mutableStatuses.update { it.putting(source, status) }
        }
      }
    }
  }

  private suspend fun fetchStatus(source: AccountSyncSource): BankSyncProviderStatus =
    try {
      when (val response = api.status(source)) {
        is BankSyncStatusResponse.Success -> {
          if (response.configured) Configured else NotConfigured
        }
        is Rejected -> {
          logcat.w { "Bank sync status for $source rejected: $response" }
          Failed
        }
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      logcat.w(e) { "Failed checking bank sync status for $source" }
      Failed
    }
}

private class LoadedAccounts(
  val linked: Map<AccountSyncSource, ImmutableList<BankSyncAccount>>,
  val unlinked: ImmutableList<BankSyncAccount>,
) {
  fun isEmpty() = linked.isEmpty() && unlinked.isEmpty()

  fun toSuccess(statuses: Map<AccountSyncSource, BankSyncProviderStatus>) =
    Success(
      providers =
        linked.entries
          // same order as upstream's groupBankSyncAccounts()
          .sortedBy { (source, _) -> source.value.lowercase() }
          .map { (source, accounts) ->
            BankSyncProvider(
              source = source,
              status = statuses[source] ?: Checking,
              accounts = accounts,
            )
          }
          .toImmutableList(),
      unlinked = unlinked,
    )

  companion object {
    val None = LoadedAccounts(linked = emptyMap(), unlinked = persistentListOf())
  }
}
