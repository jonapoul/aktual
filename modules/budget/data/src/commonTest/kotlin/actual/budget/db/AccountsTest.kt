/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
