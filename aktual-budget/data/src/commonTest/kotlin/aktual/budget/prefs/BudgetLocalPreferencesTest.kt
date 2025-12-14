package aktual.budget.prefs

import aktual.budget.model.BudgetFiles
import aktual.budget.model.BudgetId
import aktual.budget.model.DbMetadata
import aktual.budget.model.Timestamp
import aktual.budget.model.cloudFileId
import aktual.budget.model.metadata
import aktual.budget.model.writeMetadata
import aktual.test.CoTemporaryFolder
import aktual.test.TestBudgetFiles
import aktual.test.assertThatNextEmissionIsEqualTo
import alakazam.kotlin.core.CoroutineContexts
import alakazam.test.core.TestCoroutineContexts
import app.cash.burst.InterceptTest
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThan
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Instant

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
    preferences = BudgetLocalPreferencesImpl(metadata, files, this, contexts)
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
    assertThat(newWriteTime).isEqualTo(previousWriteTime)
  }

  @Test
  fun `Modifying metadata writes to file`() = runTest {
    // given
    val metadata = TEST_METADATA
    files.writeMetadata(metadata)
    val previousWriteTime = fileModificationTime(metadata)

    // when
    buildPreferences(metadata)
    preferences.update { existing ->
      existing + mapOf(
        DbMetadata.Key("some-new-key") to 123,
        DbMetadata.BudgetName to "Hello World",
      )
    }

    // then
    advanceUntilIdle()
    val newWriteTime = fileModificationTime(metadata)
    assertThat(newWriteTime).isGreaterThan(previousWriteTime)
  }

  @Test
  fun `Observe key`() = runTest {
    // given
    val userId = "abc-123"
    val metadata = TEST_METADATA + (DbMetadata.UserId to userId)
    files.writeMetadata(metadata)

    // when
    buildPreferences(metadata)
    advanceUntilIdle()

    // then
    preferences.observe(DbMetadata.UserId).test {
      assertThatNextEmissionIsEqualTo(userId)

      preferences.update { metadata -> metadata - DbMetadata.UserId }
      assertThatNextEmissionIsEqualTo(null)

      preferences.update { metadata -> metadata + (DbMetadata.UserId to "def-456") }
      assertThatNextEmissionIsEqualTo("def-456")

      expectNoEvents()
      cancelAndIgnoreRemainingEvents()
    }
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
