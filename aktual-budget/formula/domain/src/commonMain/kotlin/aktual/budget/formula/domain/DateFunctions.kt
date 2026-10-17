@file:Suppress("MagicNumber")

package aktual.budget.formula.domain

import aktual.core.Calendar
import kotlin.math.floor
import kotlin.math.truncate
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number

// Port of the matching functions in HyperFormula's DateTimePlugin. Dates are serial numbers, see
// [SerialDates]
internal fun dateFunctions(calendar: Calendar): Map<String, FormulaFunction> =
  mapOf(
    "TODAY" to
      formulaFunction { NumberValue(SerialDates.dateToNumber(calendar.today().toSimpleDate())) },
    "NOW" to
      formulaFunction {
        val now = calendar.now()
        val time = SimpleTime(now.hour.toDouble(), now.minute.toDouble(), now.second.toDouble())
        NumberValue(
          SerialDates.timeToNumber(time) + SerialDates.dateToNumber(now.date.toSimpleDate()),
        )
      },
    "DATE" to
      formulaFunction(Param(Numeric), Param(Numeric), Param(Numeric)) { args ->
        date(args.number(0), args.number(1), args.number(2))
      },
    "YEAR" to datePart { it.year },
    "MONTH" to datePart { it.month },
    "DAY" to datePart { it.day },
    "DAYS" to
      formulaFunction(Param(Numeric, minValue = 0.0), Param(Numeric, minValue = 0.0)) { args ->
        NumberValue(truncate(args.number(0)) - truncate(args.number(1)))
      },
    "EOMONTH" to
      monthShift { date, months -> SerialDates.endOfMonth(SerialDates.offsetMonth(date, months)) },
    "EDATE" to
      monthShift { date, months ->
        SerialDates.truncateDayInMonth(SerialDates.offsetMonth(date, months))
      },
    "DAYS360" to
      formulaFunction(
        Param(Numeric, minValue = 0.0),
        Param(Numeric, minValue = 0.0),
        Param(Logical, default = BooleanValue(false)),
      ) { args ->
        NumberValue(SerialDates.days360(args.number(0), args.number(1), args.boolean(2)))
      },
    "YEARFRAC" to
      formulaFunction(
        Param(Numeric, minValue = 0.0),
        Param(Numeric, minValue = 0.0),
        Param(Integral, default = NumberValue(0.0), minValue = 0.0, maxValue = 4.0),
      ) { args ->
        val first = truncate(args.number(0))
        val second = truncate(args.number(1))
        NumberValue(yearFrac(minOf(first, second), maxOf(first, second), args.number(2).toInt()))
      },
    "WEEKDAY" to
      formulaFunction(Param(Numeric, minValue = 0.0), Param(Numeric, default = NumberValue(1.0))) {
        args ->
        val absoluteDay = floor(SerialDates.toAbsolute(args.number(0)))
        val type = args.number(1)
        val offset = WEEKDAY_OFFSETS[type]
        when {
          type == 3.0 -> NumberValue((absoluteDay - 1) % 7)
          offset == null -> ErrorValue(Num, BadMode)
          else -> NumberValue((absoluteDay - offset) % 7 + 1)
        }
      },
    "WEEKNUM" to
      formulaFunction(Param(Numeric, minValue = 0.0), Param(Numeric, default = NumberValue(1.0))) {
        args ->
        val day = args.number(0)
        val type = args.number(1)
        val offset = WEEKDAY_OFFSETS[type]
        when {
          type == 21.0 -> NumberValue(isoWeekNumber(day))
          offset == null -> ErrorValue(Num, BadMode)
          else -> NumberValue(weekNumber(day, offset))
        }
      },
    "DATEDIF" to
      formulaFunction(
        Param(Numeric, minValue = 0.0),
        Param(Numeric, minValue = 0.0),
        Param(Text),
      ) { args ->
        dateDif(args.number(0), args.number(1), args.text(2))
      },
    "NETWORKDAYS" to
      formulaFunction(
        Param(Numeric, minValue = 0.0),
        Param(Numeric, minValue = 0.0),
        Param(Range, optional = true),
      ) { args ->
        networkDays(args.number(0), args.number(1), args[2] as? ArrayValue)
      },
  )

private fun LocalDate.toSimpleDate(): SimpleDate =
  SimpleDate(year.toDouble(), month.number.toDouble(), day.toDouble())

private fun startOfYear(day: Double): Double =
  SerialDates.dateToNumber(SimpleDate(SerialDates.numberToDate(day).year, 1.0, 1.0))

private fun weekNumber(day: Double, offset: Double): Double {
  val absoluteDay = floor(SerialDates.toAbsolute(day))
  val yearStart = SerialDates.toAbsolute(startOfYear(day))
  return floor((absoluteDay - offset) / 7) - floor((yearStart - offset) / 7) + 1
}

private fun isoWeekNumber(day: Double): Double {
  val absoluteDay = floor(SerialDates.toAbsolute(day))
  val yearStart = SerialDates.toAbsolute(startOfYear(day))
  val firstThursday = yearStart + ((4 - yearStart) % 7 + 7) % 7
  val week = floor((absoluteDay - 1) / 7) - floor((firstThursday - 1) / 7) + 1
  return if (week == 0.0) isoWeekNumber(day - 7) + 1 else week
}

private fun dateDif(startNumber: Double, endNumber: Double, unit: String): FormulaValue {
  if (startNumber > endNumber) return ErrorValue(Num, StartAfterEnd)
  if (unit == "D") return NumberValue(floor(endNumber) - floor(startNumber))
  val start = SerialDates.numberToDate(startNumber)
  val end = SerialDates.numberToDate(endNumber)
  val endBeforeStartDay = if (end.day < start.day) 1 else 0
  val fullYear = end.month > start.month || end.month == start.month && end.day >= start.day
  val result =
    when (unit) {
      "M" -> (end.year - start.year) * 12 + (end.month - start.month) - endBeforeStartDay
      "YM" -> (12 + (end.month - start.month) - endBeforeStartDay) % 12
      "Y" -> if (fullYear) end.year - start.year else end.year - start.year - 1
      "MD" ->
        if (end.day >= start.day) {
          end.day - start.day
        } else {
          val month = if (end.month == 1.0) 12.0 else end.month - 1
          val year = if (end.month == 1.0) end.year - 1 else end.year
          SerialDates.daysInMonth(year, month) + end.day - start.day
        }
      "YD" ->
        if (fullYear) {
          floor(endNumber) - SerialDates.dateToNumber(SimpleDate(end.year, start.month, start.day))
        } else {
          floor(endNumber) -
            floor(startNumber) -
            365 * (end.year - start.year - 1) -
            SerialDates.leapYearsCount(end.year - 1) + SerialDates.leapYearsCount(start.year)
        }
      else -> return ErrorValue(Num, BadMode)
    }
  return NumberValue(result)
}

// Only Saturday and Sunday are weekend days, like NETWORKDAYS.INTL's default
private fun networkDays(
  startNumber: Double,
  endNumber: Double,
  holidays: ArrayValue?,
): FormulaValue {
  var start = truncate(startNumber)
  var end = truncate(endNumber)
  var multiplier = 1.0
  if (start > end) {
    start = end.also { end = start }
    multiplier = -1.0
  }
  val values = holidays?.rows?.flatten().orEmpty()
  values
    .firstOrNull { it is ErrorValue }
    ?.let {
      return it
    }
  val days = mutableSetOf<Double>()
  for (value in values) {
    if (value is NumberValue) {
      days += truncate(value.value)
    } else if (value != EmptyValue) {
      return ErrorValue(Value, WrongArgType)
    }
  }
  val workdayHolidays = days.filter {
    WEEKEND_PATTERN.getOrNull(((SerialDates.toAbsolute(it) - 1) % 7).toInt()) == '0'
  }
  return NumberValue(multiplier * countWorkdays(start, end, workdayHolidays))
}

private fun countWorkdays(start: Double, end: Double, holidays: List<Double>): Double {
  val absoluteEnd = floor(SerialDates.toAbsolute(end))
  val absoluteStart = floor(SerialDates.toAbsolute(start))
  var count = 0.0
  for (i in WEEKEND_PATTERN.indices) {
    if (WEEKEND_PATTERN[i] == '0') {
      count += floor((absoluteEnd + 6 - i) / 7)
      count -= floor((absoluteStart - 1 + 6 - i) / 7)
    }
  }
  return count - holidays.count { it in start..end }
}

private fun date(year: Double, month: Double, day: Double): FormulaValue {
  var y = truncate(year)
  var m = truncate(month)
  if (y < SerialDates.EPOCH_YEAR_ZERO) y += SerialDates.EPOCH_YEAR_ZERO
  val delta = floor((m - 1) / MONTHS_PER_YEAR)
  y += delta
  m -= delta * MONTHS_PER_YEAR
  val firstOfMonth = SimpleDate(y, m, 1.0)
  if (!SerialDates.isValidDate(firstOfMonth)) return ErrorValue(Value, InvalidDate)
  return dateValue(SerialDates.dateToNumber(firstOfMonth) + truncate(day) - 1)
}

private fun datePart(part: (SimpleDate) -> Double): FormulaFunction =
  formulaFunction(Param(Numeric, minValue = 0.0)) { args ->
    NumberValue(part(SerialDates.numberToDate(args.number(0))))
  }

private fun monthShift(shift: (SimpleDate, Double) -> SimpleDate): FormulaFunction =
  formulaFunction(Param(Numeric, minValue = 0.0), Param(Numeric)) { args ->
    val shifted = shift(SerialDates.numberToDate(args.number(0)), args.number(1))
    dateValue(SerialDates.dateToNumber(shifted))
  }

private fun yearFrac(start: Double, end: Double, basis: Int): Double =
  when (basis) {
    0 -> SerialDates.days360(start, end, european = false) / 360
    1 ->
      (end - start) /
        SerialDates.yearLength(SerialDates.numberToDate(start), SerialDates.numberToDate(end))
    2 -> (end - start) / 360
    3 -> (end - start) / 365
    else -> SerialDates.days360(start, end, european = true) / 360
  }

private fun dateValue(number: Double): FormulaValue =
  SerialDates.withinBounds(number)?.let(::NumberValue) ?: ErrorValue(Num, DateOutOfRange)

private const val MONTHS_PER_YEAR = 12

// Monday to Friday are workdays, starting from Monday
private const val WEEKEND_PATTERN = "0000011"

private val WEEKDAY_OFFSETS =
  mapOf(
    1.0 to 0.0,
    2.0 to 1.0,
    11.0 to 1.0,
    12.0 to 2.0,
    13.0 to 3.0,
    14.0 to 4.0,
    15.0 to 5.0,
    16.0 to 6.0,
    17.0 to 0.0,
  )
