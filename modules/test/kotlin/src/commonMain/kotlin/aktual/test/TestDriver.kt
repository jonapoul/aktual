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
