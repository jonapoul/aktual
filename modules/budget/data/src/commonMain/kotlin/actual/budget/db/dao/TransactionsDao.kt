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
package actual.budget.db.dao

import actual.budget.db.BudgetDatabase
import actual.budget.db.GetIds
import actual.budget.db.GetIdsByAccount
import actual.budget.db.transactions.GetById
import actual.budget.model.AccountId
import actual.budget.model.TransactionId
import alakazam.kotlin.core.CoroutineContexts
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

class TransactionsDao(
  database: BudgetDatabase,
  private val contexts: CoroutineContexts,
) {
  private val queries = database.transactionsQueries

  fun observeById(id: TransactionId): Flow<GetById?> = queries
    .getById(id)
    .asFlow()
    .mapToOneOrNull(contexts.default)
    .distinctUntilChanged()

  fun observeIds(): Flow<List<GetIds>> = queries
    .getIds()
    .asFlow()
    .mapToList(contexts.default)
    .distinctUntilChanged()

  fun observeIdsByAccount(account: AccountId): Flow<List<GetIdsByAccount>> = queries
    .getIdsByAccount(account)
    .asFlow()
    .mapToList(contexts.default)
    .distinctUntilChanged()
}
