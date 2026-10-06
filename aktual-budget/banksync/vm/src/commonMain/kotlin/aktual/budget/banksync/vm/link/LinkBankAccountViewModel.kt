package aktual.budget.banksync.vm.link

import aktual.api.client.BankSyncApi
import aktual.api.client.EnableBankingApi
import aktual.api.model.banksync.BankSyncStatusResponse
import aktual.api.model.banksync.BankSyncTransactionsResponse.Failure
import aktual.api.model.banksync.BankSyncTransactionsResponse.ProviderError
import aktual.api.model.banksync.BankSyncTransactionsResponse.Rejected
import aktual.api.model.banksync.ExternalBankAccount
import aktual.budget.banksync.domain.BankAccountLinker
import aktual.budget.banksync.domain.GoCardlessLoginWaiter
import aktual.budget.db.dao.AccountDao
import aktual.budget.model.AccountId
import aktual.budget.model.AccountSyncSource
import aktual.core.model.BudgetServer
import aktual.core.model.BuildConfig
import aktual.di.BudgetScope
import alakazam.kotlin.requireMessage
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.launchMolecule
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logcat.logcat

/**
 * Links an unlinked account, or a new one if there's no [account], to one that a configured
 * provider lists, then syncs it. SimpleFIN, Pluggy.ai and Akahu list accounts up front, while
 * GoCardless and Enable Banking need the user to log in to their bank first.
 *
 * See packages/desktop-client/src/components/modals/SelectLinkedAccountsModal.tsx
 */
@Stable
@AssistedInject
class LinkBankAccountViewModel(
  @Assisted private val account: AccountId?,
  private val accountDao: AccountDao,
  private val api: BankSyncApi,
  private val linker: BankAccountLinker,
  private val server: BudgetServer,
  waiter: GoCardlessLoginWaiter,
  enableBanking: EnableBankingApi,
  buildConfig: BuildConfig,
) : ViewModel() {
  @AssistedFactory
  @ManualViewModelAssistedFactoryKey
  @ContributesIntoMap(BudgetScope::class)
  fun interface Factory : ManualViewModelAssistedFactory {
    fun create(@Assisted account: AccountId?): LinkBankAccountViewModel
  }

  private val mutableFailure = MutableStateFlow<LinkBankAccountState.Failure?>(null)
  private val mutableTarget = MutableStateFlow<LinkTarget>(LinkTarget.New())
  private val mutableProviders = MutableStateFlow<ImmutableList<AccountSyncSource>?>(null)
  private val mutableSelected = MutableStateFlow<AccountSyncSource?>(null)
  private val mutableAccounts = MutableStateFlow<Map<AccountSyncSource, Listed>>(emptyMap())
  private val mutableIsLinking = MutableStateFlow(false)

  private val mutableEvents =
    MutableSharedFlow<LinkBankAccountEvent>(
      extraBufferCapacity = 1,
      onBufferOverflow = DROP_OLDEST,
    )
  val events: SharedFlow<LinkBankAccountEvent> = mutableEvents.asSharedFlow()

  private val logins: Map<AccountSyncSource, BankLoginModel> =
    mapOf(
        AccountSyncSource.GoCardless to
          GoCardlessLoginProvider(api, waiter, showDemo = buildConfig.isDebug),
        AccountSyncSource.EnableBanking to EnableBankingLoginProvider(enableBanking),
      )
      .mapValues { (source, provider) -> loginModel(source, provider) }

  val state: StateFlow<LinkBankAccountState> =
    viewModelScope.launchMolecule(Immediate) {
      val failure by mutableFailure.collectAsState()
      val target by mutableTarget.collectAsState()
      val providers by mutableProviders.collectAsState()
      val selected by mutableSelected.collectAsState()
      val accounts by mutableAccounts.collectAsState()
      val isLinking by mutableIsLinking.collectAsState()
      val login = selected?.let(logins::get)?.state?.collectAsState()?.value
      failure?.let {
        return@launchMolecule it
      }
      val loaded = providers
      val source = selected
      val hasServer = server is Remote
      when {
        loaded == null -> LinkBankAccountState.Loading
        loaded.isEmpty() || source == null -> LinkBankAccountState.NoProviders(hasServer)
        else ->
          LinkBankAccountState.Choosing(
            target = target,
            providers = loaded,
            selected = source,
            accounts =
              accounts[source]?.state
                ?: login?.let(ExternalAccounts::NeedsLogin)
                ?: ExternalAccounts.Loading,
            isLinking = isLinking,
          )
      }
    }

  init {
    viewModelScope.launch {
      if (account != null && !loadAccount(account)) return@launch
      loadProviders()
    }
  }

  fun refreshProviders() {
    if (mutableProviders.value?.isEmpty() != true || server !is Remote) return
    mutableProviders.update { null }
    viewModelScope.launch { loadProviders() }
  }

  fun select(source: AccountSyncSource) {
    mutableSelected.update { source }
    val state = mutableAccounts.value[source]?.state
    val login = logins[source]
    when {
      login != null -> if (state == null) login.start()
      state == null || state is ExternalAccounts.Failure -> load(source)
    }
  }

  fun reload() {
    val source = mutableSelected.value ?: return
    val login = logins[source]
    when {
      login != null -> login.start()
      mutableAccounts.value[source]?.state != Loading -> load(source)
    }
  }

  fun selectCountry(country: String) {
    selectedLogin()?.selectCountry(country)
  }

  fun selectAccountType(type: LoginAccountType) {
    selectedLogin()?.selectAccountType(type)
  }

  fun logIn(bankId: String) {
    selectedLogin()?.logIn(bankId)
  }

  fun reopenLogin() {
    selectedLogin()?.reopen()
  }

  fun cancelLogin() {
    selectedLogin()?.cancel()
  }

  fun setOffBudget(offBudget: Boolean) {
    mutableTarget.update { target ->
      if (target is New) LinkTarget.New(offBudget) else target
    }
  }

  fun link(accountId: String) {
    val source = mutableSelected.value ?: return
    val external =
      mutableAccounts.value[source]?.accounts?.firstOrNull { it.accountId == accountId }
    if (external == null || !mutableIsLinking.compareAndSet(expect = false, update = true)) return
    val target = mutableTarget.value
    viewModelScope.launch {
      try {
        when {
          account != null -> linker.link(account, source, external)
          target is New -> linker.create(source, external, target.offBudget)
          else -> error("Nothing to link ${external.accountId} to")
        }
        mutableEvents.emit(Linked)
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        logcat.e(e) { "Failed linking ${account ?: "a new account"} to $accountId" }
        mutableEvents.emit(LinkBankAccountEvent.LinkFailed(e.requireMessage()))
      } finally {
        mutableIsLinking.update { false }
      }
    }
  }

  private suspend fun loadProviders() {
    val providers = configuredProviders()
    mutableProviders.update { providers }
    providers.firstOrNull()?.let(::select)
  }

  private suspend fun loadAccount(id: AccountId): Boolean =
    try {
      val row = accountDao[id]
      if (row == null) {
        mutableFailure.update { LinkBankAccountState.Failure(cause = null) }
      } else {
        mutableTarget.update { LinkTarget.Existing(row.name) }
      }
      row != null
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      logcat.e(e) { "Failed loading $id" }
      mutableFailure.update { LinkBankAccountState.Failure(e.requireMessage()) }
      false
    }

  private fun selectedLogin(): BankLoginModel? = mutableSelected.value?.let(logins::get)

  private fun loginModel(source: AccountSyncSource, provider: BankLoginProvider) =
    BankLoginModel(
      provider = provider,
      scope = viewModelScope,
      openBrowser = { mutableEvents.emit(LinkBankAccountEvent.OpenBrowser(it)) },
      onAccounts = { accounts ->
        val listed = Listed(ExternalAccounts.Loaded(items(accounts)), accounts)
        mutableAccounts.update { it + (source to listed) }
      },
    )

  private fun load(source: AccountSyncSource) {
    mutableAccounts.update { it + (source to Listed(Loading)) }
    viewModelScope.launch {
      val listed =
        try {
          when (val response = api.accounts(source)) {
            is Success -> {
              val items = items(response.accounts)
              Listed(ExternalAccounts.Loaded(items), response.accounts)
            }
            is Failed -> {
              logcat.w { "Listing $source accounts failed: ${response.error}" }
              Listed(ExternalAccounts.Failure(response.error.cause()))
            }
          }
        } catch (e: CancellationException) {
          throw e
        } catch (e: Exception) {
          logcat.e(e) { "Failed listing $source accounts" }
          Listed(ExternalAccounts.Failure(e.requireMessage()))
        }
      mutableAccounts.update { it + (source to listed) }
    }
  }

  // Which budget account each external one is already linked to, as the modal shows
  private suspend fun items(
    accounts: List<ExternalBankAccount>
  ): ImmutableList<ExternalAccountItem> {
    val linked =
      accountDao
        .getBankSyncAccounts()
        .filter { it.id != account && it.account_id != null }
        .associate { it.account_id to it.name }
    return accounts
      .map { a ->
        ExternalAccountItem(
          accountId = a.accountId,
          name = a.name,
          institution = a.institution,
          balance = a.balance,
          linkedTo = linked[a.accountId],
        )
      }
      .toImmutableList()
  }

  private suspend fun configuredProviders(): ImmutableList<AccountSyncSource> {
    if (server !is Remote) return persistentListOf()
    val configured = coroutineScope {
      PROVIDERS.map { source -> async { source.takeIf { isConfigured(it) } } }.awaitAll()
    }
    return configured.filterNotNull().toImmutableList()
  }

  private suspend fun isConfigured(source: AccountSyncSource): Boolean =
    try {
      when (val response = api.status(source)) {
        is Success -> response.configured
        is BankSyncStatusResponse.Rejected -> false
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      logcat.w(e) { "Failed checking bank sync status for $source" }
      false
    }

  private data class Listed(
    val state: ExternalAccounts,
    val accounts: List<ExternalBankAccount> = emptyList(),
  )

  private companion object {
    val PROVIDERS: List<AccountSyncSource> =
      listOf(GoCardless, EnableBanking, SimpleFin, PluggyAi, Akahu)
  }
}

internal fun Failure.cause(): String? =
  when (this) {
    is ProviderError -> reason ?: errorCode
    is Rejected -> reason ?: details
  }
