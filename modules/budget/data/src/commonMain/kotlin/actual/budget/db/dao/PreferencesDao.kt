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
import actual.budget.db.PreferencesQueries
import actual.budget.db.withResult
import actual.budget.db.withoutResult
import actual.budget.model.SyncedPrefKey
import alakazam.kotlin.core.CoroutineContexts
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Inject
class PreferencesDao(
  database: BudgetDatabase,
  private val contexts: CoroutineContexts,
) {
  private val queries = database.preferencesQueries

  suspend fun getAll(): Map<SyncedPrefKey, String?> = withResult {
    getAll()
      .executeAsList()
      .associate { (key, value) -> key to value }
  }

  suspend operator fun get(key: SyncedPrefKey): String? = withResult {
    getValue(key)
      .executeAsOneOrNull()
      ?.value_
  }

  suspend operator fun set(key: SyncedPrefKey, value: String?) = withoutResult {
    setValue(key, value)
  }

  fun observe(key: SyncedPrefKey): Flow<String?> = queries
    .getValue(key)
    .asFlow()
    .mapToOneOrNull(contexts.default)
    .map { it?.value_ }
    .distinctUntilChanged()

  suspend fun <R> withResult(
    query: suspend PreferencesQueries.() -> R,
  ): R = queries.withResult(query)

  suspend fun withoutResult(
    query: suspend PreferencesQueries.() -> Unit,
  ) = queries.withoutResult(query)
}
