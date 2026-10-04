package aktual.budget.banksync.domain

import aktual.budget.banksync.domain.BankSyncSummary.Failed
import aktual.budget.banksync.domain.BankSyncSummary.Several
import aktual.budget.banksync.domain.BankSyncSummary.Synced
import aktual.budget.model.AccountId
import aktual.budget.model.TransactionId
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test

class BankSyncSummaryTest {
  @Test
  fun `Nothing synced has no summary`() {
    assertThat(BankSyncSummary.of(emptyList())).isNull()
  }

  @Test
  fun `One account`() {
    assertThat(BankSyncSummary.of(listOf(synced("a", added = 2, updated = 1))))
      .isEqualTo(Synced(name = "a", added = 2, updated = 1))
    assertThat(BankSyncSummary.of(listOf(failed("a")))).isEqualTo(Failed(name = "a"))
  }

  @Test
  fun `Several accounts total those that synced`() {
    val results =
      listOf(synced("a", added = 2, updated = 1), failed("b"), synced("c", added = 3, updated = 0))

    assertThat(BankSyncSummary.of(results))
      .isEqualTo(Several(synced = 2, failed = 1, added = 5, updated = 1))
  }

  private fun synced(name: String, added: Int, updated: Int) =
    BankSyncResult.Synced(
      account = AccountId(name),
      name = name,
      added = List(added) { TransactionId("$name-added-$it") },
      updated = List(updated) { TransactionId("$name-updated-$it") },
    )

  private fun failed(name: String) =
    BankSyncResult.Failed(AccountId(name), name, BankSyncError.Internal(message = null))
}
