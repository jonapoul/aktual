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

private fun dateValue(number: Double): FormulaValue =
  SerialDates.withinBounds(number)?.let(::NumberValue) ?: ErrorValue(Num, DateOutOfRange)

private const val MONTHS_PER_YEAR = 12
