package aktual.about.data

import aktual.core.logging.LogLine
import aktual.core.model.bytes
import aktual.test.CoTemporaryFolder
import aktual.test.isEqualToList
import alakazam.test.TestClock
import alakazam.test.TestCoroutineContexts
import alakazam.test.unconfinedDispatcher
import app.cash.burst.InterceptTest
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import kotlin.test.Test
import kotlin.time.Instant
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import okio.FileSystem
import okio.Path

class LogsRepositoryTest {
  @InterceptTest val temporaryFolder = CoTemporaryFolder()

  private val dir: Path
    get() = temporaryFolder.root

  private val fileSystem = FileSystem.SYSTEM

  @Test
  fun `List files newest first with error counts`() = runTest {
    write("log-2026-10-17.log", "$TS / E / Api / Failed\n$TS / I / Api / Fine\n")
    write("log-2026-10-18.log", "$TS / I / Main / Hello\n")
    write("other.txt", "ignored")

    val files = repository().files()

    assertThat(files)
      .isEqualToList(
        LogFile(TODAY, 43.bytes, errorCount = 0),
        LogFile(YESTERDAY, 84.bytes, errorCount = 1),
      )
  }

  @Test
  fun `List dates newest first`() = runTest {
    write("log-2026-10-17.log", "$TS / I / Api / Old\n")
    write("log-2026-10-18.log", "$TS / I / Api / New\n")
    write("other.txt", "ignored")

    assertThat(repository().dates()).isEqualToList(TODAY, YESTERDAY)
  }

  @Test
  fun `Watch an older file emits once`() = runTest {
    write("log-2026-10-17.log", "$TS / W / Api / Slow\n")

    repository().watch(YESTERDAY).test {
      assertThat(awaitItem()).isEqualToList(LogLine(TIME, WARN, "Api", "Slow"))
      awaitComplete()
    }
  }

  @Test
  fun `Watch today picks up new lines`() = runTest {
    write("log-2026-10-18.log", "$TS / I / Main / One\n")

    repository().watch(TODAY).test {
      assertThat(awaitItem()).isEqualToList(LogLine(TIME, INFO, "Main", "One"))

      append("log-2026-10-18.log", "$TS / D / Main / Two\n")
      assertThat(awaitItem())
        .isEqualToList(LogLine(TIME, INFO, "Main", "One"), LogLine(TIME, DEBUG, "Main", "Two"))
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Read combines files with headers`() = runTest {
    write("log-2026-10-17.log", "$TS / I / Api / Old\n")
    write("log-2026-10-18.log", "$TS / I / Api / New\n")

    val text = repository().read(listOf(YESTERDAY, TODAY))

    assertThat(text)
      .isEqualTo(
        """
        ==== log-2026-10-18.log ====
        $TS / I / Api / New

        ==== log-2026-10-17.log ====
        $TS / I / Api / Old

        """
          .trimIndent(),
      )
  }

  @Test
  fun `Delete all only removes log files`() = runTest {
    write("log-2026-10-18.log", "$TS / I / Main / Hello\n")
    write("other.txt", "kept")

    val repository = repository()
    repository.deleteAll()

    assertThat(repository.files()).isEmpty()
    assertThat(fileSystem.exists(dir / "log-2026-10-18.log")).isFalse()
    assertThat(fileSystem.list(dir).map { it.name }).isEqualToList("other.txt")
  }

  @Test
  fun `Delete one day`() = runTest {
    write("log-2026-10-17.log", "$TS / I / Api / Old\n")
    write("log-2026-10-18.log", "$TS / I / Api / New\n")

    val repository = repository()
    repository.delete(YESTERDAY)

    assertThat(repository.files().map { it.date }).isEqualToList(TODAY)
  }

  private fun TestScope.repository() =
    LogsRepository(
      storage = { dir },
      fileSystem = fileSystem,
      contexts = TestCoroutineContexts(unconfinedDispatcher),
      clock = TestClock(NOW),
      timeZones = { UTC },
    )

  private fun write(name: String, text: String) = fileSystem.write(dir / name) { writeUtf8(text) }

  private fun append(name: String, text: String) =
    fileSystem.appendingSink(dir / name).use { sink ->
      okio.Buffer().writeUtf8(text).let { sink.write(it, it.size) }
    }

  private companion object {
    const val TS = "2026-10-18 12:00:00.000"
    val TIME = LocalDateTime(2026, 10, 18, 12, 0, 0)
    val TODAY = LocalDate(2026, 10, 18)
    val YESTERDAY = LocalDate(2026, 10, 17)
    val NOW = Instant.parse("2026-10-18T12:30:00Z")
  }
}
