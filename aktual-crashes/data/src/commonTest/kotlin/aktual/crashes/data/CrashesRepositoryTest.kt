package aktual.crashes.data

import aktual.core.logging.CrashReport
import aktual.core.model.CrashFile
import aktual.test.CoTemporaryFolder
import alakazam.test.TestCoroutineContexts
import alakazam.test.unconfinedDispatcher
import app.cash.burst.InterceptTest
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import okio.Path

class CrashesRepositoryTest {
  @InterceptTest val temporaryFolder = CoTemporaryFolder()

  private val fileSystem = FileSystem.SYSTEM

  private val crashDir: Path
    get() = temporaryFolder.root / "crashes"

  @Test
  fun `List reports newest first`() = runTest {
    write(OLDER, "java.lang.Error: Old")
    write(NEWER, "java.lang.Error: New")
    fileSystem.write(crashDir / "other.txt") { writeUtf8("ignored") }

    val reports = repository().reports()

    assertThat(reports.map(CrashReport::file)).containsExactly(NEWER, OLDER)
    assertThat(reports.first().summary).isEqualTo("java.lang.Error: New")
  }

  @Test
  fun `Skip unreadable reports`() = runTest {
    write(OLDER, "java.lang.Error: Old")
    fileSystem.createDirectories(crashDir / NEWER.name)

    val repository = repository()

    assertThat(repository.reports().map(CrashReport::file)).containsExactly(OLDER)
    assertThat(repository.report(NEWER)).isNull()
    assertThat(repository.read(NEWER)).isNull()
  }

  @Test
  fun `Nothing listed without a directory`() = runTest {
    assertThat(repository().reports()).isEmpty()
  }

  @Test
  fun `Delete one and all`() = runTest {
    write(OLDER, "java.lang.Error: Old")
    write(NEWER, "java.lang.Error: New")
    val repository = repository()

    repository.delete(NEWER)
    assertThat(repository.report(NEWER)).isNull()
    assertThat(repository.reports().map(CrashReport::file)).containsExactly(OLDER)

    repository.deleteAll()
    assertThat(repository.reports()).isEmpty()
  }

  private fun TestScope.repository() =
    CrashesRepository(
      storage = { temporaryFolder.root },
      fileSystem = fileSystem,
      contexts = TestCoroutineContexts(unconfinedDispatcher),
    )

  private fun write(file: CrashFile, trace: String) {
    fileSystem.createDirectories(crashDir)
    fileSystem.write(crashDir / file.name) {
      writeUtf8(
        """
        |version: 1.0
        |platform: Linux
        |thread: main
        |
        |$trace
        """
          .trimMargin(),
      )
    }
  }

  private companion object {
    val OLDER = CrashFile("crash-20261017-120000-000.txt")
    val NEWER = CrashFile("crash-20261018-120000-000.txt")
  }
}
