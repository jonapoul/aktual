package aktual.budget.formula

import kotlin.math.floor
import kotlin.math.max
import kotlin.math.pow

// Port of HyperFormula's format/format.js and format/parser.js, used by TEXT. Tries the format as a
// date/time, then as a duration, then as a single number pattern like "0.00", and otherwise returns
// the format unchanged
internal fun formatNumber(value: Double, format: String): String =
  stringifyDateTime(SerialDates.numberToDateTime(value), format)
    ?: stringifyDuration(SerialDates.numberToTime(value), format)
    ?: parseFormat(format, NUMBER_FORMAT, firstOnly = true)?.let { numberFormat(it, value) }
    ?: format

private data class FormatToken(val value: String, val isFormat: Boolean)

private val DATE_FORMAT =
  Regex(
    listOf(
        """\\.""",
        "dd|DD|d|D|mm|MM|m|M|YYYY|YY|yyyy|yy|HH|hh|H|h",
        """ss(\.(0+|s+))?|s""",
        "AM/PM|am/pm|A/P|a/p",
        """\[mm\]|\[MM\]|\[hh\]|\[HH\]""",
      )
      .joinToString(separator = "|", prefix = "(", postfix = ")"),
  )
private val NUMBER_FORMAT = Regex("""(\\.|[#0]+(\.[#0]*)?)""")
private val SECONDS_ITEM = Regex("""^ss(\.(s+|0+))?$""")
private val AM_PM = setOf("a/p", "A/P", "am/pm", "AM/PM")
private const val MILLIS_PATTERN_PREFIX = 3
private const val HOURS_PER_HALF_DAY = 12
private const val MINUTES_PER_HOUR = 60
private const val TWO_DIGIT_YEAR = 100
private const val SINGLE_DIGIT = 10

// Splits the format into pattern tokens and the free text around them. Null when nothing but
// escapes like \d matched
private fun parseFormat(
  format: String,
  regex: Regex,
  firstOnly: Boolean = false,
): List<FormatToken>? {
  val matches = regex.findAll(format).let { if (firstOnly) it.take(1) else it }.toList()
  if (matches.all { it.value.startsWith('\\') }) return null
  val tokens = mutableListOf<FormatToken>()
  var start = 0
  for (match in matches) {
    if (match.range.first != start) {
      tokens += FormatToken(format.substring(start, match.range.first), isFormat = false)
    }
    tokens += FormatToken(match.value, isFormat = !match.value.startsWith('\\'))
    start = match.range.last + 1
  }
  if (start < format.length) tokens += FormatToken(format.substring(start), isFormat = false)
  return tokens
}

private fun stringifyDateTime(dateTime: SimpleDateTime, format: String): String? {
  val tokens = parseFormat(format, DATE_FORMAT) ?: return null
  val (date, time) = dateTime
  val ampm = tokens.any { it.isFormat && it.value in AM_PM }
  var minutes = false
  return buildString {
    for ((i, token) in tokens.withIndex()) {
      if (!token.isFormat) {
        append(token.value)
        continue
      }
      val length = token.value.length
      when (token.value.lowercase()) {
        "h",
        "hh" -> {
          minutes = true
          val hours =
            if (ampm) (time.hours + HOURS_PER_HALF_DAY - 1) % HOURS_PER_HALF_DAY + 1 else time.hours
          append(padLeft(hours, length))
        }
        "d",
        "dd" -> {
          append(padLeft(date.day, length))
        }
        "s",
        "ss" -> {
          append(padLeft(floor(time.seconds), length))
        }
        "m",
        "mm" -> {
          if (tokens.getOrNull(i + 1)?.value?.startsWith(':') == true) minutes = true
          append(padLeft(if (minutes) time.minutes else date.month, length))
          minutes = true
        }
        "yy" -> {
          append(padLeft(date.year % TWO_DIGIT_YEAR, length))
        }
        "yyyy" -> {
          append(jsNumberToString(date.year))
        }
        "am/pm",
        "a/p" -> {
          val (am, pm) = token.value.split('/')
          append(if (time.hours < HOURS_PER_HALF_DAY) am else pm)
        }
        else -> {
          append(fractionalSeconds(token.value, time.seconds) ?: return null)
        }
      }
    }
  }
}

private fun stringifyDuration(time: SimpleTime, format: String): String? {
  val tokens = parseFormat(format, DATE_FORMAT) ?: return null
  var hours = time.hours
  var minutes = time.minutes
  return buildString {
    for (token in tokens) {
      if (!token.isFormat) {
        append(token.value)
        continue
      }
      val length = token.value.length
      when (token.value.lowercase()) {
        "h",
        "hh" -> {
          append(padLeft(hours, length))
          hours = 0.0
        }
        "[hh]" -> {
          append(padLeft(hours, length - 2))
          hours = 0.0
        }
        "m",
        "mm" -> {
          append(padLeft(minutes, length))
          minutes = 0.0
        }
        "[mm]" -> {
          append(padLeft(minutes + MINUTES_PER_HOUR * hours, length - 2))
          minutes = 0.0
          hours = 0.0
        }
        "s",
        "ss" -> {
          append(padLeft(floor(time.seconds), length))
        }
        else -> {
          append(fractionalSeconds(token.value, time.seconds) ?: return null)
        }
      }
    }
  }
}

// Formats like ss.000, or null for any other token
private fun fractionalSeconds(token: String, seconds: Double): String? {
  if (!SECONDS_ITEM.matches(token)) return null
  val multiplier = 10.0.pow(max(token.length - MILLIS_PATTERN_PREFIX, 0))
  val prefix = if (seconds < SINGLE_DIGIT) "0" else ""
  return prefix + jsNumberToString(floor(seconds * multiplier) / multiplier)
}

private fun numberFormat(tokens: List<FormatToken>, value: Double): String = buildString {
  for (token in tokens) {
    if (!token.isFormat) {
      append(token.value)
      continue
    }
    val formatParts = token.value.split('.')
    val integerFormat = formatParts[0]
    val decimalFormat = formatParts.getOrElse(1) { "" }
    val separator = if (decimalFormat.isEmpty()) "" else "."

    val valueParts = jsNumberToString(jsToFixed(value, decimalFormat.length).toDouble()).split('.')
    var integerPart = valueParts[0]
    var decimalPart = valueParts.getOrElse(1) { "" }
    if (integerFormat.length > integerPart.length) {
      val zeros = integerFormat.take(integerFormat.length - integerPart.length).count { it == '0' }
      integerPart = integerPart.padStart(zeros + integerPart.length, '0')
    }
    val zeros = decimalFormat.drop(decimalPart.length).count { it == '0' }
    decimalPart = decimalPart.padEnd(zeros + decimalPart.length, '0')
    append(integerPart + separator + decimalPart)
  }
}

private fun padLeft(number: Double, size: Int): String =
  jsNumberToString(number).padStart(size, '0')

// JavaScript's Number.toFixed, which rounds the exact binary value half up
internal expect fun jsToFixed(value: Double, digits: Int): String
