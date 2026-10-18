package aktual.about.ui.logs

import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.stringShort
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format.char
import kotlinx.datetime.minus
import logcat.LogPriority

internal val LogPriority.letter: Char
  get() =
    when (this) {
      VERBOSE -> 'V'
      DEBUG -> 'D'
      INFO -> 'I'
      WARN -> 'W'
      ERROR -> 'E'
      ASSERT -> 'A'
    }

@Composable
internal fun LogPriority.label(): String =
  when (this) {
    VERBOSE -> Strings.logsLevelVerbose
    DEBUG -> Strings.logsLevelDebug
    INFO -> Strings.logsLevelInfo
    WARN -> Strings.logsLevelWarn
    ERROR -> Strings.logsLevelError
    ASSERT -> Strings.logsLevelAssert
  }

internal fun LogPriority.foreground(colors: Colors): Color =
  when (this) {
    VERBOSE -> colors.pageTextSubdued
    DEBUG -> colors.pageTextLight
    INFO -> colors.noticeText
    WARN -> colors.warningText
    ERROR,
    ASSERT -> colors.errorText
  }

internal fun LogPriority.background(colors: Colors): Color =
  when (this) {
    VERBOSE,
    DEBUG -> Transparent
    INFO -> colors.noticeBackground
    WARN -> colors.warningBackground
    ERROR,
    ASSERT -> colors.errorBackground
  }

internal fun LogPriority.messageColor(colors: Colors): Color =
  when (this) {
    VERBOSE,
    DEBUG,
    INFO -> colors.pageText
    WARN -> colors.warningText
    ERROR,
    ASSERT -> colors.errorText
  }

@Composable
internal fun LocalDate.dayLabel(today: LocalDate): String =
  when (this) {
    today -> Strings.logsToday
    today.minus(DatePeriod(days = 1)) -> Strings.logsYesterday
    else -> "${dayOfWeek.stringShort()} $day ${month.stringShort()}"
  }

internal fun LocalTime.timeString(): String = TIME_FORMAT.format(this)

private val TIME_FORMAT = LocalTime.Format {
  hour()
  char(':')
  minute()
  char(':')
  second()
  char('.')
  secondFraction(minLength = 3, maxLength = 3)
}
