/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
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
