@file:Suppress("MagicNumber")

package aktual.budget.formula.domain

import kotlin.math.floor

// Port of HyperFormula's DateTimeHelper with the default config: serial 0 is 1899-12-30 and 1900
// isn't a leap year. Fields are doubles like upstream's, so out of range input gives the same odd
// results, and NaN wherever upstream would index past the end of an array
internal data class SimpleDate(val year: Double, val month: Double, val day: Double)

internal data class SimpleTime(val hours: Double, val minutes: Double, val seconds: Double)

internal data class SimpleDateTime(val date: SimpleDate, val time: SimpleTime)

internal object SerialDates {
  private val DAYS_IN_MONTH = listOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
  private val DAYS_BEFORE_MONTH = listOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
  private const val DAYS_PER_YEAR = 365.2425
  private const val MIN_VALID_YEAR = 1582
  private const val SECONDS_EPSILON = 100_000

  // Two digit years in DATE count from here
  const val EPOCH_YEAR_ZERO = 1900

  private val NULL_DATE = dateToNumberFromZero(SimpleDate(1899.0, 12.0, 30.0))
  private val MAX_DATE = dateToNumber(SimpleDate(9999.0, 12.0, 31.0))

  fun dateToNumber(date: SimpleDate): Double = dateToNumberFromZero(date) - NULL_DATE

  fun withinBounds(number: Double): Double? = number.takeIf { it in 0.0..MAX_DATE }

  fun numberToDate(number: Double): SimpleDate {
    val dateNumber = floor(number) + NULL_DATE
    var year = floor(dateNumber / DAYS_PER_YEAR)
    if (dateToNumberFromZero(SimpleDate(year + 1, 1.0, 1.0)) <= dateNumber) {
      year++
    } else if (dateToNumberFromZero(SimpleDate(year - 1, 1.0, 1.0)) > dateNumber) {
      year--
    }
    val dayOfYear = dateNumber - dateToNumberFromZero(SimpleDate(year, 1.0, 1.0))
    val leap = isLeapYear(year)
    val month = monthOfDay(dayOfYear - if (leap && dayOfYear >= 59) 1 else 0)
    val day = dayOfYear - DAYS_BEFORE_MONTH[month] - if (leap && month > 1) 1 else 0
    return SimpleDate(year, month + 1.0, day + 1)
  }

  fun numberToTime(number: Double): SimpleTime {
    // Multiplied in the same order as upstream, so rounding matches
    val asSeconds = number * 24 * 60 * 60
    val seconds = jsRound(asSeconds % 60 * SECONDS_EPSILON) / SECONDS_EPSILON % 60
    val asMinutes = (asSeconds - seconds) / 60
    val minutes = jsRound(asMinutes % 60) % 60
    val hours = jsRound((asMinutes - minutes) / 60)
    return SimpleTime(hours, minutes, seconds)
  }

  fun numberToDateTime(number: Double): SimpleDateTime {
    val time = numberToTime(number % 1)
    val carryDays = floor(time.hours / 24)
    return SimpleDateTime(
      date = numberToDate(floor(number) + carryDays),
      time = time.copy(hours = time.hours % 24),
    )
  }

  fun isValidDate(date: SimpleDate): Boolean =
    date.year.isWhole() &&
      date.month.isWhole() &&
      date.day.isWhole() &&
      date.year >= MIN_VALID_YEAR &&
      date.month in 1.0..12.0 &&
      date.day >= 1 &&
      date.day <= daysInMonth(date.year, date.month)

  fun endOfMonth(date: SimpleDate): SimpleDate = date.copy(day = daysInMonth(date.year, date.month))

  fun offsetMonth(date: SimpleDate, offset: Double): SimpleDate {
    val totalMonths = 12 * date.year + date.month - 1 + offset
    return SimpleDate(floor(totalMonths / 12), totalMonths % 12 + 1, date.day)
  }

  // Ignores leap years, so the 29th of February becomes the 28th
  fun truncateDayInMonth(date: SimpleDate): SimpleDate =
    date.copy(day = minOf(date.day, DAYS_IN_MONTH.at(date.month - 1)))

  private fun daysInMonth(year: Double, month: Double): Double =
    if (isLeapYear(year) && month == 2.0) 29.0 else DAYS_IN_MONTH.at(month - 1)

  private fun dateToNumberFromZero(date: SimpleDate): Double {
    val leapYears = leapYearsCount(if (date.month <= 2) date.year - 1 else date.year)
    return 365 * date.year + DAYS_BEFORE_MONTH.at(date.month - 1) + date.day - 1 + leapYears
  }

  private fun leapYearsCount(year: Double): Double =
    floor(year / 4) - floor(year / 100) + floor(year / 400)

  private fun isLeapYear(year: Double): Boolean =
    year % 4 == 0.0 && (year % 100 != 0.0 || year % 400 == 0.0)

  private fun monthOfDay(dayOfYear: Double): Int {
    var month = 0
    if (DAYS_BEFORE_MONTH[month + 6] <= dayOfYear) month += 6
    if (DAYS_BEFORE_MONTH[month + 3] <= dayOfYear) month += 3
    if (DAYS_BEFORE_MONTH[month + 2] <= dayOfYear) {
      month += 2
    } else if (DAYS_BEFORE_MONTH[month + 1] <= dayOfYear) {
      month += 1
    }
    return month
  }

  // A JavaScript array lookup, where anything but a valid index is undefined and so NaN
  private fun List<Int>.at(index: Double): Double =
    if (index.isWhole() && index >= 0 && index < size) get(index.toInt()).toDouble() else NaN

  private fun Double.isWhole(): Boolean = isFinite() && this == floor(this)
}
