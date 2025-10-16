package actual.budget.db

import actual.budget.db.test.buildAccount
import actual.budget.db.test.getAccountById
import actual.budget.db.test.insertAccounts
import actual.budget.model.AccountId
import actual.test.runDatabaseTest
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

    assertThat(getAccountById(id1)).isNull()
    assertThat(getAccountById(id2)).isNull()

    insertAccounts(account1)
    assertThat(getAccountById(id1)).isEqualTo(account1)
    assertThat(getAccountById(id2)).isNull()

    insertAccounts(account2)
    assertThat(getAccountById(id1)).isEqualTo(account1)
    assertThat(getAccountById(id2)).isEqualTo(account2)
  }
}
