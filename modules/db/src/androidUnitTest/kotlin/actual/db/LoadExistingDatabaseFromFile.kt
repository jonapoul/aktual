package actual.db

import actual.budget.model.BudgetId
import actual.core.files.AndroidFileSystem
import alakazam.test.core.getResourceAsStream
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import app.cash.sqldelight.db.SqlDriver
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class LoadExistingDatabaseFromFile {
  private lateinit var context: Context
  private lateinit var driver: SqlDriver

  @Before
  fun before() {
    context = ApplicationProvider.getApplicationContext()
  }

  @After
  fun after() {
    driver.close()
  }

  @Test
  fun `Opening existing file and reading table data`() = runTest {
    val file = loadDatabaseIntoFile()
    driver = AndroidSqlDriverFactory(BUDGET_ID, context).create()
    val db = buildDatabase(driver)

    val viewHash = db.metaQueries.withResult {
      getValue(key = "view-hash")
        .executeAsOneOrNull()
        ?.value_
    }

    assertEquals(expected = "c379fa428efd55a684aba4947ad054e0", actual = viewHash)
    assertTrue(file.exists())
  }

  private fun loadDatabaseIntoFile(): File {
    val outputFile = AndroidFileSystem(context).budgetDatabase(BUDGET_ID)
    getResourceAsStream("test-db.sqlite").use { input ->
      outputFile.outputStream().use { output ->
        input.copyTo(output)
      }
    }
    return outputFile
  }

  private companion object {
    val BUDGET_ID = BudgetId("abc-123")
  }
}
