package aktual.budget.banksync.domain

import aktual.api.model.banksync.BankSyncTransactionsResponse.ProviderError
import aktual.api.model.banksync.GoCardlessAccountsResponse

/**
 * Waits for the user to finish a GoCardless login in their browser. See pollGoCardlessWebToken() in
 * packages/loot-core/src/server/accounts/app.ts.
 */
interface GoCardlessLoginWaiter {
  /**
   * Polls every few seconds until the login started for [requisitionId] lists its accounts or
   * fails, giving up with a [ProviderError.TIMED_OUT] after ten minutes, as upstream.
   */
  suspend fun await(requisitionId: String): GoCardlessAccountsResponse
}
