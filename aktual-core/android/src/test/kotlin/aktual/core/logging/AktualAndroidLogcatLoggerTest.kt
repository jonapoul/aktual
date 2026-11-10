package aktual.core.logging

import aktual.log
import android.util.Log
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import logcat.LogcatLogger
import logcat.LogcatLogger.Companion.loggers
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLog

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
    loggers += AktualAndroidLogcatLogger(VERBOSE)

    // when
    log.i { "Hello world" }

    // then
    val expected =
      ShadowLog.LogItem(Log.INFO, "AktualAndroidLogcatLoggerTest", "ACTUAL: Hello world", null)
    assertThat(ShadowLog.getLogs().last()).isEqualTo(expected)
  }
}
