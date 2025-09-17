package actual.budget.prefs

import actual.budget.model.BudgetFiles
import actual.budget.model.BudgetId
import actual.budget.model.DbMetadata
import actual.budget.model.Timestamp
import actual.budget.model.metadata
import actual.budget.model.writeMetadata
import actual.test.CoTemporaryFolder
import actual.test.TestBudgetFiles
import alakazam.kotlin.core.CoroutineContexts
import alakazam.test.core.TestCoroutineContexts
import app.cash.burst.InterceptTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class BudgetLocalPreferencesTest {
  @InterceptTest val temporaryFolder = CoTemporaryFolder()

  private lateinit var files: BudgetFiles
  private lateinit var contexts: CoroutineContexts
  private lateinit var preferences: BudgetLocalPreferences

  @BeforeTest
  fun before() {
    files = TestBudgetFiles(temporaryFolder)
    contexts = TestCoroutineContexts(EmptyCoroutineContext)
  }

  private fun TestScope.buildPreferences(metadata: DbMetadata) {
    preferences = BudgetLocalPreferences(metadata, files, this, contexts)
  }

  @Test
  fun `Not modifying metadata does nothing`() = runTest {
    // given
    val metadata = TEST_METADATA
    files.writeMetadata(metadata)
    val previousWriteTime = fileModificationTime(metadata)

    // when
    buildPreferences(metadata)
    preferences.update {
      // no change
      metadata
    }

    // then
    advanceUntilIdle()
    val newWriteTime = fileModificationTime(metadata)
    assertEquals(previousWriteTime, newWriteTime)
  }

  @Test
  fun `Modifying metadata writes to file`() = runTest {
    // given
    val metadata = TEST_METADATA
    files.writeMetadata(metadata)
    val previousWriteTime = fileModificationTime(metadata)

    // when
    buildPreferences(metadata)
    preferences.update { metadata ->
      metadata + mapOf(
        "some-new-key" to 123,
        "budgetName" to "Hello World",
      )
    }

    // then
    advanceUntilIdle()
    val newWriteTime = fileModificationTime(metadata)
    assertNotEquals(previousWriteTime, newWriteTime)
    assertTrue(newWriteTime > previousWriteTime)
  }

  private fun fileModificationTime(metadata: DbMetadata): Long = files
    .metadata(metadata.cloudFileId)
    .toFile()
    .lastModified()

  private companion object {
    val BUDGET_ID = BudgetId("cf2b43ee-8067-48ed-ab5b-4e4e5531056e")

    val TEST_METADATA = DbMetadata(
      id = "My-Finances-e742ff8",
      budgetName = "Test Budget",
      cloudFileId = BUDGET_ID,
      groupId = "ee90358a-f73e-4aa5-a922-653190fd31b7",
      encryptKeyId = null,
      resetClock = true,
      userId = "583b50fe-3c55-42ca-9f09-a14ecd38677f",
      lastUploaded = LocalDate.parse("2025-02-27"),
      lastScheduleRun = LocalDate.parse("2025-03-03"),
      lastSyncedTimestamp = Timestamp(
        instant = Instant.parse("2025-02-27T19:18:25.454Z"),
        counter = 0L,
        node = "93836f5283a57c87",
      ),
    )
  }
}
