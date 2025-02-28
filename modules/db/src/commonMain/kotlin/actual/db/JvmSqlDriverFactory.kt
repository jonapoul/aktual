package actual.db

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.util.Properties

class JvmSqlDriverFactory(private val url: String) : SqlDriverFactory {
  override fun create() = JdbcSqliteDriver(
    url = url,
    schema = BudgetDatabase.Schema.synchronous(),
    properties = Properties().apply {
      put("foreign_keys", "${DatabaseBuildConfig.FOREIGN_KEY_CONSTRAINTS}")
    },
  )
}
