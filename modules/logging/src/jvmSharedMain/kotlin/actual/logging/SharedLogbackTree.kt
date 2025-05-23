package actual.logging

import alakazam.kotlin.logging.BasicTree
import alakazam.kotlin.logging.LogLevel
import ch.qos.logback.classic.AsyncAppender
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy
import ch.qos.logback.core.util.FileSize
import org.slf4j.LoggerFactory
import ch.qos.logback.classic.Logger as LogbackLogger
import org.slf4j.Logger as Slf4jLogger

@Suppress("MagicNumber")
internal abstract class SharedLogbackTree(storage: LogStorage) : BasicTree() {
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

  private val logger: Slf4jLogger = LoggerFactory.getLogger(SharedLogbackTree::class.java)

  override fun log(level: LogLevel, tag: String?, message: String, t: Throwable?) {
    when (level) {
      LogLevel.Verbose -> logger.trace(message, t)
      LogLevel.Debug -> logger.debug(message, t)
      LogLevel.Info -> logger.info(message, t)
      LogLevel.Warn -> logger.warn(message, t)
      LogLevel.Error -> logger.error(message, t)
      LogLevel.Assert -> logger.error(message, t)
    }
  }
}
