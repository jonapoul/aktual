package aktual.budget.db

import aktual.budget.model.BudgetId
import aktual.budget.model.DbMetadata
import aktual.budget.model.DbMetadataKey.Companion.GroupId
import aktual.budget.model.Timestamp
import aktual.budget.utils.BudgetFiles
import aktual.budget.utils.ImportResult
import aktual.test.CoTemporaryFolder
import aktual.test.RESOURCES_DIR
import aktual.test.copyTo
import aktual.test.doesNotExistOn
import aktual.test.existsOn
import aktual.test.testBudgetFiles
import alakazam.test.TestCoroutineContexts
import app.cash.burst.InterceptTest
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Instant
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path
import okio.buffer
import okio.source

class BudgetImporterTest {
  @InterceptTest val temporaryFolder = CoTemporaryFolder()

  private lateinit var fileSystem: FileSystem
  private lateinit var root: Path
  private lateinit var budgetFiles: BudgetFiles
  private lateinit var importer: BudgetImporterImpl

  @BeforeTest
  fun before() {
    fileSystem = temporaryFolder.fileSystem
    root = temporaryFolder.root
    budgetFiles = testBudgetFiles(fileSystem, root)
    importer =
      BudgetImporterImpl(
        contexts = TestCoroutineContexts(EmptyCoroutineContext),
        files = budgetFiles,
      )
  }

  @Test
  fun `Successfully import`() = runTest {
    val zip = resource("valid.zip")

    val result = importer.import(BUDGET_ID, zip) { it.set(GroupId, "new-group") }

    val expected = DB_METADATA.set(GroupId, "new-group")
    assertThat(result).isEqualTo(ImportResult.Success(expected))

    val dbPath = budgetFiles.database(BUDGET_ID)
    assertThat(dbPath).existsOn(fileSystem)
    assertThat(fileSystem.metadata(dbPath).size).isEqualTo(425_984L)

    val metaPath = budgetFiles.metadata(BUDGET_ID)
    val contentsJson = fileSystem.source(metaPath).buffer().use { it.readUtf8() }
    assertThat(Json.decodeFromString<DbMetadata>(contentsJson)).isEqualTo(expected)
  }

  @Test
  fun `Import from a single subdirectory`() = runTest {
    val zip = root / "nested.zip"
    writeZip(zip, "export/$DB_ENTRY" to "db", "export/$METADATA_ENTRY" to """{"budgetName":"A"}""")

    val result = importer.import(BUDGET_ID, zip) { it }

    assertThat(result).isEqualTo(ImportResult.Success(DbMetadata(budgetName = "A")))
  }

  @Test
  fun `Ignore unknown metadata keys`() = runTest {
    val zip = root / "extra-keys.zip"
    writeZip(zip, DB_ENTRY to "db", METADATA_ENTRY to """{"budgetName":"A","somethingNew":1}""")

    val result = importer.import(BUDGET_ID, zip) { it }

    assertThat(result).isInstanceOf<ImportResult.Success>()
  }

  @Test
  fun `Fail when the files are in different directories`() = runTest {
    val zip = root / "split.zip"
    writeZip(zip, "a/$DB_ENTRY" to "db", "b/$METADATA_ENTRY" to "{}")

    val result = importer.import(BUDGET_ID, zip) { it }

    assertThat(result).isEqualTo(InvalidZipFile)
    assertThat(budgetFiles.directory(BUDGET_ID)).doesNotExistOn(fileSystem)
  }

  @Test
  fun `Fail on invalid metadata`() = runTest {
    val zip = root / "bad-meta.zip"
    writeZip(zip, DB_ENTRY to "db", METADATA_ENTRY to "not json")

    val result = importer.import(BUDGET_ID, zip) { it }

    assertThat(result).isEqualTo(InvalidMetaFile)
  }

  @Test
  fun `Fail on a file that isn't a zip`() = runTest {
    val zip = resource("invalid.zip")

    val result = importer.import(BUDGET_ID, zip) { it }

    assertThat(result).isEqualTo(NotZipFile)
    assertThat(budgetFiles.directory(BUDGET_ID)).doesNotExistOn(fileSystem)
  }

  private fun resource(name: String): Path {
    val path = root / name
    RESOURCES_DIR.resolve(name).source().copyTo(path, fileSystem)
    return path
  }

  private fun writeZip(path: Path, vararg entries: Pair<String, String>) {
    fileSystem.sink(path).buffer().outputStream().use { output ->
      ZipOutputStream(output).use { zos ->
        for ((name, contents) in entries) {
          zos.putNextEntry(ZipEntry(name))
          zos.write(contents.encodeToByteArray())
          zos.closeEntry()
        }
      }
    }
  }

  private companion object {
    val BUDGET_ID = BudgetId("cf2b43ee-8067-48ed-ab5b-4e4e5531056e")

    // Contents of valid.zip's metadata.json
    val DB_METADATA =
      DbMetadata(
        budgetName = "Test Budget",
        cloudFileId = BUDGET_ID,
        groupId = "ee90358a-f73e-4aa5-a922-653190fd31b7",
        id = "My-Finances-e742ff8",
        lastScheduleRun = LocalDate.parse("2025-03-03"),
        lastSyncedTimestamp =
          Timestamp(
            instant = Instant.parse("2025-02-27T19:18:25.454Z"),
            counter = 0,
            node = "93836f5283a57c87",
          ),
        lastUploaded = LocalDate.parse("2025-02-27"),
        resetClock = true,
        userId = "583b50fe-3c55-42ca-9f09-a14ecd38677f",
      )
  }
}
