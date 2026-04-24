package aktual.budget.db

import app.cash.sqldelight.db.SqlDriver

fun interface SqlDriverFactory {
  fun create(): SqlDriver
}
