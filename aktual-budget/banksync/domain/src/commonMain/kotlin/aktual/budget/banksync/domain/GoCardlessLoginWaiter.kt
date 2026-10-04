package aktual.budget.banksync.domain

import aktual.api.client.BankSyncApi
import aktual.api.model.banksync.BankSyncTransactionsResponse.ProviderError
import aktual.api.model.banksync.GoCardlessAccountsResponse
import dev.zacsweers.metro.Inject
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Waits for the user to finish a GoCardless login in their browser. See pollGoCardlessWebToken() in
 * packages/loot-core/src/server/accounts/app.ts.
 */
@Inject
class GoCardlessLoginWaiter(private val api: BankSyncApi) {
  /**
   * Polls every few seconds until the login started for [requisitionId] lists its accounts or
   * fails, giving up with a [ProviderError.TIMED_OUT] after ten minutes, as upstream.
   */
  suspend fun await(requisitionId: String): GoCardlessAccountsResponse =
    withTimeoutOrNull(TIMEOUT) { poll(requisitionId) }
      ?: GoCardlessAccountsResponse.Failed(
        ProviderError(ProviderError.TIMED_OUT, ProviderError.TIMED_OUT)
      )

  private suspend fun poll(requisitionId: String): GoCardlessAccountsResponse {
    var response = api.goCardlessAccounts(requisitionId)
    while (response == Pending) {
      delay(INTERVAL)
      response = api.goCardlessAccounts(requisitionId)
    }
    return response
  }

  private companion object {
    val INTERVAL = 3.seconds
    val TIMEOUT = 10.minutes
  }
}
