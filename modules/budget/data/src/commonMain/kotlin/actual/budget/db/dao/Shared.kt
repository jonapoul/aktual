package actual.budget.db.dao

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver

fun <T> SqlDriver.lastInsertRowId(mapper: SqlCursor.(index: Int) -> T?): T {
  val result = executeQuery<T>(
    identifier = null,
    parameters = 0,
    sql = "SELECT last_insert_rowid()",
    mapper = { cursor -> QueryResult.Value(cursor.mapper(0)!!) },
  )
  return result.value
}
