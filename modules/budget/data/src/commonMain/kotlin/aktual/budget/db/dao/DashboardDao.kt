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
import aktual.budget.db.GetPositionAndSize
import aktual.budget.db.withResult
import aktual.budget.db.withoutResult
import aktual.budget.model.WidgetId
import aktual.budget.model.WidgetType
import kotlinx.serialization.json.JsonObject

class DashboardDao(database: BudgetDatabase) {
  private val queries = database.dashboardQueries

  suspend fun insert(
    id: WidgetId,
    type: WidgetType,
    x: Long,
    y: Long,
    meta: JsonObject,
    width: Long = DEFAULT_WIDTH,
    height: Long = DEFAULT_HEIGHT,
  ) = queries.withoutResult { insert(id, type, width, height, x, y, meta) }

  suspend fun getIds(): List<WidgetId> = queries.withResult { getIds().executeAsList() }

  suspend fun deleteById(id: WidgetId): Long = queries.withResult { delete(id) }

  suspend fun getPositionAndSize(): List<GetPositionAndSize> =
    queries.withResult { getPositionAndSize().executeAsList() }

  companion object {
    const val DEFAULT_WIDTH = 4L
    const val DEFAULT_HEIGHT = 2L
    const val MAX_WIDTH = 12L
  }
}
