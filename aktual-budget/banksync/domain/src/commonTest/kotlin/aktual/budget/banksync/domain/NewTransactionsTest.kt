package aktual.budget.banksync.domain

import aktual.budget.model.AccountId
import aktual.budget.model.TransactionId
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import kotlin.test.Test

class NewTransactionsTest {
  private val newTransactions = NewTransactions()

  @Test
  fun `Counts what each sync adds`() {
    newTransactions += listOf(synced("a", added = 2, updated = 1), synced("b", added = 1))
    newTransactions += listOf(synced("a", added = 3))

    assertThat(newTransactions.counts.value)
      .isEqualTo(mapOf(AccountId("a") to 5, AccountId("b") to 1))
  }

  @Test
  fun `Ignores accounts with nothing added`() {
    newTransactions +=
      listOf(
        synced("a", added = 0, updated = 2),
        BankSyncResult.Failed(AccountId("b"), "b", BankSyncError.Internal(null)),
      )

    assertThat(newTransactions.counts.value).isEmpty()
  }

  @Test
  fun `Marking an account read clears only its count`() {
    newTransactions += listOf(synced("a", added = 2), synced("b", added = 1))

    newTransactions.markRead(AccountId("a"))

    assertThat(newTransactions.counts.value).isEqualTo(mapOf(AccountId("b") to 1))
  }

  private fun synced(name: String, added: Int, updated: Int = 0) =
    BankSyncResult.Synced(
      account = AccountId(name),
      name = name,
      added = List(added) { TransactionId("$name-added-$it") },
      updated = List(updated) { TransactionId("$name-updated-$it") },
    )
}
