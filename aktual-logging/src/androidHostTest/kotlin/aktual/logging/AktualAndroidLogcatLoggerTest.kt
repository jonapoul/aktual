package aktual.logging

import android.util.Log
import assertk.assertThat
import assertk.assertions.isEqualTo
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

@RunWith(RobolectricTestRunner::class)
class AktualAndroidLogcatLoggerTest {
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
    loggers += AktualAndroidLogcatLogger(LogPriority.VERBOSE)

    // when
    logcat.i { "Hello world" }

    // then
    val expected = ShadowLog.LogItem(
      Log.INFO,
      "AktualAndroidLogcatLoggerTest",
      "ACTUAL: Hello world",
      null,
    )
    assertThat(ShadowLog.getLogs().last()).isEqualTo(expected)
  }
}
