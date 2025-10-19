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
package aktual.budget.db.dao

import aktual.budget.db.Accounts
import aktual.budget.db.BudgetDatabase
import aktual.budget.db.withResult
import aktual.budget.model.AccountId
import alakazam.kotlin.core.CoroutineContexts
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.flow.Flow

class AccountsDao(database: BudgetDatabase, private val contexts: CoroutineContexts) {
  private val queries = database.accountsQueries

  fun observe(id: AccountId): Flow<Accounts?> = queries
    .getById(id)
    .asFlow()
    .mapToOneOrNull(contexts.default)

  suspend operator fun get(id: AccountId): Accounts? = queries.withResult { getById(id).executeAsOneOrNull() }
}
