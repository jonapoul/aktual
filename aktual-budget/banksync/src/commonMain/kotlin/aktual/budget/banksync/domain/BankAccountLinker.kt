package aktual.budget.banksync.domain

import aktual.api.model.banksync.ExternalBankAccount
import aktual.budget.model.AccountId
import aktual.budget.model.AccountSyncSource

/**
 * Links budget accounts to accounts at a bank, or unlinks them. See linkSimpleFinAccount() and
 * unlinkAccount() in packages/loot-core/src/server/accounts/app.ts.
 */
interface BankAccountLinker : BankAccountUnlinker {
  /**
   * Links [account] to [external], which [source] listed, then starts its first sync in the
   * budget's scope, after any sync already running.
   */
  suspend fun link(account: AccountId, source: AccountSyncSource, external: ExternalBankAccount)

  /**
   * Adds a budget account named after [external], which [source] listed, at the end of the on or
   * [offBudget] list with its transfer payee, then syncs it as [link] does. Its first sync adds the
   * starting balance. See linkGoCardlessAccount() without an upgradingId.
   */
  suspend fun create(
    source: AccountSyncSource,
    external: ExternalBankAccount,
    offBudget: Boolean,
  ): AccountId
}
