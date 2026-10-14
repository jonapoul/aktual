package aktual.budget.banksync.domain

import aktual.budget.model.AccountId
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import kotlin.test.Test

internal class AccountDeleterTest {
  private val api = FakeBankSyncApi()

  @Test
  fun `Deleting a linked account unlinks it and removes its bank login`() = runBankSyncTest {
    insertLinkedAccount(LINKED, bankId = "requisition-1")

    val removed = deleter().delete(LINKED)

    assertThat(removed).isTrue()
    assertThat(account(LINKED).tombstone).isEqualTo(true)
    assertThat(account(LINKED).bank).isNull()
    assertThat(api.removedRequisitions).containsExactly("requisition-1")
  }

  @Test
  fun `A bank login that can't be removed still deletes the account`() = runBankSyncTest {
    insertLinkedAccount(LINKED, bankId = "requisition-1")
    api.removeError = IllegalStateException("Server down")

    val removed = deleter().delete(LINKED)

    assertThat(removed).isFalse()
    assertThat(account(LINKED).tombstone).isEqualTo(true)
  }

  @Test
  fun `A failed unlink after deleting still reports the account deleted`() = runBankSyncTest {
    insertLinkedAccount(LINKED, bankId = "requisition-1")
    syncErrorAfter = syncCalls.size + 1
    syncError = IllegalStateException("Disk full")

    val removed = deleter().delete(LINKED)

    assertThat(removed).isFalse()
    assertThat(account(LINKED).tombstone).isEqualTo(true)
  }

  @Test
  fun `A failed delete keeps the account linked`() = runBankSyncTest {
    insertLinkedAccount(LINKED, bankId = "requisition-1")
    syncError = IllegalStateException("Disk full")

    assertFailure { deleter().delete(LINKED) }

    assertThat(account(LINKED).tombstone).isEqualTo(false)
    assertThat(account(LINKED).bank).isNotNull()
    assertThat(api.removedRequisitions).isEmpty()
  }

  private fun BankSyncTestScope.deleter() = AccountDeleter(linker = linker(api), writer = writer)

  private companion object {
    val LINKED = AccountId("linked")
  }
}
