package aktual.budget.banksync.vm.providers

import aktual.budget.banksync.domain.ProviderCredential
import aktual.budget.model.AccountSyncSource
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class BankSyncProviderSetupState(
  val source: AccountSyncSource,
  val fields: ImmutableList<SetupField>,
  val redirectUrl: String? = null,
  val isSaving: Boolean = false,
  val error: SetupError? = null,
) {
  val canSave: Boolean
    get() = !isSaving && fields.all { it.value.isNotBlank() }
}

@Immutable data class SetupField(val credential: ProviderCredential, val value: String = "")

sealed interface BankSyncProviderSetupEvent {
  data object Saved : BankSyncProviderSetupEvent
}
