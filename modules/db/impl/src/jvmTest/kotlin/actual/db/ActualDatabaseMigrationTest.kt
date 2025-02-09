package actual.db

import alakazam.test.core.standardDispatcher
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Rule
import org.junit.Test
import java.io.File

class ActualDatabaseMigrationTest {
  private lateinit var db: ActualDatabase
  private val dbFile = File.createTempFile("migration-test", ".db")

  init {
    dbFile.deleteOnExit()
  }

  @get:Rule
  val helper = MigrationTestHelper(
    schemaDirectoryPath = TestBuildConfig.SCHEMAS_PATH.toPath(),
    databasePath = dbFile.toPath(),
    driver = BundledSQLiteDriver(),
    databaseClass = ActualDatabase::class,
  )

  @After
  fun after() {
    if (::db.isInitialized) {
      db.close()
    }
  }

  @Test
  fun `Migrate v1 to latest`() = runTest {
    // Init at v1
    helper.createDatabase(version = 1)

    // Run all migrations
    helper.runMigrationsAndValidate(version = 1, ActualDatabase.migrations())

    // Wrap in a DB object - Room will validate the tables internally
    db = buildDatabase(dbFile, standardDispatcher)
  }
}
