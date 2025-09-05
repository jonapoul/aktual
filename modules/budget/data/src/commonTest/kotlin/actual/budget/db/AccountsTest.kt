package actual.budget.db

import actual.budget.db.test.buildAccount
import actual.budget.db.test.getAccountById
import actual.budget.db.test.insertAccounts
import actual.budget.model.AccountId
import actual.test.runDatabaseTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class AccountsTest {
  @Test
  fun `Get by ID`() = runDatabaseTest {
    val id1 = AccountId("account-1")
    val id2 = AccountId("account-2")
    val account1 = buildAccount(id = id1)
    val account2 = buildAccount(id = id2)

    assertNull(getAccountById(id1))
    assertNull(getAccountById(id2))

    insertAccounts(account1)
    assertEquals(account1, getAccountById(id1))
    assertNull(getAccountById(id2))

    insertAccounts(account2)
    assertEquals(account1, getAccountById(id1))
    assertEquals(account2, getAccountById(id2))
  }
}
