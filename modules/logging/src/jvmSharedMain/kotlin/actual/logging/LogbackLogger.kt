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

import ch.qos.logback.classic.AsyncAppender
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy
import ch.qos.logback.core.util.FileSize
import logcat.LogPriority
import logcat.MinPriorityLogger
import org.slf4j.LoggerFactory
import ch.qos.logback.classic.Logger as LogbackLogger
import org.slf4j.Logger as Slf4jLogger

@Suppress("MagicNumber")
class LogbackLogger(
  storage: LogStorage,
  override val minPriority: LogPriority,
) : MinPriorityLogger {
  init {
    val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
    loggerContext.reset() // Clear default config

    val patternLayoutEncoder = PatternLayoutEncoder().apply {
      context = loggerContext
      pattern = "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %msg%n"
      start()
    }

    val sizeAndTimeBasedRollingPolicy = SizeAndTimeBasedRollingPolicy<ILoggingEvent>().apply {
      context = loggerContext
      fileNamePattern = storage.directory().resolve("log-%d{yyyy-MM-dd}.%i.log").toString()
      maxHistory = 30
      setMaxFileSize(FileSize.valueOf("5MB"))
      setTotalSizeCap(FileSize.valueOf("50MB"))
      setParent(null) // Will be set below
    }

    val rollingFileAppender = RollingFileAppender<ILoggingEvent>().apply {
      context = loggerContext
      encoder = patternLayoutEncoder
      rollingPolicy = sizeAndTimeBasedRollingPolicy
      isPrudent = true
      start()
    }

    val asyncAppender = AsyncAppender().apply {
      context = loggerContext
      name = "ASYNC"
      addAppender(rollingFileAppender)
      isIncludeCallerData = false
    }

    sizeAndTimeBasedRollingPolicy.setParent(rollingFileAppender)
    sizeAndTimeBasedRollingPolicy.start()
    rollingFileAppender.start()
    asyncAppender.start()

    val root = LoggerFactory.getLogger(Slf4jLogger.ROOT_LOGGER_NAME) as LogbackLogger
    with(root) {
      level = Level.DEBUG
      addAppender(asyncAppender)
    }
  }

  private val slf4jLogger: Slf4jLogger = LoggerFactory.getLogger(LogbackLogger::class.java)

  override fun log(priority: LogPriority, tag: String, message: String) = with(slf4jLogger) {
    when (priority) {
      LogPriority.VERBOSE -> trace(message)
      LogPriority.DEBUG -> debug(message)
      LogPriority.INFO -> info(message)
      LogPriority.WARN -> warn(message)
      LogPriority.ERROR -> error(message)
      LogPriority.ASSERT -> error(message)
    }
  }
}
