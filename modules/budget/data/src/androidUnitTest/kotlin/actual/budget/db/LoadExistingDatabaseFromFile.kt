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
package actual.budget.db

import actual.budget.model.AndroidBudgetFiles
import actual.budget.model.BudgetFiles
import actual.budget.model.BudgetId
import actual.budget.model.database
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import app.cash.sqldelight.db.SqlDriver
import assertk.assertThat
import assertk.assertions.exists
import assertk.assertions.isEqualTo
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.io.InputStream
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
class LoadExistingDatabaseFromFile {
  private lateinit var fileSystem: FileSystem
  private lateinit var context: Context
  private lateinit var budgetFiles: BudgetFiles
  private lateinit var driver: SqlDriver

  @BeforeTest
  fun before() {
    context = ApplicationProvider.getApplicationContext()
    fileSystem = FileSystem.SYSTEM
    budgetFiles = AndroidBudgetFiles(context, fileSystem)
  }

  @AfterTest
  fun after() {
    driver.close()
  }

  @Test
  fun `Opening existing file and reading table data`() = runTest {
    val file = loadDatabaseIntoFile()
    driver = AndroidSqlDriverFactory(BUDGET_ID, context, budgetFiles).create()
    val db = buildDatabase(driver)

    val viewHash = db.metaQueries.withResult {
      getValue(key = "view-hash")
        .executeAsOneOrNull()
        ?.value_
    }

    assertThat(viewHash).isEqualTo("c379fa428efd55a684aba4947ad054e0")
    assertThat(file).exists()
  }

  private fun loadDatabaseIntoFile(): File {
    val databaseFile = budgetFiles.database(BUDGET_ID, mkdirs = true).toFile()
    getResourceAsStream("test-db.sqlite").use { input ->
      databaseFile.outputStream().use { output ->
        input.copyTo(output)
      }
    }
    return databaseFile
  }

  private fun getResourceAsStream(filename: String): InputStream =
    this.javaClass.classLoader?.getResourceAsStream(filename)
      ?: error("Null input stream for $filename!")

  private companion object {
    val BUDGET_ID = BudgetId("abc-123")
  }
}
