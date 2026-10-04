package aktual.budget.banksync.ui

import aktual.budget.model.AccountId
import androidx.compose.runtime.Immutable

@Immutable internal sealed interface BankSyncAction

internal data object Reload : BankSyncAction

internal data object SyncAll : BankSyncAction

@JvmInline internal value class SyncAccount(val id: AccountId) : BankSyncAction

@Immutable
internal fun interface BankSyncActionHandler {
  operator fun invoke(action: BankSyncAction)
}
