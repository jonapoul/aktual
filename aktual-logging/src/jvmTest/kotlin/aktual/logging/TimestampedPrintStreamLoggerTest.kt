/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.logging

import alakazam.test.core.TestClock
import assertk.assertThat
import assertk.assertions.isEqualTo
import logcat.LogPriority
import logcat.LogcatLogger
import logcat.logcat
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Instant

class TimestampedPrintStreamLoggerTest {
  @BeforeTest
  fun before() {
    LogcatLogger.install()
  }

  @AfterTest
  fun after() {
    LogcatLogger.uninstall()
  }

  @Test
  fun `Log to print stream`() {
    // given
    val timeMs = 1748073213000 // Sat May 24 2025 07:53:33 GMT+0000
    val byteArrayOutputStream = ByteArrayOutputStream()
    val stream = PrintStream(byteArrayOutputStream)
    val clock = TestClock { Instant.fromEpochMilliseconds(timeMs) }
    LogcatLogger.loggers += TimestampedPrintStreamLogger(stream, clock, minPriority = LogPriority.DEBUG)

    // when
    logcat.i { "Hello world" }
    logcat.d("TAGGED") { "This one has a tag" }

    // then
    val contents = byteArrayOutputStream.toString().trim().lines()
    byteArrayOutputStream.flush()
    assertThat(contents).isEqualTo(
      listOf(
        "2025-05-24T07:53:33.000Z I/TimestampedPrintStreamLoggerTest: Hello world",
        "2025-05-24T07:53:33.000Z D/TAGGED: This one has a tag",
      ),
    )
  }
}
