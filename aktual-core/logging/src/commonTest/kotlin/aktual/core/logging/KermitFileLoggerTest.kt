package aktual.core.logging

import aktual.test.TemporaryFolder
import aktual.test.atIndex
import aktual.test.hasSize
import alakazam.test.TestClock
import app.cash.burst.InterceptTest
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.Instant
import logcat.LogPriority
import logcat.LogcatLogger
import logcat.logcat
import okio.FileSystem
import okio.Path
import okio.buffer

class KermitFileLoggerTest {
  @InterceptTest val temporaryFolder = TemporaryFolder()

  private lateinit var fileSystem: FileSystem
  private lateinit var logStorage: TestLogStorage
  private lateinit var clock: Clock

  @BeforeTest
  fun before() {
    LogcatLogger.uninstall()
    LogcatLogger.install()
    fileSystem = FileSystem.SYSTEM
    logStorage = TestLogStorage(temporaryFolder.newFolder("log"))

    // Sat Feb 07 2026 16:15:22.572
    clock = TestClock(Instant.fromEpochMilliseconds(1770480922572L))
  }

  @AfterTest
  fun after() {
    LogcatLogger.uninstall()
  }

  @Test
  fun `Log to file asynchronously`() {
    // given
    LogcatLogger.loggers +=
      KermitFileLogger(storage = logStorage, minPriority = LogPriority.DEBUG, clock = clock)

    // when
    logcat.i { "Hello world" }
    logcat.v { "This is ignored because it's verbose" }
    logcat.d { "This is just on the edge" }
    logcat.e { "Here's an error" }

    // then
    val logFile = fileSystem.list(logStorage.directory()).first()
    assertThat(logFile.name).isEqualTo("log-2026-02-07.log")

    val lines = fileSystem.source(logFile).buffer().use { it.readUtf8().trim() }.lines()

    assertThat(lines)
      .hasSize(3)
      .atIndex(0) { isEqualTo("2026-02-07 16:15:22.572 / I / Hello world") }
      .atIndex(1) { isEqualTo("2026-02-07 16:15:22.572 / D / This is just on the edge") }
      .atIndex(2) { isEqualTo("2026-02-07 16:15:22.572 / E / Here's an error") }
  }
}

private class TestLogStorage(private val path: Path) : LogStorage {
  override fun directory(): Path = path
}
