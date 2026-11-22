package aktual.budget.navrail.vm

import aktual.budget.model.DbMetadata
import aktual.budget.utils.BudgetExport
import aktual.budget.utils.BudgetExporter
import aktual.test.CoTemporaryFolder
import aktual.test.TestBudgetLocalPreferences
import aktual.test.assertThatNextEmissionIsEqualTo
import aktual.test.testBudgetFiles
import aktual.test.viewModelContext
import app.cash.burst.InterceptTest
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import okio.IOException

class ExportBudgetViewModelTest {
  @InterceptTest val temporaryFolder = CoTemporaryFolder()

  @Test
  fun `Exporting hands over a file that copies the zip and deletes it when done`() = runTest {
    val zip = temporaryFolder.root / "export.zip"
    temporaryFolder.fileSystem.write(zip) { writeUtf8("zip contents") }
    val viewModel = viewModel { BudgetExport(path = zip, fileName = "2026-11-22-Budget.zip") }

    viewModel.exports.test {
      viewModel.state.test {
        assertThatNextEmissionIsEqualTo(Idle)
        viewModel.export(Share)
        assertThatNextEmissionIsEqualTo(Exporting)
        assertThatNextEmissionIsEqualTo(Idle)
      }

      val ready = awaitItem()
      assertThat(ready.fileName).isEqualTo("2026-11-22-Budget.zip")
      assertThat(ready.target).isEqualTo(Share)
      val output = ByteArrayOutputStream()
      ready.write(output)
      assertThat(output.toString(Charsets.UTF_8)).isEqualTo("zip contents")
      assertThat(temporaryFolder.fileSystem.exists(zip)).isTrue()

      ready.onDone()
      assertThat(temporaryFolder.fileSystem.exists(zip)).isFalse()
    }
  }

  @Test
  fun `Resetting while exporting cancels the export`() = runTest {
    val gate = CompletableDeferred<BudgetExport>()
    val viewModel = viewModel { gate.await() }

    viewModel.exports.test {
      viewModel.state.test {
        assertThatNextEmissionIsEqualTo(Idle)
        viewModel.export(Share)
        assertThatNextEmissionIsEqualTo(Exporting)
        viewModel.reset()
        assertThatNextEmissionIsEqualTo(Idle)
      }

      gate.complete(BudgetExport(path = temporaryFolder.root / "export.zip", fileName = "x.zip"))
      expectNoEvents()
    }
  }

  @Test
  fun `Failing to export shows the error until reset`() = runTest {
    val viewModel = viewModel { throw IOException("Disk full") }

    viewModel.state.test {
      assertThatNextEmissionIsEqualTo(Idle)
      viewModel.export(Save)
      assertThatNextEmissionIsEqualTo(Exporting)
      assertThatNextEmissionIsEqualTo(ExportBudgetState.Failed("Disk full"))

      viewModel.reset()
      assertThatNextEmissionIsEqualTo(Idle)
    }
  }

  @Test
  fun `Encrypted budgets are flagged`() = runTest {
    assertThat(viewModel(DbMetadata(encryptKeyId = "key")) { error("unused") }.isEncrypted).isTrue()
    assertThat(viewModel { error("unused") }.isEncrypted).isFalse()
  }

  private fun TestScope.viewModel(
    metadata: DbMetadata = DbMetadata(),
    exporter: BudgetExporter,
  ) =
    ExportBudgetViewModel(
      exporter = exporter,
      files = testBudgetFiles(temporaryFolder),
      localPreferences = TestBudgetLocalPreferences(metadata),
      viewModelContext = viewModelContext,
    )
}
