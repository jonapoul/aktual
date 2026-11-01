package aktual.budget.formula

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan
import kotlin.math.truncate

// Ports of HyperFormula's NumericAggregationPlugin, RoundingPlugin, MedianPlugin, CountBlankPlugin,
// LogarithmPlugin, SumprodPlugin, TrigonometryPlugin and the single function plugins (ABS, EXP,
// MOD,
// PI, POWER, SIGN, SQRT)
internal val MATH_FUNCTIONS: Map<String, FormulaFunction> =
  mapOf(
    "SUM" to aggregate(::strictlyNumbers) { NumberValue(it.fold(0.0, ::addWithEpsilon)) },
    "PRODUCT" to aggregate(::strictlyNumbers) { NumberValue(it.fold(1.0) { a, b -> a * b }) },
    "MIN" to
      aggregate(::strictlyNumbers) {
        NumberValue(zeroForInfinite(it.fold(POSITIVE_INFINITY, ::min)))
      },
    "MAX" to
      aggregate(::strictlyNumbers) {
        NumberValue(zeroForInfinite(it.fold(NEGATIVE_INFINITY, ::max)))
      },
    "COUNT" to
      aggregate({ NumberValue(if (it is NumberValue) 1.0 else 0.0) }) { NumberValue(it.sum()) },
    "AVERAGE" to
      aggregate(::strictlyNumbers) {
        if (it.isEmpty()) ErrorValue(DivByZero) else NumberValue(it.sum() / it.size)
      },
    "SUMSQ" to
      aggregate(::strictlyNumbers) {
        NumberValue(it.fold(0.0) { total, number -> addWithEpsilon(total, number.pow(2)) })
      },
    "MINA" to
      aggregate(::numbersBooleans) {
        NumberValue(zeroForInfinite(it.fold(POSITIVE_INFINITY, ::min)))
      },
    "MAXA" to
      aggregate(::numbersBooleans) {
        NumberValue(zeroForInfinite(it.fold(NEGATIVE_INFINITY, ::max)))
      },
    "AVERAGEA" to
      aggregate(::numbersBooleans) {
        if (it.isEmpty()) ErrorValue(DivByZero) else NumberValue(it.sum() / it.size)
      },
    "SUMPRODUCT" to
      formulaFunction(Param(Range), repeatLastArgs = 1) { args ->
        sumProduct((0 until args.size).map(args::array))
      },
    "COUNTA" to
      aggregate({ NumberValue(if (it == EmptyValue) 0.0 else 1.0) }) { NumberValue(it.sum()) },
    "COUNTBLANK" to
      formulaFunction(Param(Scalar), repeatLastArgs = 1, expandRanges = true) { args ->
        NumberValue((0 until args.size).count { args[it] == EmptyValue }.toDouble())
      },
    "VAR" to moments { it.sampleVariance() },
    "VARP" to moments { it.populationVariance() },
    "STDEV" to moments { it.sampleVariance()?.let(::sqrt) },
    "STDEVP" to moments { it.populationVariance()?.let(::sqrt) },
    "MEDIAN" to
      formulaFunction(Param(Anything), repeatLastArgs = 1) { args ->
        exactNumbers((0 until args.size).map(args::value), ::median)
      },
    "EXP" to formulaFunction(Param(Numeric)) { args -> NumberValue(exp(args.number(0))) },
    "LN" to formulaFunction(Param(Numeric)) { args -> NumberValue(ln(args.number(0))) },
    "LOG" to
      formulaFunction(
        Param(Numeric, greaterThan = 0.0),
        Param(Numeric, default = NumberValue(10.0), greaterThan = 0.0),
      ) { args ->
        NumberValue(ln(args.number(0)) / ln(args.number(1)))
      },
    "LOG10" to formulaFunction(Param(Numeric)) { args -> NumberValue(log10(args.number(0))) },
    "PI" to formulaFunction { NumberValue(PI) },
    "SIN" to formulaFunction(Param(Numeric)) { args -> NumberValue(sin(args.number(0))) },
    "COS" to formulaFunction(Param(Numeric)) { args -> NumberValue(cos(args.number(0))) },
    "TAN" to formulaFunction(Param(Numeric)) { args -> NumberValue(tan(args.number(0))) },
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

// Sums kept in the same order as upstream's MomentsAggregate, so rounding matches
private class Moments(numbers: List<Double>) {
  private val count = numbers.size
  private val sum = numbers.fold(0.0) { total, number -> total + number }
  private val sumOfSquares = numbers.fold(0.0) { total, number -> total + number * number }

  fun sampleVariance(): Double? =
    if (count > 1) (sumOfSquares - sum * sum / count) / (count - 1) else null

  fun populationVariance(): Double? =
    if (count > 0) (sumOfSquares - sum * sum / count) / count else null
}

private fun moments(result: (Moments) -> Double?): FormulaFunction =
  aggregate(::strictlyNumbers) { numbers ->
    result(Moments(numbers))?.let(::NumberValue) ?: ErrorValue(DivByZero)
  }

private fun median(numbers: List<Double>): FormulaValue {
  if (numbers.isEmpty()) return ErrorValue(Num, NoValues)
  val sorted = numbers.sorted()
  val middle = sorted.size / 2
  return NumberValue(
    if (sorted.size % 2 == 0) (sorted[middle - 1] + sorted[middle]) / 2 else sorted[middle],
  )
}

private fun strictlyNumbers(value: FormulaValue): FormulaValue? = value.takeIf {
  it is NumberValue || it is ErrorValue
}

// Booleans count as numbers and text as 0, but only in arrays since plain arguments are coerced to
// numbers first
private fun numbersBooleans(value: FormulaValue): FormulaValue? =
  when (value) {
    is BooleanValue -> NumberValue(if (value.value) 1.0 else 0.0)
    is NumberValue,
    is ErrorValue -> value
    is TextValue -> NumberValue(0.0)
    EmptyValue,
    is ArrayValue -> null
  }

// Values that aren't numbers make their product 0, but errors still stop the sum
private fun sumProduct(arrays: List<ArrayValue>): FormulaValue {
  if (arrays.any { !it.hasSizeOf(arrays[0]) }) return ErrorValue(Value, ArraySizeMismatch)
  val columns = arrays.map { it.rows.flatten() }
  var total = 0.0
  for (i in columns[0].indices) {
    var product = 1.0
    for (column in columns) {
      val value = column[i]
      if (value is ErrorValue) return value
      val number = coerceToNumber(value)
      product = if (number is NumberValue) product * number.value else 0.0
    }
    total += product
  }
  return NumberValue(total)
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

// Upstream rounds pi to 14 decimal places
private const val PI = 3.14159265358979
