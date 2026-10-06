package aktual.budget.banksync.vm.providers

import aktual.budget.banksync.domain.BankSyncProviderSetup
import aktual.budget.banksync.domain.ProviderCredential
import aktual.budget.model.AccountSyncSource
import aktual.core.model.BudgetServer
import aktual.di.BudgetScope
import alakazam.kotlin.requireMessage
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logcat.logcat

/**
 * Saves the credentials the server uses to reach [source]. They apply to every budget on the
 * server, so the server refuses anyone but an admin.
 *
 * See packages/desktop-client/src/components/modals/GoCardlessInitialiseModal.tsx and the other
 * providers' initialise modals
 */
@Stable
@AssistedInject
class BankSyncProviderSetupViewModel(
  @Assisted private val source: AccountSyncSource,
  private val setup: BankSyncProviderSetup,
  server: BudgetServer,
) : ViewModel() {
  @AssistedFactory
  @ManualViewModelAssistedFactoryKey
  @ContributesIntoMap(BudgetScope::class)
  fun interface Factory : ManualViewModelAssistedFactory {
    fun create(@Assisted source: AccountSyncSource): BankSyncProviderSetupViewModel
  }

  private val mutableState =
    MutableStateFlow(
      BankSyncProviderSetupState(
        source = source,
        fields = ProviderCredential.of(source).map(::SetupField).toImmutableList(),
        redirectUrl = redirectUrl(server),
      )
    )
  val state: StateFlow<BankSyncProviderSetupState> = mutableState.asStateFlow()

  private val mutableEvents =
    MutableSharedFlow<BankSyncProviderSetupEvent>(
      extraBufferCapacity = 1,
      onBufferOverflow = DROP_OLDEST,
    )
  val events: SharedFlow<BankSyncProviderSetupEvent> = mutableEvents.asSharedFlow()

  fun setValue(credential: ProviderCredential, value: String) {
    mutableState.update { state ->
      val fields =
        state.fields.map { if (it.credential == credential) it.copy(value = value) else it }
      state.copy(fields = fields.toImmutableList(), error = null)
    }
  }

  fun save() {
    val current = mutableState.value
    if (!current.canSave) return
    mutableState.update { it.copy(isSaving = true, error = null) }
    viewModelScope.launch {
      val error =
        try {
          val values = current.fields.associate { it.credential to it.value }
          when (val response = setup.save(source, values)) {
            Success -> {
              null
            }
            is Failed -> {
              logcat.w { "Saving $source credentials refused: $response" }
              response.toSetupError()
            }
          }
        } catch (e: CancellationException) {
          throw e
        } catch (e: Exception) {
          logcat.e(e) { "Failed saving $source credentials" }
          SetupError.Other(e.requireMessage())
        }
      mutableState.update { it.copy(isSaving = false, error = error) }
      if (error == null) mutableEvents.emit(Saved)
    }
  }

  private fun redirectUrl(server: BudgetServer): String? =
    if (source == EnableBanking && server is Remote) {
      "${server.url.toString().trimEnd('/')}/enablebanking/auth_callback"
    } else {
      null
    }
}
