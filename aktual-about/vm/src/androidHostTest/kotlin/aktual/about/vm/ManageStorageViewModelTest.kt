package aktual.about.vm

import aktual.about.vm.ManageStorageState.Loaded
import aktual.core.AppDirectory
import aktual.core.logging.LogStorage
import aktual.core.model.bytes
import aktual.test.CoTemporaryFolder
import aktual.test.testBudgetFiles
import alakazam.test.TestCoroutineContexts
import alakazam.test.unconfinedDispatcher
import app.cash.burst.InterceptTest
import app.cash.turbine.test
import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import io.mockk.mockk
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okio.FileSystem
import okio.Path

class ManageStorageViewModelTest {
  @InterceptTest val temporaryFolder = CoTemporaryFolder()

  private fun TestScope.buildViewModel(root: Path, logsDir: Path): ManageStorageViewModel {
    Dispatchers.setMain(unconfinedDispatcher)
    return ManageStorageViewModel(
      files = testBudgetFiles(SYSTEM, root / "budgets"),
      appDirectory = AppDirectory { root },
      logStorage = LogStorage { logsDir },
      contexts = TestCoroutineContexts(unconfinedDispatcher),
      customThemeCache = mockk(),
      runLevelState = mockk(),
      runLevelController = mockk(),
      dataStore = mockk(),
    )
  }

  @AfterTest
  fun after() {
    Dispatchers.resetMain()
  }

  @Test
  fun `Logs inside app directory aren't double counted`() = runTest {
    val root = temporaryFolder.newFolder("app")
    write(root / "budgets" / "abc" / "db.sqlite", size = BUDGET_SIZE)
    write(root / "logs" / "2026-10-19.log", size = LOGS_SIZE)
    write(root / "other.txt", size = OTHER_SIZE)
    val viewModel = buildViewModel(root, logsDir = root / "logs")
    val rootSize = sizeOnDisk(root)

    viewModel.state.test {
      assertThat(awaitItem() as Loaded).all {
        prop(Loaded::totalSize).isEqualTo(rootSize.bytes)
        prop(Loaded::logsSize).isEqualTo(LOGS_SIZE.bytes)
        prop(Loaded::otherSize).isEqualTo((rootSize - BUDGET_SIZE - LOGS_SIZE).bytes)
      }
    }
  }

  @Test
  fun `Logs outside app directory are added to the total`() = runTest {
    val root = temporaryFolder.newFolder("app")
    val logsDir = temporaryFolder.newFolder("logs")
    write(root / "budgets" / "abc" / "db.sqlite", size = BUDGET_SIZE)
    write(logsDir / "2026-10-19.log", size = LOGS_SIZE)
    write(root / "other.txt", size = OTHER_SIZE)
    val viewModel = buildViewModel(root, logsDir)
    val rootSize = sizeOnDisk(root)

    viewModel.state.test {
      assertThat(awaitItem() as Loaded).all {
        prop(Loaded::totalSize).isEqualTo((rootSize + LOGS_SIZE).bytes)
        prop(Loaded::logsSize).isEqualTo(LOGS_SIZE.bytes)
        prop(Loaded::otherSize).isEqualTo((rootSize - BUDGET_SIZE).bytes)
      }
    }
  }

  private fun write(path: Path, size: Long) {
    path.parent?.let(FileSystem.SYSTEM::createDirectories)
    FileSystem.SYSTEM.write(path) { write(ByteArray(size.toInt())) }
  }

  // Directory entries have a size too, so measure the tree the same way the VM does
  private fun sizeOnDisk(path: Path): Long =
    FileSystem.SYSTEM.listRecursively(path).sumOf { FileSystem.SYSTEM.metadata(it).size ?: 0L }

  private companion object {
    const val BUDGET_SIZE = 1000L
    const val LOGS_SIZE = 200L
    const val OTHER_SIZE = 30L
  }
}
