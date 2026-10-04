package aktual.budget.banksync.vm.providers

import aktual.api.client.BankSyncApi
import aktual.budget.banksync.domain.BankSyncProviderSetup
import aktual.budget.banksync.vm.BankSyncProviderStatus
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
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logcat.logcat

/**
 * Whether each bank sync provider has credentials on the server, to set them up or clear them.
 *
 * See packages/desktop-client/src/components/banksync/index.tsx
 */
@Stable
@ViewModelKey
@ContributesIntoMap(BudgetScope::class)
class BankSyncProvidersViewModel(
  private val api: BankSyncApi,
  private val setup: BankSyncProviderSetup,
  private val server: BudgetServer,
) : ViewModel() {
  private val mutableStatuses =
    MutableStateFlow(PROVIDERS.associateWith { BankSyncProviderStatus.Checking })
  private val mutableResetting = MutableStateFlow<AccountSyncSource?>(null)
  private var refreshJob: Job? = null

  private val mutableEvents =
    MutableSharedFlow<BankSyncProvidersEvent>(
      extraBufferCapacity = 1,
      onBufferOverflow = DROP_OLDEST,
    )
  val events: SharedFlow<BankSyncProvidersEvent> = mutableEvents.asSharedFlow()

  val state: StateFlow<BankSyncProvidersState> =
    viewModelScope.launchMolecule(Immediate) {
      val statuses by mutableStatuses.collectAsState()
      val resetting by mutableResetting.collectAsState()
      if (server is Remote) {
        BankSyncProvidersState.Loaded(
          providers =
            PROVIDERS.map { BankSyncProviderItem(it, statuses.getValue(it)) }.toImmutableList(),
          resetting = resetting,
        )
      } else {
        BankSyncProvidersState.NoServer
      }
    }

  init {
    refresh()
  }

  fun refresh() {
    if (server !is Remote || refreshJob?.isActive == true) return
    refreshJob = viewModelScope.launch {
      for (source in PROVIDERS) {
        launch {
          val status = fetchStatus(source)
          mutableStatuses.update { it + (source to status) }
        }
      }
    }
  }

  fun reset(source: AccountSyncSource) {
    if (!mutableResetting.compareAndSet(expect = null, update = source)) return
    viewModelScope.launch {
      val event =
        try {
          when (val response = setup.reset(source)) {
            Success -> {
              mutableStatuses.update { it + (source to BankSyncProviderStatus.NotConfigured) }
              BankSyncProvidersEvent.Reset
            }
            is Failed -> {
              logcat.w { "Resetting $source refused: $response" }
              BankSyncProvidersEvent.ResetFailed(response.toSetupError())
            }
          }
        } catch (e: CancellationException) {
          throw e
        } catch (e: Exception) {
          logcat.e(e) { "Failed resetting $source" }
          BankSyncProvidersEvent.ResetFailed(SetupError.Other(e.requireMessage()))
        } finally {
          mutableResetting.update { null }
        }
      mutableEvents.emit(event)
    }
  }

  private suspend fun fetchStatus(source: AccountSyncSource): BankSyncProviderStatus =
    try {
      when (val response = api.status(source)) {
        is Success -> {
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

  private companion object {
    // As upstream lists them
    val PROVIDERS: List<AccountSyncSource> =
      listOf(GoCardless, EnableBanking, SimpleFin, PluggyAi, Akahu)
  }
}
