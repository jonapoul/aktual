/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.test

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.JvmSqlDriverFactory
import aktual.budget.db.SqlDriverFactory
import aktual.budget.db.buildDatabase
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import java.io.File

fun inMemoryDriverFactory() = JvmSqlDriverFactory(JdbcSqliteDriver.IN_MEMORY)

fun fileDriverFactory(file: File) = JvmSqlDriverFactory(file)

fun runDatabaseTest(
  driverFactory: SqlDriverFactory = inMemoryDriverFactory(),
  action: suspend BudgetDatabase.(TestScope) -> Unit,
) = runTest {
  val driver = driverFactory.create()
  val database = buildDatabase(driver)
  driver.use { action(database, this) }
}
