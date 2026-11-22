package aktual.budget.list.vm

import aktual.budget.model.BudgetId
import aktual.budget.model.DbMetadata
import aktual.budget.utils.BudgetFiles
import aktual.budget.utils.BudgetImporter
import aktual.budget.utils.ImportResult
import aktual.test.CoTemporaryFolder
import aktual.test.testBudgetFiles
import alakazam.test.TestCoroutineContexts
import app.cash.burst.InterceptTest
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import java.io.IOException
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class ZipBudgetImporterTest {
  @InterceptTest val temporaryFolder = CoTemporaryFolder()

  @Test
  fun `Imports a copy of the file as a new local budget`() = runTest {
    val files = testBudgetFiles(temporaryFolder)
    var copied: String? = null
    var updated: DbMetadata? = null
    val importer =
      importer(files) { _, zip, update ->
        copied = files.fileSystem.read(zip) { readUtf8() }
        val meta = update(CLOUD_METADATA)
        updated = meta
        ImportResult.Success(meta)
      }

    val import = importer.import { "zip contents".byteInputStream() }

    assertThat(import.id).isEqualTo(NEW_ID)
    assertThat(copied).isEqualTo("zip contents")
    assertThat(updated).isEqualTo(DbMetadata(budgetName = "Budget", id = NEW_ID.value))
    assertThat(files.fileSystem.list(files.tmp())).isEmpty()
  }

  @Test
  fun `Failing to read the file is a failure`() = runTest {
    val files = testBudgetFiles(temporaryFolder)
    val importer = importer(files) { _, _, _ -> error("Shouldn't import") }

    val import = importer.import { throw IOException("Gone") }

    assertThat(import.result).isInstanceOf<ImportResult.OtherFailure>()
    assertThat(files.fileSystem.list(files.tmp())).isEmpty()
  }

  private fun importer(files: BudgetFiles, importer: BudgetImporter) =
    ZipBudgetImporter(
      importer = importer,
      files = files,
      uuids = { NEW_ID.value },
      contexts = TestCoroutineContexts(EmptyCoroutineContext),
    )

  private companion object {
    val NEW_ID = BudgetId("new-id")

    val CLOUD_METADATA =
      DbMetadata(
        budgetName = "Budget",
        cloudFileId = BudgetId("cloud-id"),
        groupId = "group",
        id = "old-id",
        encryptKeyId = "key",
      )
  }
}
