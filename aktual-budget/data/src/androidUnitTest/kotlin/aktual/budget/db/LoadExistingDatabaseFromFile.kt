/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.budget.db

import aktual.budget.model.AndroidBudgetFiles
import aktual.budget.model.BudgetFiles
import aktual.budget.model.BudgetId
import aktual.budget.model.database
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
