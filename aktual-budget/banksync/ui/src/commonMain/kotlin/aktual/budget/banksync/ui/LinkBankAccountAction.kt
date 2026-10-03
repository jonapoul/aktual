package aktual.budget.banksync.ui

import aktual.budget.model.AccountSyncSource
import androidx.compose.runtime.Immutable

@Immutable internal sealed interface LinkBankAccountAction

internal data object CloseLink : LinkBankAccountAction

internal data object RetryListing : LinkBankAccountAction

@JvmInline
internal value class SelectProvider(val source: AccountSyncSource) : LinkBankAccountAction

@JvmInline internal value class LinkTo(val accountId: String) : LinkBankAccountAction

@Immutable
internal fun interface LinkBankAccountActionHandler {
  operator fun invoke(action: LinkBankAccountAction)
}
