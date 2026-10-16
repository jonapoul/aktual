package aktual.budget.formula.domain

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.math.truncate

// Ports of HyperFormula's NumericAggregationPlugin, RoundingPlugin and the single function plugins
// (ABS, MOD, POWER, SIGN, SQRT)
internal val MATH_FUNCTIONS: Map<String, FormulaFunction> =
  mapOf(
    "SUM" to aggregate(::strictlyNumbers) { NumberValue(it.fold(0.0, ::addWithEpsilon)) },
    "PRODUCT" to aggregate(::strictlyNumbers) { NumberValue(it.fold(1.0) { a, b -> a * b }) },
    "MIN" to
      aggregate(::strictlyNumbers) {
        NumberValue(zeroForInfinite(it.fold(Double.POSITIVE_INFINITY, ::min)))
      },
    "MAX" to
      aggregate(::strictlyNumbers) {
        NumberValue(zeroForInfinite(it.fold(Double.NEGATIVE_INFINITY, ::max)))
      },
    "COUNT" to
      aggregate({ NumberValue(if (it is NumberValue) 1.0 else 0.0) }) { NumberValue(it.sum()) },
    "AVERAGE" to
      aggregate(::strictlyNumbers) {
        if (it.isEmpty()) ErrorValue(DivByZero) else NumberValue(it.sum() / it.size)
      },
    "ABS" to formulaFunction(Param(Numeric)) { args -> NumberValue(abs(args.number(0))) },
    "SQRT" to formulaFunction(Param(Numeric)) { args -> NumberValue(sqrt(args.number(0))) },
    "POWER" to
      formulaFunction(Param(Numeric), Param(Numeric)) { args ->
        NumberValue(args.number(0).pow(args.number(1)))
      },
    "MOD" to
      formulaFunction(Param(Numeric), Param(Numeric)) { args ->
        val divisor = args.number(1)
        if (divisor == 0.0) ErrorValue(DivByZero) else NumberValue(args.number(0) % divisor)
      },
    "SIGN" to
      formulaFunction(Param(Numeric)) { args ->
        val number = args.number(0)
        NumberValue(
          when {
            number > 0 -> 1.0
            number < 0 -> -1.0
            else -> 0.0
          },
        )
      },
    "INT" to formulaFunction(Param(Numeric)) { args -> NumberValue(truncate(args.number(0))) },
    "ROUND" to rounding(::jsRound),
    "ROUNDUP" to rounding(::ceil),
    "ROUNDDOWN" to rounding(::floor),
    "TRUNC" to rounding(::floor),
    "CEILING" to significance(::ceil),
    "FLOOR" to significance(::floor),
  )

// Port of NumericAggregationPlugin.reduce. [coerce] turns each value into a number to reduce, an
// error that stops the reduction, or null to skip it. Plain arguments go through number coercion
// first, but array elements and cell references don't
private fun aggregate(
  coerce: (FormulaValue) -> FormulaValue?,
  reduce: (List<Double>) -> FormulaValue,
): FormulaFunction = FormulaFunction { scope, args ->
  if (args.isEmpty()) return@FormulaFunction ErrorValue(NotAvailable, WrongArgCount)
  val numbers = mutableListOf<Double>()
  for (arg in args) {
    val value = scope.evaluate(arg)
    val values =
      when {
        value is ArrayValue -> value.rows.flatten().map(coerce)
        arg is CellReferenceNode -> listOf(coerce(value))
        else -> listOf(coerce(coerceToNumber(value)))
      }
    for (coerced in values) {
      if (coerced is ErrorValue) return@FormulaFunction coerced
      if (coerced is NumberValue) numbers += coerced.value
    }
  }
  reduce(numbers)
}

private fun strictlyNumbers(value: FormulaValue): FormulaValue? = value.takeIf {
  it is NumberValue || it is ErrorValue
}

private fun zeroForInfinite(value: Double): Double = if (value.isFinite()) value else 0.0

private fun rounding(round: (Double) -> Double): FormulaFunction =
  formulaFunction(Param(Numeric), Param(Numeric, default = NumberValue(0.0))) { args ->
    val number = args.number(0)
    val multiplier = 10.0.pow(args.number(1))
    val rounded =
      if (number < 0) {
        -round(-number * multiplier) / multiplier
      } else {
        round(number * multiplier) / multiplier
      }
    NumberValue(rounded)
  }

private fun significance(round: (Double) -> Double): FormulaFunction =
  formulaFunction(Param(Numeric), Param(Numeric)) { args ->
    val value = args.number(0)
    val significance = args.number(1)
    when {
      value == 0.0 -> NumberValue(0.0)
      significance == 0.0 -> ErrorValue(DivByZero)
      value > 0 && significance < 0 -> ErrorValue(Num, DistinctSigns)
      else -> NumberValue(round(value / significance) * significance)
    }
  }
