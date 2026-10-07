package aktual.budget.banksync.domain

import aktual.api.model.banksync.ExternalBankAccount
import aktual.budget.db.dao.DatabaseTables.ACCOUNTS
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.BankId
import aktual.budget.model.LocalChange
import aktual.budget.model.MessageValue
import assertk.all
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.prop
import kotlin.test.Test

internal class BankAccountLinkerTest {
  private val api = FakeBankSyncApi()

  @Test
  fun `Linking creates the bank and syncs the account`() = runBankSyncTest {
    api.responses["ACT-1"] = success()

    linker(api).link(ACCOUNT, SimpleFin, EXTERNAL)
    runBackground()

    val account = account(ACCOUNT)
    assertThat(account).all {
      prop("account_id") { it.account_id }.isEqualTo("ACT-1")
      prop("account_sync_source") { it.account_sync_source }.isEqualTo(SimpleFin)
    }
    val bank = checkNotNull(account.bank)
    assertThat(dao.bankId(bank)).isEqualTo(BankId("mybank.example.com"))
    assertThat(api.requests.map { it.second.accountId }).containsExactly("ACT-1")
    assertThat(account(ACCOUNT).last_sync).isNotNull()
  }

  @Test
  fun `Linking reuses a bank with the same ID and name`() = runBankSyncTest {
    insertLinkedAccount(OTHER, bankId = "mybank.example.com", bankName = "My Bank")
    api.responses["ACT-1"] = success()

    linker(api).link(ACCOUNT, SimpleFin, EXTERNAL)

    assertThat(account(ACCOUNT).bank).isEqualTo(account(OTHER).bank)
    assertThat(lastSync().map { it.dataset }).containsExactly("accounts", "accounts", "accounts")
  }

  @Test
  fun `Banks without an org domain use the org ID, and match without a name`() = runBankSyncTest {
    insertLinkedAccount(OTHER, bankId = "ORG-1", bankName = null)
    api.responses["ACT-1"] = success()

    linker(api)
      .link(
        ACCOUNT,
        SimpleFin,
        EXTERNAL.copy(orgDomain = null, institution = null),
      )

    assertThat(account(ACCOUNT).bank).isEqualTo(account(OTHER).bank)
  }

  @Test
  fun `Banks without an org domain or ID match on name`() = runBankSyncTest {
    insertLinkedAccount(OTHER, bankId = null, bankName = "My Bank")
    api.responses["ACT-1"] = success()

    linker(api).link(ACCOUNT, SimpleFin, EXTERNAL.copy(orgDomain = null, orgId = null))

    assertThat(account(ACCOUNT).bank).isEqualTo(account(OTHER).bank)
  }

  @Test
  fun `Linking doesn't reuse a deleted bank`() = runBankSyncTest {
    insertLinkedAccount(OTHER, bankId = "mybank.example.com", bankName = "My Bank")
    deleteBanks()
    api.responses["ACT-1"] = success()

    linker(api).link(ACCOUNT, SimpleFin, EXTERNAL)

    assertThat(account(ACCOUNT).bank).isNotNull().isNotEqualTo(account(OTHER).bank)
  }

  @Test
  fun `Linking a missing account fails`() = runBankSyncTest {
    assertFailure {
      linker(api).link(AccountId("missing"), SimpleFin, EXTERNAL)
    }
    assertThat(syncCalls).isEmpty()
  }

  @Test
  fun `Creating adds a linked account with its transfer payee and syncs it`() = runBankSyncTest {
    api.responses["ACT-1"] = success(balance = 123456)

    val created = linker(api).create(SimpleFin, EXTERNAL, offBudget = false)
    val sent = lastSync()
    runBackground()

    assertThat(account(created)).all {
      prop("name") { it.name }.isEqualTo("Checking")
      prop("offbudget") { it.offbudget }.isEqualTo(false)
      prop("sort_order") { it.sort_order }.isEqualTo(16384.0)
      prop("account_id") { it.account_id }.isEqualTo("ACT-1")
      prop("account_sync_source") { it.account_sync_source }.isEqualTo(SimpleFin)
      prop("last_sync") { it.last_sync }.isNotNull()
    }
    val payee = sent.single { it.dataset == "payees" && it.column == "transfer_acct" }
    assertThat(payee.value).isEqualTo(string(created.value))
    assertThat(sent.filter { it.row == payee.row && it.column == "name" }.map { it.value })
      .containsExactly(string(""))
    assertThat(api.requests.map { it.second.accountId }).containsExactly("ACT-1")
  }

  @Test
  fun `New accounts go after the others on or off budget`() = runBankSyncTest {
    syncChanges(
      listOf(LocalChange(ACCOUNTS, ACCOUNT.value, "sort_order", MessageValue.Number(20000)))
    )
    insertAccount(OTHER, offBudget = true)
    val linker = linker(api)

    val onBudget = linker.create(SimpleFin, EXTERNAL, offBudget = false)
    val offBudget = linker.create(SimpleFin, EXTERNAL, offBudget = true)

    assertThat(account(onBudget).sort_order).isEqualTo(36384.0)
    assertThat(account(offBudget)).all {
      prop("offbudget") { it.offbudget }.isEqualTo(true)
      prop("sort_order") { it.sort_order }.isEqualTo(16384.0)
    }
  }

  @Test
  fun `New accounts ignore the order of deleted accounts`() = runBankSyncTest {
    syncChanges(
      listOf(
        LocalChange(ACCOUNTS, ACCOUNT.value, "sort_order", MessageValue.Number(20000)),
        LocalChange(ACCOUNTS, ACCOUNT.value, "tombstone", MessageValue.Number(1)),
      )
    )

    val created = linker(api).create(SimpleFin, EXTERNAL, offBudget = false)

    assertThat(account(created).sort_order).isEqualTo(16384.0)
  }

  @Test
  fun `Unlinking clears the bank sync fields`() = runBankSyncTest {
    insertLinkedAccount(OTHER, source = SimpleFin)
    database.accountsQueries.updateBalance(Amount(100), OTHER)

    linker(api).unlink(OTHER)

    assertThat(account(OTHER)).all {
      prop("account_id") { it.account_id }.isNull()
      prop("bank") { it.bank }.isNull()
      prop("account_sync_source") { it.account_sync_source }.isNull()
      prop("balance_current") { it.balance_current }.isNull()
    }
    assertThat(api.removedRequisitions).isEmpty()
  }

  @Test
  fun `Unlinking an unlinked account does nothing`() = runBankSyncTest {
    linker(api).unlink(ACCOUNT)

    assertThat(syncCalls).isEmpty()
  }

  @Test
  fun `Unlinking the last GoCardless account deletes its requisition`() = runBankSyncTest {
    insertLinkedAccount(OTHER, bankId = "requisition-1")

    linker(api).unlink(OTHER)

    assertThat(api.removedRequisitions).containsExactly("requisition-1")
  }

  @Test
  fun `GoCardless requisitions still in use are kept`() = runBankSyncTest {
    insertLinkedAccount(OTHER, bankId = "requisition-1")
    val bank = checkNotNull(account(OTHER).bank)
    accountDao.insert(id = THIRD, name = "third", bank = bank)

    linker(api).unlink(OTHER)

    assertThat(api.removedRequisitions).isEmpty()
  }

  @Test
  fun `Failing to delete a requisition still unlinks`() = runBankSyncTest {
    insertLinkedAccount(OTHER, bankId = "requisition-1")
    api.removeError = IllegalStateException("Server down")

    linker(api).unlink(OTHER)

    assertThat(account(OTHER).bank).isNull()
  }

  private companion object {
    val OTHER = AccountId("other")
    val THIRD = AccountId("third")
    val EXTERNAL =
      ExternalBankAccount(
        accountId = "ACT-1",
        name = "Checking",
        institution = "My Bank",
        orgId = "ORG-1",
        orgDomain = "mybank.example.com",
        balance = Amount(123456),
      )
  }
}
