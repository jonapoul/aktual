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
