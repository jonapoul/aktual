/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.budget.db

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File
import java.util.Properties

class JvmSqlDriverFactory(private val url: String) : SqlDriverFactory {
  constructor(file: File) : this(url = "jdbc:sqlite:${file.absolutePath}")

  override fun create() = JdbcSqliteDriver(
    url = url,
    schema = BudgetDatabase.Schema.synchronous(),
    properties = Properties().apply {
      put("foreign_keys", "${DatabaseBuildConfig.FOREIGN_KEY_CONSTRAINTS}")
    },
  )
}
