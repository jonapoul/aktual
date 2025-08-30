package actual.logging

import actual.test.TemporaryFolder
import app.cash.burst.InterceptTest
import logcat.LogPriority
import logcat.LogcatLogger
import logcat.logcat
import okio.FileSystem
import okio.Path
import okio.buffer
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LogbackLoggerTest {
  @InterceptTest val temporaryFolder = TemporaryFolder()

  private lateinit var fileSystem: FileSystem
  private lateinit var logStorage: JvmLogStorage
  private lateinit var logStorageRoot: Path

  @BeforeTest
  fun before() {
    LogcatLogger.uninstall()
    LogcatLogger.install()
    fileSystem = FileSystem.SYSTEM
    logStorageRoot = temporaryFolder.newFolder("log")
    logStorage = JvmLogStorage(logStorageRoot)
  }

  @AfterTest
  fun after() {
    LogcatLogger.uninstall()
  }

  @Test
  fun `Log to file asynchronously`() {
    // given
    LogcatLogger.loggers += LogbackLogger(
      storage = logStorage,
      minPriority = LogPriority.DEBUG,
    )

    // when
    logcat.i { "Hello world" }
    logcat.v { "This is ignored because it's verbose" }
    logcat.d { "This is just on the edge" }
    logcat.e { "Here's an error, with a formatted argument: %04d".format(123) }

    // then
    val logDir = logStorage.directory()
    while (!fileSystem.exists(logDir)) {
      Thread.sleep(100L)
    }

    val logFilePath = fileSystem.list(logDir).first()
    val lines = fileSystem
      .source(logFilePath)
      .buffer()
      .use { it.readUtf8().trim() }
      .lines()
    assertEquals(expected = 3, actual = lines.size)
    assertTrue(lines[0].endsWith("INFO  Hello world"))
    assertTrue(lines[1].endsWith("DEBUG This is just on the edge"))
    assertTrue(lines[2].endsWith("ERROR Here's an error, with a formatted argument: 0123"))
  }
}
