package aktual.budget.formula.domain

import kotlin.math.floor

internal fun interface FormulaFunction {
  suspend fun call(scope: EvaluationScope, args: List<FormulaNode>): FormulaValue
}

// HyperFormula's FunctionArgumentType
internal enum class ArgType {
  Numeric,
  Integral,
  Text,
  Logical,
  Scalar,
  NoError,
  Anything,
  Range,
}

internal data class Param(
  val type: ArgType,
  val optional: Boolean = false,
  val default: FormulaValue? = null,
  val minValue: Double? = null,
  val maxValue: Double? = null,
  val greaterThan: Double? = null,
  val lessThan: Double? = null,
)

// Coerced argument values, null where an optional argument without a default was left out
internal class FunctionArgs(private val values: List<FormulaValue?>) {
  val size: Int
    get() = values.size

  operator fun get(index: Int): FormulaValue? = values.getOrNull(index)

  fun number(index: Int): Double = (values[index] as NumberValue).value

  fun text(index: Int): String = (values[index] as TextValue).value

  fun boolean(index: Int): Boolean = (values[index] as BooleanValue).value
}

// Port of HyperFormula's FunctionPlugin.runFunction: evaluates every argument, checks the count,
// fills in defaults and coerces each value to its parameter's type before calling [body]
internal fun formulaFunction(
  vararg params: Param,
  repeatLastArgs: Int = 0,
  expandRanges: Boolean = false,
  body: suspend EvaluationScope.(FunctionArgs) -> FormulaValue,
): FormulaFunction = FormulaFunction { scope, args ->
  scope.runFunction(args, params.toList(), repeatLastArgs, expandRanges, body)
}

internal const val WRONG_ARG_NUMBER_MESSAGE = "Wrong number of arguments."
private const val WRONG_TYPE_MESSAGE = "Wrong type of argument."

private suspend fun EvaluationScope.runFunction(
  args: List<FormulaNode>,
  params: List<Param>,
  repeatLastArgs: Int,
  expandRanges: Boolean,
  body: suspend EvaluationScope.(FunctionArgs) -> FormulaValue,
): FormulaValue {
  // Each value is paired with whether it came out of an expanded array, so can be skipped
  val values =
    if (expandRanges) {
      args.flatMap { arg ->
        val value = evaluate(arg)
        if (value is ArrayValue) value.rows.flatten().map { it to true } else listOf(value to false)
      }
    } else {
      args.map { evaluate(it) to false }
    }

  val metadata = params.toMutableList()
  if (repeatLastArgs > 0) {
    while (values.size > metadata.size) metadata += params.takeLast(repeatLastArgs)
  }
  if (values.size > metadata.size) return ErrorValue(NotAvailable, WRONG_ARG_NUMBER_MESSAGE)
  val missingAreOptional = metadata.drop(values.size).all { it.optional || it.default != null }
  if (!missingAreOptional) return ErrorValue(NotAvailable, WRONG_ARG_NUMBER_MESSAGE)

  val coerced = ArrayList<FormulaValue?>(metadata.size)
  for ((index, param) in metadata.withIndex()) {
    val argument = values.getOrNull(index)
    val value = argument?.first ?: param.default
    if (value == null) {
      coerced += null
      continue
    }
    val coercedValue = coerceToType(value, param)
    val ignorable = argument?.second == true
    if (coercedValue == null && !ignorable)
      return ErrorValue(FormulaErrorType.Value, WRONG_TYPE_MESSAGE)
    if (coercedValue is ErrorValue && param.type != Scalar) return coercedValue
    coerced += coercedValue
  }
  return body(FunctionArgs(coerced))
}

private fun coerceToType(value: FormulaValue, param: Param): FormulaValue? {
  if (value is ArrayValue && (param.type == Range || param.type == Anything)) return value
  val scalar = value.toScalar()
  return when (param.type) {
    Numeric,
    Integral -> coerceToNumberParam(scalar, param)
    ArgType.Text -> coerceToText(scalar)
    Logical -> coerceToBoolean(scalar)
    Scalar,
    NoError,
    Anything -> scalar
    Range -> scalar as? ErrorValue ?: ArrayValue(listOf(listOf(scalar)))
  }
}

private fun coerceToNumberParam(value: FormulaValue, param: Param): FormulaValue {
  val coerced = coerceToNumber(value)
  if (coerced !is NumberValue) return coerced
  val number = coerced.value
  return when {
    param.maxValue != null && number > param.maxValue -> ErrorValue(Num, VALUE_LARGE_MESSAGE)
    param.minValue != null && number < param.minValue -> ErrorValue(Num, VALUE_SMALL_MESSAGE)
    param.lessThan != null && number >= param.lessThan -> ErrorValue(Num, VALUE_LARGE_MESSAGE)
    param.greaterThan != null && number <= param.greaterThan -> ErrorValue(Num, VALUE_SMALL_MESSAGE)
    param.type == Integral && number != floor(number) -> ErrorValue(Num, INTEGER_EXPECTED_MESSAGE)
    else -> coerced
  }
}

private const val VALUE_LARGE_MESSAGE = "Value too large."
private const val VALUE_SMALL_MESSAGE = "Value too small."
private const val INTEGER_EXPECTED_MESSAGE = "Value needs to be an integer."
