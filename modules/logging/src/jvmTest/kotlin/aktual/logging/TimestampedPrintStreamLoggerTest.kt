/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
