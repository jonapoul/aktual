package aktual.budget.banksync.domain

import aktual.api.client.BankSyncApi
import aktual.api.model.banksync.BankSyncTransactionsResponse.ProviderError
import aktual.api.model.banksync.GoCardlessAccountsResponse
import aktual.di.BudgetScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull

@Inject
@ContributesBinding(BudgetScope::class)
class GoCardlessLoginWaiterImpl(private val api: BankSyncApi) : GoCardlessLoginWaiter {
  override suspend fun await(requisitionId: String): GoCardlessAccountsResponse =
    withTimeoutOrNull(TIMEOUT) { poll(requisitionId) }
      ?: GoCardlessAccountsResponse.Failed(
        ProviderError(ProviderError.TIMED_OUT, ProviderError.TIMED_OUT),
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
