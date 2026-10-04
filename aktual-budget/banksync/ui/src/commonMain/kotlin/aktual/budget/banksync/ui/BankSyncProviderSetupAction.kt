package aktual.budget.banksync.ui

import aktual.budget.banksync.domain.ProviderCredential
import androidx.compose.runtime.Immutable

@Immutable internal sealed interface BankSyncProviderSetupAction

internal data object CloseSetup : BankSyncProviderSetupAction

internal data object SaveSetup : BankSyncProviderSetupAction

internal data class SetCredential(val credential: ProviderCredential, val value: String) :
  BankSyncProviderSetupAction

@JvmInline internal value class OpenProviderSite(val url: String) : BankSyncProviderSetupAction

@Immutable
internal fun interface BankSyncProviderSetupActionHandler {
  operator fun invoke(action: BankSyncProviderSetupAction)
}
