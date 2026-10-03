package aktual.budget.banksync.ui

import androidx.compose.runtime.Immutable

@Immutable internal sealed interface BankSyncAction

internal data object Reload : BankSyncAction

@Immutable
internal fun interface BankSyncActionHandler {
  operator fun invoke(action: BankSyncAction)
}
