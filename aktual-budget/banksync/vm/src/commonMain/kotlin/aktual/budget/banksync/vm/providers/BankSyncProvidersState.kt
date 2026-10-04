package aktual.budget.banksync.vm.providers

import aktual.budget.banksync.vm.BankSyncProviderStatus
import aktual.budget.model.AccountSyncSource
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface BankSyncProvidersState {
  data object NoServer : BankSyncProvidersState

  @Immutable
  data class Loaded(
    val providers: ImmutableList<BankSyncProviderItem>,
    val resetting: AccountSyncSource? = null,
  ) : BankSyncProvidersState
}

@Immutable
data class BankSyncProviderItem(val source: AccountSyncSource, val status: BankSyncProviderStatus)

sealed interface BankSyncProvidersEvent {
  data object Reset : BankSyncProvidersEvent

  @JvmInline value class ResetFailed(val error: SetupError) : BankSyncProvidersEvent
}

@Immutable
sealed interface SetupError {
  data object NotAdmin : SetupError

  data object LoggedOut : SetupError

  @JvmInline value class Other(val cause: String?) : SetupError
}
