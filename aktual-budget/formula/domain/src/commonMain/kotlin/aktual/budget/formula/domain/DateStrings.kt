@file:Suppress("MagicNumber", "ReturnCount")

package aktual.budget.formula.domain

import kotlin.math.floor
import kotlinx.datetime.LocalDate

// Port of HyperFormula's defaultParseToDateTime and DateTimeHelper, with upstream Actual's date
// formats. Dates become Excel serial numbers: days since 1899-12-30, plus the time as a fraction

private val DATE_FORMATS = listOf("DD/MM/YYYY", "YYYY-MM-DD", "YYYY/MM/DD")
private val TIME_FORMATS = listOf("hh:mm", "hh:mm:ss.sss")
private const val NULL_YEAR = 30
private const val MIN_YEAR = 1582
private const val SECONDS_PRECISION = 1000.0

private val QUICK_CHECK = Regex("""^[0-9/.\-: ]+[ap]?m?$""")
private val WHITESPACE = Regex("""\s+""")
private val DATE_SEPARATOR = Regex("[ /.-]")
private val SECONDS_ITEM = Regex("""^ss(\.(s+|0+))?$""")
private val EPOCH = LocalDate(year = 1899, month = 12, day = 30)

private data class DateParts(val year: Int, val month: Int, val day: Int)

private data class TimeParts(val hours: Double, val minutes: Double, val seconds: Double)

internal fun dateStringToNumber(text: String): Double? {
  for (dateFormat in DATE_FORMATS) {
    for (timeFormat in TIME_FORMATS) {
      val (date, time) = parseSingleFormat(text, dateFormat, timeFormat) ?: continue
      val days = date?.let {
        LocalDate(it.year, it.month, it.day).toEpochDays() - EPOCH.toEpochDays()
      }
      val fraction = time?.let { ((it.seconds / 60 + it.minutes) / 60 + it.hours) / 24 }
      return (days ?: 0L) + (fraction ?: 0.0)
    }
  }
  return null
}

private fun parseSingleFormat(
  text: String,
  dateFormat: String,
  timeFormat: String,
): Pair<DateParts?, TimeParts?>? {
  val (rawDate, time) = parseDateTime(text, dateFormat, timeFormat) ?: return null
  val date = rawDate?.withFullYear() ?: return null to time
  return if (date.isValid()) date to time else null
}

private fun DateParts.withFullYear(): DateParts =
  when (year) {
    in 0..<NULL_YEAR -> copy(year = year + 2000)
    in NULL_YEAR..99 -> copy(year = year + 1900)
    else -> this
  }

private fun DateParts.isValid(): Boolean {
  if (year < MIN_YEAR || month !in 1..12 || day < 1) return false
  val leap = year % 4 == 0 && year % 100 != 0 || year % 400 == 0
  val daysInMonth =
    when (month) {
      2 -> if (leap) 29 else 28
      4,
      6,
      9,
      11 -> 30
      else -> 31
    }
  return day <= daysInMonth
}

private fun parseDateTime(
  text: String,
  dateFormat: String,
  timeFormat: String,
): Pair<DateParts?, TimeParts?>? {
  // JS String.replace with a non-global regex only replaces the first match
  var dateTime = WHITESPACE.replaceFirst(text, " ").trim().lowercase()
  if (!QUICK_CHECK.matches(dateTime)) return null

  var ampm: String? = dateTime.takeLast(2)
  if (ampm == "am" || ampm == "pm") {
    dateTime = dateTime.dropLast(2).trim()
  } else {
    ampm = dateTime.takeLast(1)
    if (ampm == "a" || ampm == "p") {
      dateTime = dateTime.dropLast(1).trim()
    } else {
      ampm = null
    }
  }

  val dateItems = dateTime.split(DATE_SEPARATOR).toMutableList()
  if (dateItems.size >= 2 && dateItems[dateItems.size - 2].contains(':')) {
    dateItems[dateItems.size - 2] = dateItems[dateItems.size - 2] + "." + dateItems.last()
    dateItems.removeAt(dateItems.lastIndex)
  }
  val timeItems = dateItems.last().split(':').toMutableList()
  if (ampm != null) timeItems += ampm

  if (dateItems.size == 1) return parseTime(timeItems, timeFormat)?.let { null to it }
  if (timeItems.size == 1) return parseDate(dateItems, dateFormat)?.let { it to null }
  val date = parseDate(dateItems.dropLast(1), dateFormat) ?: return null
  val time = parseTime(timeItems, timeFormat) ?: return null
  return date to time
}

private fun parseTime(items: MutableList<String>, format: String): TimeParts? {
  val lower = format.lowercase().trim()
  val withoutAmPm =
    when {
      lower.endsWith("am/pm") -> lower.dropLast("am/pm".length)
      lower.endsWith("a/p") -> lower.dropLast("a/p".length)
      else -> lower
    }
  val formatItems = withoutAmPm.trim().split(':')
  val hourItem = formatItems.indexOf("hh")
  val minuteItem = formatItems.indexOf("mm")
  val secondItem = formatItems.indexOfFirst { SECONDS_ITEM.matches(it) }

  val pm =
    when (items.lastOrNull()) {
      "am",
      "a" -> false
      "pm",
      "p" -> true
      else -> null
    }
  if (pm != null) items.removeAt(items.lastIndex)
  if (items.size != formatItems.size) return null

  val secondsParsed = jsNumber(items.getOrNull(secondItem) ?: "0")
  if (!secondsParsed.isFinite()) return null
  val seconds = jsRound(secondsParsed * SECONDS_PRECISION) / SECONDS_PRECISION
  val minutes = jsNumber(items.getOrNull(minuteItem) ?: "0")
  if (!minutes.isWholeNumber()) return null
  val hoursParsed = jsNumber(items.getOrNull(hourItem) ?: "0")
  if (!hoursParsed.isWholeNumber()) return null
  if (pm != null && hoursParsed !in 0.0..12.0) return null
  val hours = if (pm != null) hoursParsed % 12 + (if (pm) 12 else 0) else hoursParsed
  return TimeParts(hours, minutes, seconds)
}

private fun parseDate(items: List<String>, format: String): DateParts? {
  val formatItems = format.lowercase().trim().split(DATE_SEPARATOR)
  if (items.size != formatItems.size) return null

  val day = jsNumber(items.getOrNull(formatItems.indexOf("dd")))
  if (!day.isWholeNumber()) return null
  val month = jsNumber(items.getOrNull(formatItems.indexOf("mm")))
  if (!month.isWholeNumber()) return null

  val longYear = items.getOrNull(formatItems.indexOf("yyyy"))
  val shortYear = items.getOrNull(formatItems.indexOf("yy"))
  if (!longYear.isNullOrEmpty() && !shortYear.isNullOrEmpty()) return null
  val year = jsNumber(longYear ?: shortYear)
  if (!year.isWholeNumber()) return null
  if (!longYear.isNullOrEmpty() && year !in 1000.0..9999.0) return null
  if (!shortYear.isNullOrEmpty() && year !in 0.0..99.0) return null

  return DateParts(year.toInt(), month.toInt(), day.toInt())
}

// JavaScript's Number(string): blank is 0, anything unparseable is NaN
private fun jsNumber(text: String?): Double =
  when {
    text == null -> NaN
    text.isBlank() -> 0.0
    else -> text.trim().toDoubleOrNull() ?: Double.NaN
  }

private fun Double.isWholeNumber(): Boolean = isFinite() && this == floor(this)
