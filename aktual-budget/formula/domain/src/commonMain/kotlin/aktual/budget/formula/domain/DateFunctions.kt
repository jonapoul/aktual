package aktual.budget.formula.domain

import aktual.core.Calendar
import kotlin.math.floor
import kotlin.math.truncate
import kotlinx.datetime.number

// Port of the matching functions in HyperFormula's DateTimePlugin. Dates are serial numbers, see
// [SerialDates]
internal fun dateFunctions(calendar: Calendar): Map<String, FormulaFunction> =
  mapOf(
    "TODAY" to
      formulaFunction {
        val today = calendar.today()
        val date =
          SimpleDate(today.year.toDouble(), today.month.number.toDouble(), today.day.toDouble())
        NumberValue(SerialDates.dateToNumber(date))
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
  )

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

@Suppress("MagicNumber")
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
