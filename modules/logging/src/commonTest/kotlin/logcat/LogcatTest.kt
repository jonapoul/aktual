/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package logcat

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import logcat.LogPriority.ASSERT
import logcat.LogPriority.DEBUG
import logcat.LogPriority.ERROR
import logcat.LogPriority.INFO
import logcat.LogPriority.VERBOSE
import logcat.LogPriority.WARN
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class LogcatTest {
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
    logcat.e { e.asLog() }
    val log = logger.latestLog
    assertThat(log?.priority).isEqualTo(ERROR)
    assertThat(log?.tag).isEqualTo("LogcatTest")
    val msg = log?.message.orEmpty()
    assertThat(msg).contains("java.lang.IllegalStateException: SOMETHING BROKE")
    assertThat(msg).contains("at logcat.LogcatTest.logException(LogcatTest.kt:39)")
  }

  @Test
  fun `Logcat shortcuts`() {
    logcat.v { "verbose" }
    logger.assertLatest(priority = VERBOSE, tag = "LogcatTest", message = "verbose")

    logcat.d("DebugTag") { "debug" }
    logger.assertLatest(priority = DEBUG, tag = "DebugTag", message = "debug")

    logcat.i { "info" }
    logger.assertLatest(priority = INFO, tag = "LogcatTest", message = "info")

    logcat.w(tag = null) { "warning" }
    logger.assertLatest(priority = WARN, tag = "LogcatTest", message = "warning")

    logcat.wtf { "PANIC" }
    logger.assertLatest(priority = ASSERT, tag = "LogcatTest", message = "PANIC")
  }

  @Test
  fun `Grab tag from the wrapper class inside apply block`() {
    val myString = "abc-123"

    myString.apply {
      logcat.i { "my log message" }
    }

    logger.assertLatest(priority = INFO, tag = "String", message = "my log message")
  }
}
