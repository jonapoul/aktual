package actual.logging

import alakazam.kotlin.logging.Logger
import alakazam.test.core.TestClock
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toOkioPath
import okio.buffer
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

class JvmTreeFactoryTest {
  @get:Rule val temporaryFolder = TemporaryFolder()

  private lateinit var fileSystem: FileSystem
  private lateinit var logStorage: JvmLogStorage
  private lateinit var logStorageRoot: Path

  @BeforeTest
  fun before() {
    Logger.uprootAll()
    fileSystem = FileSystem.SYSTEM
    logStorageRoot = temporaryFolder.newFolder("log").toOkioPath()
    logStorage = JvmLogStorage(logStorageRoot)
  }

  @AfterTest
  fun after() {
    Logger.uprootAll()
  }

  @Test
  fun `Log to print stream`() {
    // given
    val timeMs = 1748073213000 // Sat May 24 2025 07:53:33 GMT+0000
    val byteArrayOutputStream = ByteArrayOutputStream()
    val factory = JvmTreeFactory(
      stream = PrintStream(byteArrayOutputStream),
      clock = TestClock { Instant.fromEpochMilliseconds(timeMs) },
    )
    ActualLogging.init(factory, logStorage)

    // when
    Logger.i("Hello world")
    Logger.tag("TAGGED").d("This one has a tag")

    // then
    val contents = byteArrayOutputStream.toString().trim().lines()
    byteArrayOutputStream.flush()
    assertEquals(expected = "2025-05-24T07:53:33.000Z I/null: Hello world", actual = contents[0])
    assertEquals(expected = "2025-05-24T07:53:33.000Z D/TAGGED: This one has a tag", actual = contents[1])
  }

  @Test
  fun `Log to file asynchronously`() {
    // given
    val factory = JvmTreeFactory()
    ActualLogging.init(factory, logStorage)

    // when
    Logger.i("Hello world")
    Logger.v("This is ignored because it's verbose")
    Logger.d("This is just on the edge")
    Logger.e("Here's an error, with a formatted argument: %04d", 123)

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
