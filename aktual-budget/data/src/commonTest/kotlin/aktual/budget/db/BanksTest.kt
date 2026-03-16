package aktual.budget.db

import aktual.budget.db.model.Account
import aktual.budget.db.model.AccountWithBank
import aktual.budget.db.model.Bank
import aktual.budget.db.test.buildAccount
import aktual.budget.db.test.insertAccounts
import aktual.budget.db.test.insertBanks
import aktual.budget.model.AccountId
import aktual.budget.model.BankId
import aktual.test.isEqualToList
import aktual.test.runDatabaseTest
import assertk.assertThat
import kotlin.test.Test
import kotlin.uuid.Uuid

internal class BanksTest {
  private val bankA =
    Bank(
      id = Uuid.parse("9707afb0-dd6a-4623-aab2-922f8e4ab38d"),
      bankId = BankId("a"),
      name = "Acme, Inc",
      tombstone = false,
    )
  private val bankB =
    Bank(
      id = Uuid.parse("55d8cffd-2bf4-4b30-aa8d-604b6fc5d032"),
      bankId = BankId("b"),
      name = "Bankity Bank",
      tombstone = false,
    )
  private val bankC =
    Bank(
      id = Uuid.parse("6143ea9e-db7d-4313-9680-d57a816b0eec"),
      bankId = BankId("c"),
      name = "Charity Bank",
      tombstone = false,
    )

  private val accountA = buildAccount(id = AccountId("a"), bank = bankA.id)
  private val accountB = buildAccount(id = AccountId("b"), bank = bankB.id)
  private val accountC =
    buildAccount(id = AccountId("c"), bank = Uuid.parse("514cb20e-c0af-4a45-bc12-ec7b43819c77"))

  @Test
  fun `Get accounts with bank`() = runDatabaseTest {
    // Given
    val db = buildDatabase()
    db.insertBanks(bankA, bankB, bankC)
    db.insertAccounts(accountA, accountB, accountC)

    // When
    val result = db.banks().getAccountsWithBank()

    // Then
    assertThat(result)
      .isEqualToList(
        toExpected(accountA, bankA),
        toExpected(accountB, bankB),
        toExpected(accountC, bank = null),
      )
  }

  private fun toExpected(account: Account, bank: Bank?) =
    with(account) {
      AccountWithBank(
        id = id,
        accountId = accountId,
        name = name,
        balanceCurrent = balanceCurrent,
        balanceAvailable = balanceAvailable,
        balanceLimit = balanceLimit,
        mask = mask,
        officialName = officialName,
        subtype = subtype,
        bank = this.bank,
        offBudget = offBudget,
        closed = closed,
        tombstone = tombstone,
        sortOrder = sortOrder,
        type = type,
        accountSyncSource = accountSyncSource,
        lastSync = lastSync,
        lastReconciled = lastReconciled,
        bankName = bank?.name,
        bankId = bank?.id,
      )
    }
}
