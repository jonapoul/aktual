package aktual.budget.db

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File
import java.util.Properties

class JvmSqlDriverFactory(private val url: String) : SqlDriverFactory {
  constructor(file: File) : this(url = "jdbc:sqlite:${file.absolutePath}")

  override fun create(): SqlDriver =
    JdbcSqliteDriver(
      url = url,
      schema = BudgetDatabase.Schema.synchronous(),
      properties =
        Properties().apply { put("foreign_keys", "${DatabaseBuildConfig.FOREIGN_KEY_CONSTRAINTS}") },
    )
}
