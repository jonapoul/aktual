package actual.db

import actual.budget.model.AccountId
import actual.db.test.buildAccount
import actual.db.test.getAccountById
import actual.db.test.insertAccount
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class AccountsTest : DatabaseTest() {
  @Test
  fun `Get by ID`() = runDatabaseTest {
    val id1 = AccountId("account-1")
    val id2 = AccountId("account-2")
    val account1 = buildAccount(id = id1)
    val account2 = buildAccount(id = id2)

    assertNull(getAccountById(id1))
    assertNull(getAccountById(id2))

    insertAccount(account1)
    assertEquals(account1, getAccountById(id1))
    assertNull(getAccountById(id2))

    insertAccount(account2)
    assertEquals(account1, getAccountById(id1))
    assertEquals(account2, getAccountById(id2))
  }
}
