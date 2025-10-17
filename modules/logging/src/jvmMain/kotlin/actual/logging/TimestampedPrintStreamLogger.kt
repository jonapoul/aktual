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
package actual.logging

import kotlinx.datetime.LocalDate
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.char
import logcat.LogPriority
import logcat.MinPriorityLogger
import java.io.PrintStream
import kotlin.time.Clock

class TimestampedPrintStreamLogger(
  private val stream: PrintStream,
  private val clock: Clock,
  override val minPriority: LogPriority,
) : MinPriorityLogger {
  override fun log(priority: LogPriority, tag: String, message: String) {
    val time = clock.now().format(TIMESTAMP_FORMAT)
    stream.println("$time ${priority.char()}/$tag: $message")
  }

  private fun LogPriority.char() = when (this) {
    LogPriority.VERBOSE -> 'V'
    LogPriority.DEBUG -> 'D'
    LogPriority.INFO -> 'I'
    LogPriority.WARN -> 'W'
    LogPriority.ERROR -> 'E'
    LogPriority.ASSERT -> 'A'
  }

  private companion object {
    val TIMESTAMP_FORMAT = DateTimeComponents.Format {
      date(LocalDate.Formats.ISO)
      char('T')
      hour()
      char(':')
      minute()
      char(':')
      second()
      char('.')
      secondFraction(3)
      offset(UtcOffset.Formats.ISO)
    }
  }
}
