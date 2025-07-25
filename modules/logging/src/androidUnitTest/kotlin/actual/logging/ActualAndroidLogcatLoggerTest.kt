package actual.logging

import android.util.Log
import logcat.LogPriority
import logcat.LogcatLogger
import logcat.LogcatLogger.Companion.loggers
import logcat.logcat
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLog
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class ActualAndroidLogcatLoggerTest {
  @BeforeTest
  fun before() {
    LogcatLogger.uninstall()
    LogcatLogger.install()
  }

  @AfterTest
  fun after() {
    LogcatLogger.uninstall()
  }

  @Test
  fun `Log to logcat`() {
    // given
    loggers += ActualAndroidLogcatLogger(LogPriority.VERBOSE)

    // when
    logcat.i { "Hello world" }

    // then
    assertEquals(
      actual = ShadowLog.getLogs().last(),
      expected = ShadowLog.LogItem(
        Log.INFO,
        "ActualAndroidLogcatLogg", // trimmed tag
        "ACTUAL: Hello world",
        null,
      ),
    )
  }
}
