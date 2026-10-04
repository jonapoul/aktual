package aktual.budget.banksync.ui

import aktual.budget.model.AccountSyncSource
import androidx.compose.runtime.Immutable

@Immutable internal sealed interface BankSyncProvidersAction

internal data object CloseProviders : BankSyncProvidersAction

@JvmInline
internal value class SetUpProvider(val source: AccountSyncSource) : BankSyncProvidersAction

@JvmInline
internal value class ResetProvider(val source: AccountSyncSource) : BankSyncProvidersAction

@Immutable
internal fun interface BankSyncProvidersActionHandler {
  operator fun invoke(action: BankSyncProvidersAction)
}
