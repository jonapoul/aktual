package aktual.budget.db

import aktual.budget.db.test.buildAccount
import aktual.budget.db.test.getAccountById
import aktual.budget.db.test.insertAccounts
import aktual.budget.model.AccountId
import aktual.test.runDatabaseTest
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test

internal class AccountsTest {
  @Test
  fun `Get by ID`() = runDatabaseTest {
    val id1 = AccountId("account-1")
    val id2 = AccountId("account-2")
    val account1 = buildAccount(id = id1)
    val account2 = buildAccount(id = id2)

    val db = buildDatabase()

    assertThat(db.getAccountById(id1)).isNull()
    assertThat(db.getAccountById(id2)).isNull()

    db.insertAccounts(account1)
    assertThat(db.getAccountById(id1)).isEqualTo(account1)
    assertThat(db.getAccountById(id2)).isNull()

    db.insertAccounts(account2)
    assertThat(db.getAccountById(id1)).isEqualTo(account1)
    assertThat(db.getAccountById(id2)).isEqualTo(account2)
  }
}
