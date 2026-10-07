package aktual.budget.banksync.domain

import aktual.api.model.banksync.BankSyncTransactionsResponse.ProviderError
import aktual.api.model.banksync.ExternalBankAccount
import aktual.api.model.banksync.GoCardlessAccountsResponse
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest

internal class GoCardlessLoginWaiterTest {
  private val api = FakeBankSyncApi()
  private val waiter = GoCardlessLoginWaiter(api)

  @Test
  fun `Polls until the login finishes`() = runTest {
    val success = GoCardlessAccountsResponse.Success(listOf(ACCOUNT))
    api.pollResponses += GoCardlessAccountsResponse.Pending
    api.pollResponses += GoCardlessAccountsResponse.Pending
    api.pollResponses += success

    val response = waiter.await(REQUISITION)

    assertThat(response).isEqualTo(success)
    assertThat(api.polled).containsExactly(REQUISITION, REQUISITION, REQUISITION)
    assertThat(currentTime).isEqualTo(6.seconds.inWholeMilliseconds)
  }

  @Test
  fun `Failures stop polling`() = runTest {
    val failed = GoCardlessAccountsResponse.Failed(providerError("RATE_LIMIT_EXCEEDED"))
    api.pollResponses += failed

    assertThat(waiter.await(REQUISITION)).isEqualTo(failed)
    assertThat(api.polled).hasSize(1)
  }

  @Test
  fun `Gives up after ten minutes`() = runTest {
    api.pendingForever = true

    val response = waiter.await(REQUISITION)

    assertThat(response)
      .isEqualTo(
        GoCardlessAccountsResponse.Failed(
          ProviderError(ProviderError.TIMED_OUT, ProviderError.TIMED_OUT),
        ),
      )
    assertThat(currentTime).isEqualTo(10.minutes.inWholeMilliseconds)
  }

  private companion object {
    const val REQUISITION = "requisition-1"
    val ACCOUNT =
      ExternalBankAccount(
        accountId = "ACT-1",
        name = "Checking",
        institution = "My Bank",
        orgId = REQUISITION,
        orgDomain = null,
        balance = null,
      )
  }
}
