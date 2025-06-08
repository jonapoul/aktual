package actual.budget.sync.vm

import actual.api.model.sync.UserFile
import actual.api.model.sync.UserWithAccess
import actual.budget.model.BudgetId
import actual.budget.model.DbMetadata
import actual.budget.model.Timestamp
import actual.budget.model.database
import actual.budget.model.metadata
import actual.core.model.TimeZones
import actual.test.TestBudgetFiles
import actual.test.copyTo
import actual.test.resource
import alakazam.test.core.TestClock
import alakazam.test.core.TestCoroutineContexts
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toOkioPath
import okio.buffer
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DatabaseImporterTest {
  @get:Rule val temporaryFolder = TemporaryFolder()

  private lateinit var fileSystem: FileSystem
  private lateinit var root: Path
  private lateinit var budgetFiles: TestBudgetFiles
  private lateinit var importer: DatabaseImporter

  @BeforeTest
  fun before() {
    fileSystem = FileSystem.SYSTEM
    root = temporaryFolder.root.toOkioPath()
    budgetFiles = TestBudgetFiles(fileSystem, root)
    importer = DatabaseImporter(
      contexts = TestCoroutineContexts(EmptyCoroutineContext),
      fileSystem = fileSystem,
      budgetFiles = budgetFiles,
      clock = TestClock { INSTANT },
      timeZones = TimeZones { TimeZone.UTC },
    )
  }

  @Test
  fun `Successfully import`() = runTest {
    // given valid zip is downloaded/decrypted
    val resource = resource("valid.zip")
    val zip = root.resolve("valid.zip")
    resource.copyTo(zip, fileSystem)

    // when
    val result = importer(USER_FILE, zip)

    // then the metadata is parsed
    assertEquals(actual = result, expected = ImportResult.Success(DB_METADATA))

    // and the database is saved too
    val dbPath = budgetFiles.database(BUDGET_ID)
    assertTrue(fileSystem.exists(dbPath), "DB doesn't exist: $dbPath")
    assertEquals(expected = 425_984L, actual = fileSystem.metadata(dbPath).size)

    // and the updated metadata is written
    val metaPath = budgetFiles.metadata(BUDGET_ID)
    assertTrue(fileSystem.exists(metaPath), "Meta doesn't exist: $metaPath")
    val contentsJson = fileSystem.source(metaPath).buffer().use { it.readUtf8() }
    val contents = Json.decodeFromString<DbMetadata>(contentsJson)
    assertEquals(expected = LocalDate(2024, Month.MARCH, 18), actual = contents.lastUploaded)
  }

  @Test
  fun `Fail on invalid ZIP`() = runTest {
    // given a non-zip file pretending to be a zip is downloaded
    val resource = resource("invalid.zip")
    val zip = root.resolve("invalid.zip")
    resource.copyTo(zip, fileSystem)

    // when
    val result = importer(USER_FILE, zip)

    // then
    assertEquals(expected = ImportResult.InvalidZipFile, actual = result)

    // and the budget dir doesn't exist
    val dir = budgetFiles.directory(BUDGET_ID)
    assertFalse(fileSystem.exists(dir))
  }

  private companion object {
    val INSTANT = Instant.fromEpochMilliseconds(1710786854286L) // Mon Mar 18 2024 18:34:14

    val BUDGET_ID = BudgetId("cf2b43ee-8067-48ed-ab5b-4e4e5531056e")

    val USER_FILE = UserFile(
      deleted = 0,
      fileId = BUDGET_ID,
      groupId = "ee90358a-f73e-4aa5-a922-653190fd31b7",
      name = "Test Budget",
      usersWithAccess = listOf(
        UserWithAccess(
          userId = "354d0cfe-cd36-44eb-a404-5990a3ab5c39",
          userName = "",
          displayName = "",
          isOwner = true,
        ),
      ),
    )

    val DB_METADATA = DbMetadata(
      budgetName = USER_FILE.name,
      cloudFileId = BUDGET_ID,
      groupId = USER_FILE.groupId,
      id = "My-Finances-e742ff8",
      lastScheduleRun = LocalDate.parse("2025-03-03"),
      lastSyncedTimestamp = Timestamp(
        instant = Instant.parse("2025-02-27T19:18:25.454Z"),
        counter = 0,
        node = "93836f5283a57c87",
      ),
      lastUploaded = LocalDate.parse("2024-03-18"),
      resetClock = true,
      userId = "583b50fe-3c55-42ca-9f09-a14ecd38677f",
    )
  }
}
