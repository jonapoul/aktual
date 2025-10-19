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
