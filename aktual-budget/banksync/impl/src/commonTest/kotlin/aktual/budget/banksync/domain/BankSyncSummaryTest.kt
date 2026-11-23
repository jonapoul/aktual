package aktual.budget.banksync.domain

import aktual.budget.banksync.domain.BankSyncSummary.Companion.MAX_LINKED
import aktual.budget.banksync.domain.BankSyncSummary.Failed
import aktual.budget.banksync.domain.BankSyncSummary.Several
import aktual.budget.banksync.domain.BankSyncSummary.Synced
import aktual.budget.model.AccountId
import aktual.budget.model.TransactionId
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class BankSyncSummaryTest {
  @Test
  fun `Nothing synced has no summary`() = runTest {
    assertThat(BankSyncSummary.of(emptyList(), ::asIs)).isNull()
  }

  @Test
  fun `One account`() = runTest {
    assertThat(BankSyncSummary.of(listOf(synced("a", added = 2, updated = 1)), ::asIs))
      .isEqualTo(
        Synced(
          name = "a",
          added = 2,
          updated = 1,
          linked = LinkedTransactions(ids("a-added-0", "a-added-1"), total = 2),
        ),
      )
    assertThat(BankSyncSummary.of(listOf(failed("a")), ::asIs)).isEqualTo(Failed(name = "a"))
  }

  @Test
  fun `Several accounts total those that synced and link everything they added`() = runTest {
    val results =
      listOf(synced("a", added = 2, updated = 1), failed("b"), synced("c", added = 1, updated = 0))

    assertThat(BankSyncSummary.of(results, ::asIs))
      .isEqualTo(
        Several(
          synced = 2,
          failed = 1,
          added = 3,
          updated = 1,
          linked = LinkedTransactions(ids("a-added-0", "a-added-1", "c-added-0"), total = 3),
        ),
      )
  }

  @Test
  fun `Only the newest added transactions are linked`() = runTest {
    // given
    val results = listOf(synced("a", added = MAX_LINKED + 2, updated = 0))

    // when
    val summary = BankSyncSummary.of(results) { it.asReversed() }

    // then
    val linked = summary?.linked
    assertThat(linked?.total).isEqualTo(MAX_LINKED + 2)
    assertThat(linked?.ids?.size).isEqualTo(MAX_LINKED)
    assertThat(linked?.ids?.first()).isEqualTo(TransactionId("a-added-${MAX_LINKED + 1}"))
  }

  @Test
  fun `Nothing is linked when what was added has since gone`() = runTest {
    val results = listOf(synced("a", added = 1, updated = 0))

    assertThat(BankSyncSummary.of(results) { emptyList() })
      .isEqualTo(Synced(name = "a", added = 1, updated = 0, linked = null))
  }

  private fun asIs(ids: List<TransactionId>) = ids

  private fun ids(vararg ids: String) = ids.map(::TransactionId)

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
