package aktual.core.logging

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import assertk.assertThat
import assertk.assertions.endsWith
import assertk.assertions.hasSize
import logcat.LogPriority
import logcat.LogcatLogger
import logcat.logcat
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
class LogbackLoggerTest {
  private lateinit var context: Context
  private lateinit var logStorage: AndroidLogStorage

  @BeforeTest
  fun before() {
    LogcatLogger.uninstall()
    LogcatLogger.install()
    context = ApplicationProvider.getApplicationContext()
    logStorage = AndroidLogStorage(context)
  }

  @AfterTest
  fun after() {
    LogcatLogger.uninstall()
  }

  @Test
  fun `Log to file asynchronously`() {
    // given
    LogcatLogger.loggers += LogbackLogger(logStorage, minPriority = LogPriority.DEBUG)

    // when
    logcat.i { "Hello world" }
    logcat.v { "This is ignored because it's verbose" }
    logcat.d { "This is just on the edge" }
    logcat.e { "Here's an error, with a formatted argument: %04d".format(123) }

    // then
    val logDir = context.filesDir.resolve("../logs")
    while (!logDir.exists()) {
      Thread.sleep(100L)
    }

    val lines = logDir
      .listFiles()
      .orEmpty()
      .first()
      .readLines()
    assertThat(lines).hasSize(3)
    assertThat(lines[0]).endsWith("INFO  Hello world")
    assertThat(lines[1]).endsWith("DEBUG This is just on the edge")
    assertThat(lines[2]).endsWith("ERROR Here's an error, with a formatted argument: 0123")
  }
}
