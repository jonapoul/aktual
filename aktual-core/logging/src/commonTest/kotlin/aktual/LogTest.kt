package aktual

import aktual.test.contains
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import logcat.LogcatLogger
import logcat.asLog

class LogTest {
  private lateinit var logger: TestLogcatLogger

  @BeforeTest
  fun before() {
    LogcatLogger.install()
    logger = TestLogcatLogger()
    LogcatLogger.loggers += logger
  }

  @AfterTest
  fun after() {
    LogcatLogger.uninstall()
    LogcatLogger.loggers -= logger
  }

  // don't add another test above this - it depends on the line number
  @Test
  fun logException() {
    val e = IllegalStateException("SOMETHING BROKE")
    log.e { e.asLog() }
    val latest = logger.latestLog
    assertThat(latest?.priority).isEqualTo(ERROR)
    assertThat(latest?.tag).isEqualTo("LogTest")
    val msg = latest?.message.orEmpty()
    assertThat(msg)
      .contains("java.lang.IllegalStateException: SOMETHING BROKE")
      .contains("at aktual.LogTest.logException(LogTest.kt:31)")
  }

  @Test
  fun `Log shortcuts`() {
    log.v { "verbose" }
    logger.assertLatest(priority = VERBOSE, tag = "LogTest", message = "verbose")

    log.d("DebugTag") { "debug" }
    logger.assertLatest(priority = DEBUG, tag = "DebugTag", message = "debug")

    log.i { "info" }
    logger.assertLatest(priority = INFO, tag = "LogTest", message = "info")

    log.w(tag = null) { "warning" }
    logger.assertLatest(priority = WARN, tag = "LogTest", message = "warning")

    log.wtf { "PANIC" }
    logger.assertLatest(priority = ASSERT, tag = "LogTest", message = "PANIC")
  }

  @Test
  fun `Grab tag from the wrapper class inside apply block`() {
    val myString = "abc-123"

    myString.apply { log.i { "my log message" } }

    logger.assertLatest(priority = INFO, tag = "String", message = "my log message")
  }
}
