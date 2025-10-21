/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
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
