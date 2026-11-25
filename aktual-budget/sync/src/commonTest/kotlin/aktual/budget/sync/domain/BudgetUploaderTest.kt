package aktual.budget.sync.domain

import aktual.api.client.BudgetSyncApi
import aktual.api.client.UploadRejectedException
import aktual.budget.model.BudgetId
import aktual.budget.model.DbMetadata
import aktual.budget.model.SyncResponse
import aktual.budget.utils.BudgetExport
import aktual.budget.utils.BudgetFiles
import aktual.budget.utils.UploadResult
import aktual.core.model.BudgetServer
import aktual.core.model.Token
import aktual.test.CoTemporaryFolder
import aktual.test.TestBudgetLocalPreferences
import aktual.test.TestBudgetSync
import aktual.test.doesNotExistOn
import aktual.test.testBudgetFiles
import alakazam.test.TestCoroutineContexts
import app.cash.burst.InterceptTest
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import okio.ByteString

class BudgetUploaderTest {
  @InterceptTest val temporaryFolder = CoTemporaryFolder()

  private lateinit var files: BudgetFiles
  private val api = TestBudgetSyncApi()
  private val sync = TestBudgetSync()

  @BeforeTest
  fun before() {
    files = testBudgetFiles(temporaryFolder)
  }

  @Test
  fun `Upload a new budget and save its cloud IDs`() = runTest {
    val prefs = TestBudgetLocalPreferences(DbMetadata(budgetName = "Household"))

    val result = uploader(prefs).upload()

    assertThat(result).isEqualTo(Success)
    assertThat(api.uploads).containsExactly(Upload(CLOUD_ID, "Household", null, ZIP))
    assertThat(prefs.value)
      .isEqualTo(
        DbMetadata(
          budgetName = "Household",
          cloudFileId = CLOUD_ID,
          groupId = GROUP_ID,
          lastUploaded = TODAY,
        ),
      )
    assertThat(sync.scheduled).isEqualTo(1)
    assertThat(zipPath()).doesNotExistOn(files.fileSystem)
  }

  @Test
  fun `Reuse existing cloud IDs`() = runTest {
    val existing = BudgetId("existing")
    val prefs =
      TestBudgetLocalPreferences(
        DbMetadata(budgetName = "Household", cloudFileId = existing, groupId = "old-group"),
      )

    uploader(prefs).upload()

    assertThat(api.uploads).containsExactly(Upload(existing, "Household", "old-group", ZIP))
  }

  @Test
  fun `Keep the budget local when the server rejects it`() = runTest {
    val prefs = TestBudgetLocalPreferences(DbMetadata(budgetName = "Household"))
    api.failure = UploadRejectedException(Unauthorized)

    val result = uploader(prefs).upload()

    assertThat(result).isEqualTo(UploadResult.Rejected("unauthorized"))
    assertThat(prefs.value).isEqualTo(DbMetadata(budgetName = "Household"))
    assertThat(sync.scheduled).isEqualTo(0)
    assertThat(zipPath()).doesNotExistOn(files.fileSystem)
  }

  @Test
  fun `No server`() = runTest {
    val result = uploader(TestBudgetLocalPreferences(), server = None).upload()

    assertThat(result).isEqualTo(NoServer)
    assertThat(api.uploads).isEmpty()
  }

  @Test
  fun `Encrypted budgets aren't uploaded`() = runTest {
    val prefs = TestBudgetLocalPreferences(DbMetadata(encryptKeyId = "key"))

    val result = uploader(prefs).upload()

    assertThat(result).isEqualTo(Encrypted)
    assertThat(api.uploads).isEmpty()
  }

  private fun zipPath() = temporaryFolder.root / "export.zip"

  private fun uploader(
    prefs: TestBudgetLocalPreferences,
    server: BudgetServer = BudgetServer.Remote(Demo, Token("token")),
  ) =
    BudgetUploaderImpl(
      server = server,
      exporter = {
        files.fileSystem.write(zipPath()) { write(ZIP) }
        BudgetExport(zipPath(), "export.zip")
      },
      api = api,
      preferences = prefs,
      files = files,
      sync = sync,
      uuids = { CLOUD_ID.value },
      calendar = { TODAY },
      contexts = TestCoroutineContexts(EmptyCoroutineContext),
    )

  private data class Upload(
    val id: BudgetId,
    val name: String,
    val groupId: String?,
    val zip: List<Byte>,
  ) {
    constructor(
      id: BudgetId,
      name: String,
      groupId: String?,
      zip: ByteArray,
    ) : this(id, name, groupId, zip.toList())
  }

  private class TestBudgetSyncApi : BudgetSyncApi {
    val uploads = mutableListOf<Upload>()
    var failure: Exception? = null

    override suspend fun syncBudget(requestBody: ByteString): SyncResponse = error("Unused")

    override suspend fun renameBudget(id: BudgetId, name: String) = error("Unused")

    override suspend fun uploadBudget(
      id: BudgetId,
      name: String,
      groupId: String?,
      zip: ByteArray,
    ): String {
      failure?.let { throw it }
      uploads += Upload(id, name, groupId, zip)
      return GROUP_ID
    }
  }

  private companion object {
    val CLOUD_ID = BudgetId("0f8b1c2e-3d4a-4b5c-9e6f-0a1b2c3d4e5f")
    const val GROUP_ID = "group"
    val TODAY = LocalDate(2026, 11, 23)
    val ZIP = byteArrayOf(1, 2, 3)
  }
}
