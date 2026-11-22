package aktual.budget.db

import aktual.budget.db.BudgetExporterImpl.Companion.DB_ENTRY
import aktual.budget.db.BudgetExporterImpl.Companion.METADATA_ENTRY
import aktual.budget.model.BudgetId
import aktual.budget.model.DbMetadata
import aktual.budget.utils.BudgetFiles
import aktual.test.CoTemporaryFolder
import aktual.test.TestBudgetLocalPreferences
import aktual.test.TestCalendar
import aktual.test.fileDriverFactory
import aktual.test.inMemoryDriverFactory
import aktual.test.testBudgetFiles
import alakazam.test.TestCoroutineContexts
import app.cash.burst.InterceptTest
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import java.util.zip.ZipInputStream
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import okio.Path
import okio.buffer

class BudgetExporterTest {
  @InterceptTest val temporaryFolder = CoTemporaryFolder()

  private lateinit var files: BudgetFiles

  @BeforeTest
  fun before() {
    files = testBudgetFiles(temporaryFolder)
  }

  @Test
  fun `Zip holds the database without its cache, and metadata asking for a new clock`() = runTest {
    inMemoryDriverFactory().create(ID).use { driver ->
      driver.execute(null, "INSERT INTO tags(id, tag) VALUES ('t1', 'food')", 0)
      driver.execute(null, "INSERT INTO kvcache(key, value) VALUES ('a', 'b')", 0)
      driver.execute(null, "INSERT INTO kvcache_key(id, key) VALUES (1, 2.0)", 0)
      val metadata = DbMetadata(budgetName = "My: Budget", groupId = "group")

      val export = exporter(driver, metadata).export()

      assertThat(export.fileName).isEqualTo("2026-11-22-My- Budget.zip")
      val entries = unzip(export.path)
      assertThat(entries.keys.toList()).containsExactly(DB_ENTRY, METADATA_ENTRY)

      val exportedMetadata =
        Json.decodeFromString(
          DbMetadata.serializer(),
          entries.getValue(METADATA_ENTRY).decodeToString(),
        )
      assertThat(exportedMetadata)
        .isEqualTo(DbMetadata(budgetName = "My: Budget", groupId = "group", resetClock = true))

      val dbFile = temporaryFolder.root / "exported.sqlite"
      files.fileSystem.write(dbFile) { write(entries.getValue(DB_ENTRY)) }
      fileDriverFactory(dbFile.toFile()).create(ID).use { exported ->
        assertThat(exported.count("tags")).isEqualTo(1L)
        assertThat(exported.count("kvcache")).isEqualTo(0L)
        assertThat(exported.count("kvcache_key")).isEqualTo(0L)
      }

      // The open database keeps its cache
      assertThat(driver.count("kvcache")).isEqualTo(1L)
    }
  }

  private fun exporter(driver: SqlDriver, metadata: DbMetadata) =
    BudgetExporterImpl(
      id = ID,
      driver = driver,
      files = files,
      preferences = TestBudgetLocalPreferences(metadata),
      calendar = TestCalendar(LocalDate(2026, 11, 22)),
      contexts = TestCoroutineContexts(EmptyCoroutineContext),
    )

  private fun unzip(path: Path): Map<String, ByteArray> = buildMap {
    files.fileSystem.source(path).buffer().inputStream().use { input ->
      ZipInputStream(input).use { zis ->
        var entry = zis.nextEntry
        while (entry != null) {
          put(entry.name, zis.readBytes())
          entry = zis.nextEntry
        }
      }
    }
  }

  private fun SqlDriver.count(table: String): Long =
    executeQuery(
        identifier = null,
        sql = "SELECT COUNT(*) FROM $table",
        mapper = { cursor ->
          cursor.next()
          QueryResult.Value(cursor.getLong(0)!!)
        },
        parameters = 0,
      )
      .value

  private companion object {
    val ID = BudgetId("abc-123")
  }
}
