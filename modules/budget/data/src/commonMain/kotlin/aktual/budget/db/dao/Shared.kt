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

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver

fun <T> SqlDriver.lastInsertRowId(mapper: SqlCursor.(index: Int) -> T?): T {
  val result = executeQuery<T>(
    identifier = null,
    parameters = 0,
    sql = "SELECT last_insert_rowid()",
    mapper = { cursor -> QueryResult.Value(cursor.mapper(0) ?: error("No value matching mapper!")) },
  )
  return result.value
}
