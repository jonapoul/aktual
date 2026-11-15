package aktual.budget.banksync.domain

import aktual.budget.model.AccountId
import aktual.budget.model.SyncedPrefKey.PerAccount.NewTransactionsSeen
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.first

internal class NewTransactionsTest {
  private val api = FakeBankSyncApi()

  @Test
  fun `A first sync counts its imported transactions as new`() = runBankSyncTest {
    insertLinkedAccount(A)
    api.responses["provider-a"] =
      success(bankTx("-1.00", transactionId = "t1"), bankTx("-2.00", transactionId = "t2"))

    controller(api).sync()

    // Not the starting balance, which wasn't imported
    assertThat(newTransactions.counts.first()).isEqualTo(mapOf(A to 2))
  }

  @Test
  fun `Opening the account clears its count`() = runBankSyncTest {
    insertLinkedAccount(A)
    insertLinkedAccount(B)
    api.responses["provider-a"] = success(bankTx("-1.00", transactionId = "t1"))
    api.responses["provider-b"] = success(bankTx("-1.00", transactionId = "t2"))
    controller(api).sync()

    now += 1.minutes
    newTransactions.markSeen(A)

    assertThat(newTransactions.counts.first()).isEqualTo(mapOf(B to 1))
    assertThat(preferences[NewTransactionsSeen(A)]).isEqualTo(now.toEpochMilliseconds().toString())
  }

  @Test
  fun `Later syncs count what they import after the account was opened`() = runBankSyncTest {
    insertLinkedAccount(A)
    api.responses["provider-a"] = success(bankTx("-1.00", transactionId = "t1"))
    val controller = controller(api)
    controller.sync()
    now += 1.hours
    newTransactions.markSeen(A)
    val seen = preferences[NewTransactionsSeen(A)]

    now += 1.hours
    api.responses["provider-a"] =
      success(bankTx("-1.00", transactionId = "t1"), bankTx("-2.00", transactionId = "t2"))
    controller.sync()

    assertThat(newTransactions.counts.first()).isEqualTo(mapOf(A to 1))
    assertThat(preferences[NewTransactionsSeen(A)]).isEqualTo(seen)
  }

  @Test
  fun `An import reaching back past when the account was opened moves it back`() = runBankSyncTest {
    insertLinkedAccount(A)
    api.responses["provider-a"] = success(bankTx("-1.00", transactionId = "t1"))
    val controller = controller(api)
    controller.sync()
    now += 1.hours
    newTransactions.markSeen(A)

    // Sort orders count down by 1024ms from now, so the second is before the account was opened
    now += 1.seconds
    api.responses["provider-a"] =
      success(
        bankTx("-1.00", transactionId = "t1"),
        bankTx("-2.00", transactionId = "t2"),
        bankTx("-3.00", transactionId = "t3"),
      )
    controller.sync()

    assertThat(newTransactions.counts.first()).isEqualTo(mapOf(A to 2))
    val oldest = now - 1024.milliseconds
    assertThat(preferences[NewTransactionsSeen(A)])
      .isEqualTo((oldest.toEpochMilliseconds() - 1).toString())
  }

  @Test
  fun `Nothing is new in an account that was never synced`() = runBankSyncTest {
    insertLinkedAccount(A)

    newTransactions.markSeen(A)

    assertThat(newTransactions.counts.first()).isEmpty()
  }

  private companion object {
    val A = AccountId("a")
    val B = AccountId("b")
  }
}
