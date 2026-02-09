package aktual.budget.db

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
      Banks(
          id = Uuid.parse("9707afb0-dd6a-4623-aab2-922f8e4ab38d"),
          bank_id = BankId("a"),
          name = "Acme, Inc",
          tombstone = false,
      )
  private val bankB =
      Banks(
          id = Uuid.parse("55d8cffd-2bf4-4b30-aa8d-604b6fc5d032"),
          bank_id = BankId("b"),
          name = "Bankity Bank",
          tombstone = false,
      )
  private val bankC =
      Banks(
          id = Uuid.parse("6143ea9e-db7d-4313-9680-d57a816b0eec"),
          bank_id = BankId("c"),
          name = "Charity Bank",
          tombstone = false,
      )

  private val accountA =
      buildAccount(
          id = AccountId("a"),
          bank = bankA.id,
      )
  private val accountB =
      buildAccount(
          id = AccountId("b"),
          bank = bankB.id,
      )
  private val accountC =
      buildAccount(
          id = AccountId("c"),
          bank = Uuid.parse("514cb20e-c0af-4a45-bc12-ec7b43819c77"),
      )

  @Test
  fun `Get accounts with bank`() = runDatabaseTest {
    // Given
    insertBanks(bankA, bankB, bankC)
    insertAccounts(accountA, accountB, accountC)

    // When
    val result = banksQueries.withResult { getAccountsWithBank().executeAsList() }

    // Then
    assertThat(result)
        .isEqualToList(
            toExpected(accountA, bankA),
            toExpected(accountB, bankB),
            toExpected(accountC, bank = null),
        )
  }

  private fun toExpected(account: Accounts, bank: Banks?) =
      with(account) {
        GetAccountsWithBank(
            id = id,
            account_id = account_id,
            name = name,
            balance_current = balance_current,
            balance_available = balance_available,
            balance_limit = balance_limit,
            mask = mask,
            official_name = official_name,
            subtype = subtype,
            bank = this.bank,
            offbudget = offbudget,
            closed = closed,
            tombstone = tombstone,
            sort_order = sort_order,
            type = type,
            account_sync_source = account_sync_source,
            last_sync = last_sync,
            last_reconciled = last_reconciled,
            bankName = bank?.name,
            bankId = bank?.id,
        )
      }
}
