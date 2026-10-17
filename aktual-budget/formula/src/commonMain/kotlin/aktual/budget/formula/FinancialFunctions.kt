@file:Suppress("MagicNumber")

package aktual.budget.formula

import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

// Port of the matching functions in HyperFormula's FinancialPlugin. Like upstream, a type argument
// is any truthy number, meaning payments at the start of each period
internal val FINANCIAL_FUNCTIONS: Map<String, FormulaFunction> =
  mapOf(
    "PMT" to
      formulaFunction(
        Param(Numeric),
        Param(Numeric),
        Param(Numeric),
        Param(Numeric, default = NumberValue(0.0)),
        Param(Numeric, default = NumberValue(0.0)),
      ) { args ->
        NumberValue(
          pmt(args.number(0), args.number(1), args.number(2), args.number(3), args.number(4)),
        )
      },
    "IPMT" to periodPayment(::ipmt),
    "PPMT" to periodPayment(::ppmt),
    "FV" to
      formulaFunction(
        Param(Numeric),
        Param(Numeric),
        Param(Numeric),
        Param(Numeric, default = NumberValue(0.0)),
        Param(Numeric, default = NumberValue(0.0)),
      ) { args ->
        NumberValue(
          fv(args.number(0), args.number(1), args.number(2), args.number(3), args.number(4)),
        )
      },
    "CUMIPMT" to cumulativePayment(::ipmt),
    "CUMPRINC" to cumulativePayment(::ppmt),
    "NPER" to
      formulaFunction(
        Param(Numeric),
        Param(Numeric),
        Param(Numeric),
        Param(Numeric, default = NumberValue(0.0)),
        Param(Numeric, default = NumberValue(0.0)),
      ) { args ->
        nper(args.number(0), args.number(1), args.number(2), args.number(3), args.number(4))
      },
    "RATE" to
      formulaFunction(
        Param(Numeric, greaterThan = 0.0),
        Param(Numeric),
        Param(Numeric),
        Param(Numeric, default = NumberValue(0.0)),
        Param(Numeric, default = NumberValue(0.0)),
        Param(Numeric, default = NumberValue(0.1)),
      ) { args ->
        rate(
          periods = args.number(0),
          payment = args.number(1),
          present = args.number(2),
          future = args.number(3),
          type = if (isTruthy(args.number(4))) 1.0 else 0.0,
          guess = args.number(5),
        )
      },
    "PV" to
      formulaFunction(
        Param(Numeric),
        Param(Numeric),
        Param(Numeric),
        Param(Numeric, default = NumberValue(0.0)),
        Param(Numeric, default = NumberValue(0.0)),
      ) { args ->
        pv(args.number(0), args.number(1), args.number(2), args.number(3), args.number(4))
      },
    "NPV" to
      formulaFunction(Param(Numeric), Param(Anything), repeatLastArgs = 1) { args ->
        exactNumbers((1 until args.size).map(args::value)) { npv(args.number(0), it) }
      },
    "MIRR" to
      formulaFunction(Param(Range), Param(Numeric), Param(Numeric)) { args ->
        exactNumbers(listOf(args.array(0))) { mirr(it, args.number(1), args.number(2)) }
      },
    "FVSCHEDULE" to
      formulaFunction(Param(Numeric), Param(Range)) { args ->
        fvSchedule(args.number(0), args.array(1).rows.flatten())
      },
    "XNPV" to
      formulaFunction(Param(Numeric, greaterThan = -1.0), Param(Range), Param(Range)) { args ->
        xnpv(args.number(0), args.array(1).rows.flatten(), args.array(2).rows.flatten())
      },
  )

private fun periodPayment(
  calculate: (Double, Double, Double, Double, Double, Double) -> Double,
): FormulaFunction =
  formulaFunction(
    Param(Numeric),
    Param(Numeric),
    Param(Numeric),
    Param(Numeric),
    Param(Numeric, default = NumberValue(0.0)),
    Param(Numeric, default = NumberValue(0.0)),
  ) { args ->
    NumberValue(
      calculate(
        args.number(0),
        args.number(1),
        args.number(2),
        args.number(3),
        args.number(4),
        args.number(5),
      ),
    )
  }

private fun cumulativePayment(
  calculate: (Double, Double, Double, Double, Double, Double) -> Double,
): FormulaFunction =
  formulaFunction(
    Param(Numeric, greaterThan = 0.0),
    Param(Numeric, greaterThan = 0.0),
    Param(Numeric, greaterThan = 0.0),
    Param(Integral, minValue = 1.0),
    Param(Integral, minValue = 1.0),
    Param(Integral, minValue = 0.0, maxValue = 1.0),
  ) { args ->
    val rate = args.number(0)
    val periods = args.number(1)
    val value = args.number(2)
    val start = args.number(3)
    val end = args.number(4)
    val type = args.number(5)
    if (start > end) {
      ErrorValue(Num, StartAfterEnd)
    } else {
      var total = 0.0
      var period = start
      while (period <= end) {
        total += calculate(rate, period, periods, value, 0.0, type)
        period++
      }
      NumberValue(total)
    }
  }

// JavaScript's truthiness for numbers
private fun isTruthy(value: Double): Boolean = value != 0.0 && !value.isNaN()

private fun pmt(
  rate: Double,
  periods: Double,
  present: Double,
  future: Double,
  type: Double,
): Double {
  if (rate == 0.0) return (-present - future) / periods
  val term = (1 + rate).pow(periods)
  val timing = if (isTruthy(type)) 1 / (1 + rate) else 1.0
  return (future * rate + present * rate * term) * timing / (1 - term)
}

private fun ipmt(
  rate: Double,
  period: Double,
  periods: Double,
  present: Double,
  future: Double,
  type: Double,
): Double {
  val payment = pmt(rate, periods, present, future, type)
  return when {
    period == 1.0 -> rate * if (isTruthy(type)) 0.0 else -present
    isTruthy(type) -> rate * (fv(rate, period - 2, payment, present, type) - payment)
    else -> rate * fv(rate, period - 1, payment, present, type)
  }
}

private fun ppmt(
  rate: Double,
  period: Double,
  periods: Double,
  present: Double,
  future: Double,
  type: Double,
): Double =
  pmt(rate, periods, present, future, type) - ipmt(rate, period, periods, present, future, type)

private fun fv(
  rate: Double,
  periods: Double,
  payment: Double,
  value: Double,
  type: Double,
): Double {
  if (rate == 0.0) return -value - payment * periods
  val term = (1 + rate).pow(periods)
  return payment * (if (isTruthy(type)) 1 + rate else 1.0) * (1 - term) / rate - value * term
}

private fun nper(
  rate: Double,
  payment: Double,
  present: Double,
  future: Double,
  type: Double,
): FormulaValue {
  if (rate == 0.0) {
    return if (payment == 0.0) ErrorValue(DivByZero) else NumberValue((-present - future) / payment)
  }
  val adjusted = if (isTruthy(type)) payment * (1 + rate) else payment
  return NumberValue(ln((adjusted - future * rate) / (present * rate + adjusted)) / ln(1 + rate))
}

// Newton's method, giving up after a fixed number of steps
@Suppress("LongParameterList")
private fun rate(
  periods: Double,
  payment: Double,
  present: Double,
  future: Double,
  type: Double,
  guess: Double,
): FormulaValue {
  if (guess <= -1) return ErrorValue(Value)
  var rate = guess
  repeat(RATE_MAX_ITERATIONS) {
    if (rate <= -1) return ErrorValue(Num)
    val y =
      if (abs(rate) < RATE_EPSILON) {
        present * (1 + periods * rate) + payment * (1 + rate * type) * periods + future
      } else {
        val f = (1 + rate).pow(periods)
        present * f + payment * (1 / rate + type) * (f - 1) + future
      }
    if (abs(y) < RATE_EPSILON) return NumberValue(rate)
    val dy =
      if (abs(rate) < RATE_EPSILON) {
        present * periods + payment * type * periods
      } else {
        val f = (1 + rate).pow(periods)
        val df = periods * (1 + rate).pow(periods - 1)
        present * df + payment * (1 / rate + type) * df + payment * (-1 / (rate * rate)) * (f - 1)
      }
    rate -= y / dy
  }
  return ErrorValue(Num)
}

private fun pv(
  rate: Double,
  periods: Double,
  payment: Double,
  future: Double,
  type: Double,
): FormulaValue {
  val timing = if (isTruthy(type)) 1.0 else 0.0
  val term = (1 + rate).pow(periods)
  return when {
    rate == -1.0 -> if (periods == 0.0) ErrorValue(Num) else ErrorValue(DivByZero)
    rate == 0.0 -> NumberValue(-payment * periods - future)
    else -> NumberValue(((1 - term) * payment * (1 + rate * timing) / rate - future) / term)
  }
}

private fun npv(rate: Double, values: List<Double>): FormulaValue {
  var total = 0.0
  for (value in values.asReversed()) {
    total += value
    if (rate == -1.0) {
      if (total == 0.0) continue
      return ErrorValue(DivByZero)
    }
    total /= 1 + rate
  }
  return NumberValue(total)
}

private fun mirr(values: List<Double>, financeRate: Double, reinvestRate: Double): FormulaValue {
  if (values.none { it > 0 } || values.none { it < 0 }) return ErrorValue(DivByZero)
  val positive = npv(reinvestRate, values.map { max(it, 0.0) })
  if (positive !is NumberValue) return positive
  val negative = npv(financeRate, values.map { min(it, 0.0) })
  if (negative !is NumberValue) return negative
  val n = values.size
  val growth = -positive.value * (1 + reinvestRate).pow(n) / negative.value / (1 + financeRate)
  return NumberValue(growth.pow(1.0 / (n - 1)) - 1)
}

private fun fvSchedule(principal: Double, rates: List<FormulaValue>): FormulaValue {
  rates
    .firstOrNull { it is ErrorValue }
    ?.let {
      return it
    }
  var value = principal
  for (rate in rates) {
    if (rate is NumberValue) {
      value *= 1 + rate.value
    } else if (rate != EmptyValue) {
      return ErrorValue(Value, NumberCoercion)
    }
  }
  return NumberValue(value)
}

private fun xnpv(
  rate: Double,
  values: List<FormulaValue>,
  dates: List<FormulaValue>,
): FormulaValue {
  if ((values + dates).any { it !is NumberValue }) return ErrorValue(Value, NumberCoercion)
  val amounts = values.map { (it as NumberValue).value }
  val days = dates.map { floor((it as NumberValue).value) }
  if (days.size != amounts.size) return ErrorValue(Num, ArraySizeMismatch)
  // Upstream checks the first date before rounding it down
  val first = days[0]
  val raw = (dates[0] as NumberValue).value
  if (raw < 0 || days.any { it < first }) return ErrorValue(Num, ValueTooSmall)
  val total = amounts.indices.sumOf { amounts[it] / (1 + rate).pow((days[it] - first) / 365) }
  return NumberValue(total)
}

private const val RATE_EPSILON = 1e-7
private const val RATE_MAX_ITERATIONS = 50
