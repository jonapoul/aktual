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

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.CustomReports
import aktual.budget.db.withResult
import aktual.budget.model.CustomReportId
import alakazam.kotlin.core.CoroutineContexts
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.JsonObject

class CustomReportsDao(database: BudgetDatabase, private val contexts: CoroutineContexts) {
  private val queries = database.customReportsQueries

  suspend fun insert(reports: CustomReports): Long = queries.withResult { insert(reports) }

  suspend operator fun get(id: CustomReportId): CustomReports? = queries.withResult { getById(id).executeAsOneOrNull() }

  fun observeMetadataById(id: CustomReportId): Flow<JsonObject?> = queries
    .getMetadataById(id)
    .asFlow()
    .mapToOneOrNull(contexts.default)
    .map { it?.metadata }
    .distinctUntilChanged()

  suspend fun getIds(): List<CustomReportId> = queries.withResult { getIds().executeAsList() }

  suspend fun getIdByName(name: String): CustomReportId? = queries.withResult { getIdByName(name).executeAsOneOrNull() }

  suspend fun deleteById(id: CustomReportId): Long = queries.withResult { delete(id) }
}
